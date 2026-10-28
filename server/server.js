const http = require("http");
const url = require("url");
const path = require("path");
const fs = require("fs");
const { WebSocketServer, WebSocket } = require("ws");
const { Pool } = require("pg");

const PORT = process.env.PORT || 3000;
const DATABASE_URL = process.env.DATABASE_URL || process.env.POSTGRES_URL;

const { getCatalog, handleAdminRoutes } = require("./admin");

// Dynamic Curated Remote Avatar Catalog (Boys & Girls)
function getActiveCatalog() {
  return getCatalog();
}

// Pending partner pairing requests: recipientCode -> { fromCode, fromName, fromAvatarId, createdAt }
const pendingPairingRequests = new Map();

// ==========================================
// 1. PostgreSQL Database & In-Memory Fallback
// ==========================================
let pool = null;
let isDbConnected = false;

// In-Memory Fallbacks for zero-crash stability
const memoryClients = new Map(); // code -> { sockets: Set, partnerCode, connectedAt }
const memoryNotes = new Map(); // id -> { id, authorCode, recipientCode, authorName, content, createdAt, isDeleted }
const memoryChat = []; // array of { id, fromCode, toCode, senderName, content, messageType, timestamp, isRead }
const memoryCallLogs = []; // array of { id, fromCode, toCode, callType, durationSeconds, createdAt }
const activeCallSessions = new Map(); // callId -> session object

function endCallSession(session, reason = "NORMAL", statusMessage = "Call ended") {
  if (!session || session.isCleanedUp) return;
  session.isCleanedUp = true;
  session.status = "ENDED";
  if (session.graceTimer) {
    clearTimeout(session.graceTimer);
    session.graceTimer = null;
  }
  const duration = session.connectedAt ? Math.max(0, Math.round((Date.now() - session.connectedAt) / 1000)) : 0;
  
  const endPayload = {
    type: "call_ended",
    callId: session.callId,
    reason,
    statusMessage,
    durationSeconds: duration
  };

  forwardToTarget(session.callerCode, endPayload);
  forwardToTarget(session.recipientCode, endPayload);

  if (duration > 0 || reason === "MISSED") {
    dbSaveCallLog(session.callId, session.callerCode, session.recipientCode, session.callType || "VOICE", duration, Date.now());
  }

  activeCallSessions.delete(session.callId);
  console.log(`[CALL-SESSION] Ended call ${session.callId} (${reason}, duration=${duration}s)`);
}

if (DATABASE_URL || process.env.PGHOST) {
  try {
    pool = new Pool({
      connectionString: DATABASE_URL,
      ssl: DATABASE_URL && (DATABASE_URL.includes("localhost") || DATABASE_URL.includes("127.0.0.1")) 
        ? false 
        : (DATABASE_URL ? { rejectUnauthorized: false } : undefined)
    });

    pool.connect((err, client, release) => {
      if (err) {
        console.warn("⚠️  PostgreSQL connection failed, operating with in-memory persistence:", err.message);
        isDbConnected = false;
      } else {
        isDbConnected = true;
        console.log("✅ PostgreSQL connected successfully!");
        release();
        initDatabaseTables();
      }
    });
  } catch (err) {
    console.warn("PostgreSQL initialization notice:", err.message);
  }
} else {
  console.log("ℹ️  No DATABASE_URL configured. Running with in-memory couple database.");
}

async function initDatabaseTables() {
  if (!pool || !isDbConnected) return;
  try {
    // 1. Couples table
    await pool.query(`
      CREATE TABLE IF NOT EXISTS couples (
        pair_code VARCHAR(32) PRIMARY KEY,
        partner_code VARCHAR(32),
        user_name VARCHAR(128),
        partner_status TEXT DEFAULT 'Thinking of you always ❤️',
        avatar_id VARCHAR(64) DEFAULT 'boy_001',
        last_seen BIGINT,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );
    `);
    await pool.query(`
      ALTER TABLE couples ADD COLUMN IF NOT EXISTS avatar_id VARCHAR(64) DEFAULT 'boy_001';
      ALTER TABLE couples ADD COLUMN IF NOT EXISTS avatar_url TEXT DEFAULT '';
      ALTER TABLE couples ADD COLUMN IF NOT EXISTS is_blocked BOOLEAN DEFAULT FALSE;
      ALTER TABLE couples ADD COLUMN IF NOT EXISTS blue_tick BOOLEAN DEFAULT FALSE;
      ALTER TABLE couples ADD COLUMN IF NOT EXISTS green_tick BOOLEAN DEFAULT FALSE;
    `).catch(() => {});

    // 1.5. Pairing Requests table
    await pool.query(`
      CREATE TABLE IF NOT EXISTS pairing_requests (
        id VARCHAR(64) PRIMARY KEY,
        from_code VARCHAR(32) NOT NULL,
        to_code VARCHAR(32) NOT NULL,
        from_name VARCHAR(128),
        from_avatar_id VARCHAR(64),
        from_avatar_url TEXT DEFAULT '',
        status VARCHAR(20) DEFAULT 'PENDING',
        created_at BIGINT,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );
    `);

    // 2. Call Logs table
    await pool.query(`
      CREATE TABLE IF NOT EXISTS call_logs (
        id VARCHAR(64) PRIMARY KEY,
        from_code VARCHAR(32),
        to_code VARCHAR(32),
        call_type VARCHAR(16),
        duration_seconds INT DEFAULT 0,
        created_at BIGINT
      );
    `);

    // 3. Shared Notes (24h) table
    await pool.query(`
      CREATE TABLE IF NOT EXISTS shared_notes (
        id VARCHAR(64) PRIMARY KEY,
        author_code VARCHAR(32),
        recipient_code VARCHAR(32),
        author_name VARCHAR(128),
        content TEXT,
        created_at BIGINT,
        is_deleted BOOLEAN DEFAULT FALSE
      );
    `);

    // 4. Chat Messages table
    await pool.query(`
      CREATE TABLE IF NOT EXISTS chat_messages (
        id VARCHAR(64) PRIMARY KEY,
        from_code VARCHAR(32),
        to_code VARCHAR(32),
        sender_name VARCHAR(128),
        content TEXT,
        message_type VARCHAR(16) DEFAULT 'text',
        timestamp BIGINT,
        is_read BOOLEAN DEFAULT FALSE
      );
    `);

    console.log("✅ PostgreSQL tables initialized: couples, pairing_requests, call_logs, shared_notes, chat_messages");
  } catch (err) {
    console.error("Failed to create tables in PostgreSQL:", err.message);
  }
}

// Data access operations
async function dbSaveCouple(pairCode, partnerCode, userName, avatarId, avatarUrl) {
  let entry = memoryClients.get(pairCode) || { sockets: new Set() };
  entry.partnerCode = partnerCode;
  if (userName) entry.userName = userName;
  if (avatarId) entry.avatarId = avatarId;
  if (avatarUrl) entry.avatarUrl = avatarUrl;
  memoryClients.set(pairCode, entry);

  if (pool && isDbConnected) {
    try {
      await pool.query(`
        INSERT INTO couples (pair_code, partner_code, user_name, avatar_id, avatar_url, last_seen, updated_at)
        VALUES ($1, $2, $3, $4, $5, $6, NOW())
        ON CONFLICT (pair_code) DO UPDATE
        SET partner_code = COALESCE(EXCLUDED.partner_code, couples.partner_code),
            user_name = COALESCE(NULLIF(EXCLUDED.user_name, ''), couples.user_name),
            avatar_id = COALESCE(NULLIF(EXCLUDED.avatar_id, ''), couples.avatar_id),
            avatar_url = COALESCE(NULLIF(EXCLUDED.avatar_url, ''), couples.avatar_url),
            last_seen = EXCLUDED.last_seen,
            updated_at = NOW();
      `, [pairCode, partnerCode, userName || "You", avatarId || "boy_001", avatarUrl || "", Date.now()]);
    } catch (e) {
      console.warn("DB save couple error:", e.message);
    }
  }
}

async function dbUpdateAvatar(pairCode, avatarId, avatarUrl) {
  let entry = memoryClients.get(pairCode);
  if (!entry) {
    entry = { sockets: new Set(), partnerCode: "", connectedAt: Date.now(), userName: "You", avatarId, avatarUrl: avatarUrl || "" };
    memoryClients.set(pairCode, entry);
  } else {
    entry.avatarId = avatarId;
    if (avatarUrl) entry.avatarUrl = avatarUrl;
  }

  if (pool && isDbConnected) {
    try {
      if (avatarUrl) {
        await pool.query(`
          UPDATE couples SET avatar_id = $1, avatar_url = $2, updated_at = NOW() WHERE pair_code = $3;
        `, [avatarId, avatarUrl, pairCode]);
      } else {
        await pool.query(`
          UPDATE couples SET avatar_id = $1, updated_at = NOW() WHERE pair_code = $2;
        `, [avatarId, pairCode]);
      }
    } catch (e) {
      console.warn("DB update avatar error:", e.message);
    }
  }
}

async function dbGetProfile(pairCode) {
  let prof = null;
  if (pool && isDbConnected) {
    try {
      const res = await pool.query(`
        SELECT pair_code AS "pairCode", partner_code AS "partnerCode", user_name AS "userName",
               partner_status AS "partnerStatus", COALESCE(avatar_id, 'boy_001') AS "avatarId",
               COALESCE(avatar_url, '') AS "avatarUrl", COALESCE(is_blocked, FALSE) AS "isBlocked",
               COALESCE(blue_tick, FALSE) AS "blueTick", COALESCE(green_tick, FALSE) AS "greenTick"
        FROM couples WHERE pair_code = $1;
      `, [pairCode]);
      if (res.rows.length > 0) prof = res.rows[0];
    } catch (e) {}
  }
  if (!prof) {
    const entry = memoryClients.get(pairCode);
    prof = {
      pairCode,
      partnerCode: entry?.partnerCode || "",
      userName: entry?.userName || "You",
      partnerStatus: "Thinking of you always ❤️",
      avatarId: entry?.avatarId || "boy_001",
      avatarUrl: entry?.avatarUrl || "",
      isBlocked: !!entry?.isBlocked,
      blueTick: !!entry?.blueTick,
      greenTick: !!entry?.greenTick
    };
  }

  // Auto-resolve avatarUrl from catalog if missing or empty
  if (!prof.avatarUrl) {
    const match = getActiveCatalog().find(a => a.id === prof.avatarId);
    if (match && (match.uploadThingUrl || match.imageUrl)) {
      prof.avatarUrl = match.uploadThingUrl || match.imageUrl;
    }
  }

  return prof;
}

async function dbSaveCallLog(id, fromCode, toCode, callType, duration, createdAt) {
  if (pool && isDbConnected) {
    try {
      await pool.query(`
        INSERT INTO call_logs (id, from_code, to_code, call_type, duration_seconds, created_at)
        VALUES ($1, $2, $3, $4, $5, $6)
        ON CONFLICT (id) DO NOTHING;
      `, [id, fromCode, toCode, callType, duration || 0, createdAt || Date.now()]);
    } catch (e) {
      console.warn("DB save call log error:", e.message);
    }
  } else {
    memoryCallLogs.unshift({ id, fromCode, toCode, callType, durationSeconds: duration || 0, createdAt: createdAt || Date.now() });
    if (memoryCallLogs.length > 100) memoryCallLogs.pop();
  }
}

async function dbSaveSharedNote(id, authorCode, recipientCode, authorName, content, createdAt) {
  if (pool && isDbConnected) {
    try {
      await pool.query(`
        INSERT INTO shared_notes (id, author_code, recipient_code, author_name, content, created_at, is_deleted)
        VALUES ($1, $2, $3, $4, $5, $6, FALSE)
        ON CONFLICT (id) DO UPDATE
        SET content = EXCLUDED.content, is_deleted = FALSE;
      `, [id, authorCode, recipientCode, authorName || "Partner", content, createdAt || Date.now()]);
    } catch (e) {
      console.warn("DB save note error:", e.message);
    }
  } else {
    memoryNotes.set(id, { id, authorCode, recipientCode, authorName: authorName || "Partner", content, createdAt: createdAt || Date.now(), isDeleted: false });
  }
}

async function dbDeleteSharedNote(id) {
  if (pool && isDbConnected) {
    try {
      await pool.query(`UPDATE shared_notes SET is_deleted = TRUE WHERE id = $1`, [id]);
    } catch (e) {
      console.warn("DB delete note error:", e.message);
    }
  } else {
    const existing = memoryNotes.get(id);
    if (existing) existing.isDeleted = true;
  }
}

async function dbGetActiveNote(myCode, partnerCode) {
  const cutoff = Date.now() - (24 * 60 * 60 * 1000);
  if (pool && isDbConnected) {
    try {
      const res = await pool.query(`
        SELECT * FROM shared_notes
        WHERE ((author_code = $1 AND recipient_code = $2) OR (author_code = $2 AND recipient_code = $1))
          AND is_deleted = FALSE
          AND created_at > $3
        ORDER BY created_at DESC
        LIMIT 1;
      `, [myCode, partnerCode, cutoff]);
      return res.rows[0] || null;
    } catch (e) {
      console.warn("DB get note error:", e.message);
    }
  }
  for (const note of Array.from(memoryNotes.values()).reverse()) {
    if (!note.isDeleted && note.createdAt > cutoff) {
      if ((note.authorCode === myCode && note.recipientCode === partnerCode) ||
          (note.authorCode === partnerCode && note.recipientCode === myCode)) {
        return note;
      }
    }
  }
  return null;
}

async function dbSaveChatMessage(id, fromCode, toCode, senderName, content, messageType, timestamp) {
  const ts = timestamp || Date.now();
  if (pool && isDbConnected) {
    try {
      await pool.query(`
        INSERT INTO chat_messages (id, from_code, to_code, sender_name, content, message_type, timestamp, is_read)
        VALUES ($1, $2, $3, $4, $5, $6, $7, FALSE)
        ON CONFLICT (id) DO NOTHING;
      `, [id, fromCode, toCode, senderName || "My Love", content, messageType || "text", ts]);
    } catch (e) {
      console.warn("DB save chat error:", e.message);
    }
  } else {
    memoryChat.push({ id, fromCode, toCode, senderName: senderName || "My Love", content, messageType: messageType || "text", timestamp: ts, isRead: false });
    if (memoryChat.length > 500) memoryChat.shift();
  }
}

async function dbGetChatHistory(myCode, partnerCode, limit = 100) {
  if (pool && isDbConnected) {
    try {
      const res = await pool.query(`
        SELECT id, from_code AS "fromCode", to_code AS "toCode", sender_name AS "senderName",
               content, message_type AS "messageType", timestamp, is_read AS "isRead"
        FROM chat_messages
        WHERE (from_code = $1 AND to_code = $2) OR (from_code = $2 AND to_code = $1)
        ORDER BY timestamp ASC
        LIMIT $3;
      `, [myCode, partnerCode, limit]);
      return res.rows;
    } catch (e) {
      console.warn("DB get chat error:", e.message);
    }
  }
  return memoryChat.filter(msg => 
    (msg.fromCode === myCode && msg.toCode === partnerCode) ||
    (msg.fromCode === partnerCode && msg.toCode === myCode)
  ).slice(-limit);
}

async function dbMarkChatRead(myCode, partnerCode) {
  if (pool && isDbConnected) {
    try {
      await pool.query(`
        UPDATE chat_messages
        SET is_read = TRUE
        WHERE from_code = $1 AND to_code = $2;
      `, [partnerCode, myCode]);
    } catch (e) {}
  } else {
    memoryChat.forEach(msg => {
      if (msg.fromCode === partnerCode && msg.toCode === myCode) {
        msg.isRead = true;
      }
    });
  }
}

// Helper to parse JSON body
function readJsonBody(req) {
  return new Promise((resolve, reject) => {
    let body = "";
    req.on("data", chunk => {
      body += chunk;
      if (body.length > 1e6) {
        req.destroy();
        reject(new Error("Payload too large"));
      }
    });
    req.on("end", () => {
      try {
        resolve(body ? JSON.parse(body) : {});
      } catch (err) {
        resolve({});
      }
    });
    req.on("error", reject);
  });
}

// ==========================================
// 2. HTTP Server & REST Endpoints
// ==========================================
const server = http.createServer(async (req, res) => {
  // Global CORS headers
  res.setHeader("Access-Control-Allow-Origin", "*");
  res.setHeader("Access-Control-Allow-Methods", "GET, POST, PATCH, OPTIONS");
  res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

  if (req.method === "OPTIONS") {
    res.writeHead(204);
    return res.end();
  }

  const parsedUrl = url.parse(req.url, true);
  const pathname = parsedUrl.pathname;

  // Admin Web App & Admin APIs
  const handledAdmin = await handleAdminRoutes(req, res, parsedUrl, readJsonBody, {
    pool,
    isDbConnected,
    memoryClients,
    wss,
    forwardToTarget,
    dbGetProfile
  });
  if (handledAdmin !== false) return;

  // Serve static curated avatar assets
  if (pathname.startsWith("/assets/avatars/")) {
    const relative = pathname.replace(/^\/assets\/avatars\//, "");
    const safePath = path.normalize(relative).replace(/^(\.\.[\/\\])+/, "");
    const fullPath = path.join(__dirname, "assets", "avatars", safePath);
    if (fs.existsSync(fullPath) && fs.statSync(fullPath).isFile()) {
      const ext = path.extname(fullPath).toLowerCase();
      const contentType = ext === ".png" ? "image/png" : (ext === ".jpg" || ext === ".jpeg") ? "image/jpeg" : "image/webp";
      res.writeHead(200, {
        "Content-Type": contentType,
        "Cache-Control": "public, max-age=604800, immutable"
      });
      return fs.createReadStream(fullPath).pipe(res);
    } else {
      res.writeHead(404, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ error: "Avatar asset not found" }));
    }
  }

  // 1. Avatar Catalog API
  if (pathname === "/api/avatars" && req.method === "GET") {
    const category = (parsedUrl.query.category || "").toUpperCase();
    const tag = (parsedUrl.query.tag || "").toLowerCase();
    let filtered = getActiveCatalog();
    if (category) {
      filtered = filtered.filter(a => a.category === category);
    }
    if (tag && tag !== "all") {
      filtered = filtered.filter(a => a.tags && a.tags.some(t => t.toLowerCase() === tag));
    }
    const host = req.headers.host || ("localhost:" + PORT);
    const protocol = req.headers["x-forwarded-proto"] || "http";
    const baseUrl = `${protocol}://${host}`;
    const items = filtered.map(a => {
      const img = a.uploadThingUrl || a.imageUrl || (a.assetPath ? `${baseUrl}/assets/avatars/${a.assetPath}` : `${baseUrl}${a.localUrl}`);
      const thumb = a.uploadThingUrl || a.imageUrl || (a.assetPath ? `${baseUrl}/assets/avatars/${a.assetPath.replace(/^([^/]+)\/(.*)$/, '$1/thumbs/$2')}` : `${baseUrl}${a.localUrl}`);
      return {
        id: a.id,
        name: a.name,
        category: a.category,
        tags: a.tags || [],
        imageUrl: img,
        thumbnailUrl: thumb
      };
    });
    res.writeHead(200, { "Content-Type": "application/json" });
    return res.end(JSON.stringify({ success: true, count: items.length, items }));
  }

  // 2. Single Avatar Detail API
  if (pathname.startsWith("/api/avatars/") && req.method === "GET") {
    const avatarId = pathname.replace("/api/avatars/", "").trim();
    const found = getActiveCatalog().find(a => a.id === avatarId);
    if (!found) {
      res.writeHead(404, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: false, error: "Avatar not found" }));
    }
    const host = req.headers.host || ("localhost:" + PORT);
    const protocol = req.headers["x-forwarded-proto"] || "http";
    const baseUrl = `${protocol}://${host}`;
    const img = found.uploadThingUrl || found.imageUrl || (found.assetPath ? `${baseUrl}/assets/avatars/${found.assetPath}` : `${baseUrl}${found.localUrl}`);
    res.writeHead(200, { "Content-Type": "application/json" });
    return res.end(JSON.stringify({
      success: true,
      avatar: {
        id: found.id,
        name: found.name,
        category: found.category,
        tags: found.tags || [],
        imageUrl: img,
        thumbnailUrl: img
      }
    }));
  }

  // 3. User Profile API
  if (pathname === "/api/profile/me" && req.method === "GET") {
    const code = (parsedUrl.query.code || parsedUrl.query.pairCode || "").toUpperCase().trim();
    if (!code) {
      res.writeHead(400, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: false, error: "Missing pairCode" }));
    }
    const prof = await dbGetProfile(code);
    res.writeHead(200, { "Content-Type": "application/json" });
    return res.end(JSON.stringify({ success: true, profile: prof }));
  }

  // 4. Update Profile Avatar
  if (pathname === "/api/profile/me/avatar" && (req.method === "PATCH" || req.method === "POST")) {
    const body = await readJsonBody(req);
    const code = (body.pairCode || body.code || parsedUrl.query.code || "").toUpperCase().trim();
    const avatarId = (body.avatarId || "").trim();
    if (!code || !avatarId) {
      res.writeHead(400, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: false, error: "Missing pairCode or avatarId" }));
    }
    const exists = getActiveCatalog().some(a => a.id === avatarId);
    if (!exists) {
      res.writeHead(400, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: false, error: "Invalid avatarId" }));
    }
    await dbUpdateAvatar(code, avatarId);
    const prof = await dbGetProfile(code);
    if (prof.partnerCode) {
      forwardToTarget(prof.partnerCode, {
        type: "partner_avatar_updated",
        partnerCode: code,
        partnerAvatarId: avatarId
      });
    }
    res.writeHead(200, { "Content-Type": "application/json" });
    return res.end(JSON.stringify({
      success: true,
      avatarId,
      displayName: prof.userName,
      message: "Avatar updated successfully"
    }));
  }

  // 4.5. Resolve Partner Code Profile API
  if (pathname === "/api/pairing/resolve-code" && req.method === "POST") {
    const body = await readJsonBody(req);
    const code = (body.code || body.partnerCode || "").toUpperCase().trim();
    if (!code) {
      res.writeHead(400, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: false, error: "Missing code" }));
    }
    const prof = await dbGetProfile(code);
    res.writeHead(200, { "Content-Type": "application/json" });
    return res.end(JSON.stringify({
      success: true,
      partner: {
        code: prof.pairCode,
        name: prof.userName,
        avatarId: prof.avatarId,
        avatarUrl: prof.avatarUrl || "",
        blueTick: !!prof.blueTick,
        greenTick: !!prof.greenTick,
        status: prof.partnerStatus,
        isPaired: !!prof.partnerCode
      }
    }));
  }

  // 5. Partner Pairing Request
  if (pathname === "/api/pairing/request" && req.method === "POST") {
    const body = await readJsonBody(req);
    const myCode = (body.myCode || "").toUpperCase().trim();
    const myName = (body.myName || "You").trim();
    const myAvatarId = (body.myAvatarId || "boy_001").trim();
    const myAvatarUrl = (body.myAvatarUrl || "").trim();
    const partnerCode = (body.partnerCode || "").toUpperCase().trim();

    if (!myCode || !partnerCode) {
      res.writeHead(400, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: false, error: "Both myCode and partnerCode are required" }));
    }
    if (myCode === partnerCode) {
      res.writeHead(400, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: false, error: "Cannot pair with yourself" }));
    }

    const reqData = {
      fromCode: myCode,
      fromName: myName,
      fromAvatarId: myAvatarId,
      fromAvatarUrl: myAvatarUrl,
      createdAt: Date.now()
    };

    pendingPairingRequests.set(partnerCode, reqData);

    if (pool && isDbConnected) {
      try {
        const reqId = `req_${myCode}_${partnerCode}`;
        await pool.query(`
          INSERT INTO pairing_requests (id, from_code, to_code, from_name, from_avatar_id, from_avatar_url, status, created_at)
          VALUES ($1, $2, $3, $4, $5, $6, 'PENDING', $7)
          ON CONFLICT (id) DO UPDATE
          SET from_name = EXCLUDED.from_name,
              from_avatar_id = EXCLUDED.from_avatar_id,
              from_avatar_url = EXCLUDED.from_avatar_url,
              status = 'PENDING',
              created_at = EXCLUDED.created_at;
        `, [reqId, myCode, partnerCode, myName, myAvatarId, myAvatarUrl, Date.now()]);
      } catch (e) {
        console.warn("DB pairing request insert warning:", e.message);
      }
    }

    forwardToTarget(partnerCode, {
      type: "pairing_request_received",
      fromCode: myCode,
      fromName: myName,
      fromAvatarId: myAvatarId,
      fromAvatarUrl: myAvatarUrl
    });

    res.writeHead(200, { "Content-Type": "application/json" });
    return res.end(JSON.stringify({
      success: true,
      message: `Pairing request sent to #${partnerCode}`
    }));
  }

  // 6. Partner Pairing Respond
  if (pathname === "/api/pairing/respond" && req.method === "POST") {
    const body = await readJsonBody(req);
    const myCode = (body.myCode || "").toUpperCase().trim();
    const partnerCode = (body.partnerCode || "").toUpperCase().trim();
    const action = (body.action || "").toUpperCase().trim();

    if (!myCode || !partnerCode || !action) {
      res.writeHead(400, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: false, error: "Missing parameters" }));
    }

    pendingPairingRequests.delete(myCode);

    if (pool && isDbConnected) {
      try {
        const reqId = `req_${partnerCode}_${myCode}`;
        await pool.query(`
          UPDATE pairing_requests SET status = $1, updated_at = NOW() WHERE id = $2;
        `, [action === "ACCEPT" ? "ACCEPTED" : "DECLINED", reqId]);
      } catch (e) {}
    }

    if (action === "DECLINE") {
      forwardToTarget(partnerCode, {
        type: "pairing_request_declined",
        fromCode: myCode
      });
      res.writeHead(200, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: true, status: "DECLINED" }));
    }

    if (action === "ACCEPT") {
      const myProfile = await dbGetProfile(myCode);
      const partnerProfile = await dbGetProfile(partnerCode);

      await dbSaveCouple(myCode, partnerCode, myProfile.userName, myProfile.avatarId, myProfile.avatarUrl);
      await dbSaveCouple(partnerCode, myCode, partnerProfile.userName, partnerProfile.avatarId, partnerProfile.avatarUrl);

      const mutualPayload = {
        type: "pairing_success",
        partnerCode: myCode,
        partnerName: myProfile.userName,
        partnerAvatarId: myProfile.avatarId,
        partnerAvatarUrl: myProfile.avatarUrl || ""
      };
      forwardToTarget(partnerCode, mutualPayload);

      res.writeHead(200, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({
        success: true,
        status: "ACCEPTED",
        partnerCode,
        partnerName: partnerProfile.userName,
        partnerAvatarId: partnerProfile.avatarId,
        partnerAvatarUrl: partnerProfile.avatarUrl || ""
      }));
    }
  }

  // 7. Partner Pairing Status & Unlink
  if (pathname === "/api/pairing/status" && req.method === "GET") {
    const code = (parsedUrl.query.code || "").toUpperCase().trim();
    let pending = pendingPairingRequests.get(code) || null;

    if (!pending && pool && isDbConnected) {
      try {
        const pr = await pool.query(`
          SELECT from_code AS "fromCode", from_name AS "fromName", from_avatar_id AS "fromAvatarId",
                 from_avatar_url AS "fromAvatarUrl", created_at AS "createdAt"
          FROM pairing_requests WHERE to_code = $1 AND status = 'PENDING'
          ORDER BY created_at DESC LIMIT 1;
        `, [code]);
        if (pr.rows.length > 0) pending = pr.rows[0];
      } catch (e) {}
    }

    const prof = await dbGetProfile(code);
    let partnerInfo = null;
    if (prof.partnerCode) {
      const pProf = await dbGetProfile(prof.partnerCode);
      partnerInfo = {
        code: pProf.pairCode,
        name: pProf.userName,
        avatarId: pProf.avatarId,
        avatarUrl: pProf.avatarUrl || "",
        blueTick: !!pProf.blueTick,
        greenTick: !!pProf.greenTick
      };
    }

    res.writeHead(200, { "Content-Type": "application/json" });
    return res.end(JSON.stringify({
      success: true,
      isPaired: !!(prof && prof.partnerCode),
      partnerCode: prof?.partnerCode || "",
      partner: partnerInfo,
      pendingRequest: pending || null
    }));
  }

  if (pathname === "/api/pairing/unlink" && req.method === "POST") {
    const body = await readJsonBody(req);
    const myCode = (body.myCode || "").toUpperCase().trim();
    const prof = await dbGetProfile(myCode);
    const partnerCode = prof.partnerCode;

    await dbSaveCouple(myCode, "", prof.userName, prof.avatarId, prof.avatarUrl);
    if (partnerCode) {
      const pProf = await dbGetProfile(partnerCode);
      await dbSaveCouple(partnerCode, "", pProf.userName, pProf.avatarId, pProf.avatarUrl);
      forwardToTarget(partnerCode, { type: "partner_unlinked", fromCode: myCode });
    }

    res.writeHead(200, { "Content-Type": "application/json" });
    return res.end(JSON.stringify({ success: true, message: "Unlinked successfully" }));
  }

  if (pathname === "/health" || pathname === "/api/v1/health") {
    res.writeHead(200, { "Content-Type": "application/json" });
    return res.end(JSON.stringify({
      status: "ok",
      activePeers: memoryClients.size,
      database: isDbConnected ? "PostgreSQL Connected" : "In-Memory Fallback Active",
      version: "3.0.0",
      avatarsAvailable: getActiveCatalog().length,
      time: new Date().toISOString()
    }));
  }

  if (pathname === "/ready" || pathname === "/api/v1/ready") {
    res.writeHead(200, { "Content-Type": "application/json" });
    return res.end(JSON.stringify({
      status: "ready",
      ready: true,
      activePeers: memoryClients.size,
      database: isDbConnected ? "PostgreSQL" : "In-Memory Fallback",
      time: new Date().toISOString()
    }));
  }

  if (pathname === "/api/db-status") {
    res.writeHead(200, { "Content-Type": "application/json" });
    return res.end(JSON.stringify({
      isPostgresConnected: isDbConnected,
      engine: isDbConnected ? "PostgreSQL" : "In-Memory",
      databaseUrlConfigured: !!DATABASE_URL,
      tables: ["couples", "call_logs", "shared_notes", "chat_messages"]
    }));
  }

  if (pathname === "/api/chat" || pathname === "/api/v1/conversations/messages") {
    const myCode = (parsedUrl.query.myCode || "").toUpperCase().trim();
    const partnerCode = (parsedUrl.query.partnerCode || "").toUpperCase().trim();
    const messages = await dbGetChatHistory(myCode, partnerCode);
    res.writeHead(200, { "Content-Type": "application/json" });
    return res.end(JSON.stringify({ success: true, count: messages.length, messages }));
  }

  if (pathname === "/api/note") {
    const myCode = (parsedUrl.query.myCode || "").toUpperCase().trim();
    const partnerCode = (parsedUrl.query.partnerCode || "").toUpperCase().trim();
    const note = await dbGetActiveNote(myCode, partnerCode);
    res.writeHead(200, { "Content-Type": "application/json" });
    return res.end(JSON.stringify({ success: true, note }));
  }

  if (pathname === "/api/v1/pairing/verify" || pathname === "/api/v1/pairing/current") {
    const code = (parsedUrl.query.code || parsedUrl.query.pairCode || "").toUpperCase().trim();
    const isOnline = memoryClients.has(code);
    res.writeHead(200, { "Content-Type": "application/json" });
    return res.end(JSON.stringify({
      success: true,
      code,
      exists: true,
      isOnline,
      message: "Pairing code verified successfully"
    }));
  }

  if (pathname === "/api/v1/calls") {
    res.writeHead(200, { "Content-Type": "application/json" });
    return res.end(JSON.stringify({
      success: true,
      status: "idle",
      recentLogs: memoryCallLogs.slice(0, 10)
    }));
  }

  res.writeHead(200, { "Content-Type": "text/html" });
  res.end(`
    <!DOCTYPE html>
    <html>
      <head>
        <title>Duora Server & PostgreSQL</title>
        <style>
          body { font-family: system-ui, sans-serif; background: #0E0712; color: #FFF1F2; display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 90vh; margin: 0; }
          .card { background: #261023; border: 1px solid #4A1E43; padding: 2.2rem; border-radius: 24px; text-align: center; max-width: 440px; box-shadow: 0 12px 36px rgba(0,0,0,0.5); }
          h1 { color: #F43F5E; margin-bottom: 0.5rem; font-size: 1.8rem; }
          .badge { background: ${isDbConnected ? "#10B981" : "#F59E0B"}; color: #fff; padding: 4px 12px; border-radius: 20px; font-size: 0.85rem; display: inline-block; margin-bottom: 1rem; }
          p { color: #D4A3B8; font-size: 0.95rem; line-height: 1.5; }
          .stat { color: #FBBF24; font-weight: bold; font-size: 1.2rem; margin: 0.8rem 0; }
          .db-tag { background: #3B1238; border: 1px solid #F43F5E; padding: 6px 14px; border-radius: 12px; font-size: 0.85rem; color: #FEE2E2; margin: 10px 0; }
          .footer { margin-top: 1.5rem; font-size: 0.8rem; color: #888; }
        </style>
      </head>
      <body>
        <div class="card">
          <h1>Duora Server & Database ❤️</h1>
          <div class="badge">● ${isDbConnected ? "PostgreSQL Active" : "Online (In-Memory Fallback)"}</div>
          <p>Dedicated real-time signaling, calling, chat, and 24h couple shared notes.</p>
          <div class="db-tag">Database: ${isDbConnected ? "PostgreSQL (4 tables)" : "In-Memory Buffer"}</div>
          <div class="stat">Connected Devices: ${memoryClients.size}</div>
          <p>WebSocket: <code>ws://${req.headers.host || "localhost:" + PORT}</code></p>
          <div class="footer">Duora v2.0.1 • Developer: Sukhdev Singh (@sukhdevr898)</div>
        </div>
      </body>
    </html>
  `);
});

// ==========================================
// 3. WebSocket Real-Time Signaling & Chat
// ==========================================
const wss = new WebSocketServer({ server });

function forwardToTarget(targetCode, messageObj) {
  if (!targetCode) return false;
  const targetEntry = memoryClients.get(targetCode.toUpperCase().trim());
  if (!targetEntry || !targetEntry.sockets || targetEntry.sockets.size === 0) {
    return false;
  }
  const payload = JSON.stringify(messageObj);
  let sentCount = 0;
  for (const socket of targetEntry.sockets) {
    if (socket.readyState === WebSocket.OPEN) {
      try {
        socket.send(payload);
        sentCount++;
      } catch (err) {
        console.error(`Failed to send to socket of #${targetCode}:`, err.message);
      }
    }
  }
  return sentCount > 0;
}

wss.on("connection", (ws, req) => {
  let registeredCode = null;

  ws.on("message", async (rawMessage) => {
    try {
      const data = JSON.parse(rawMessage.toString());

      switch (data.type) {
        case "register": {
          const code = (data.pairCode || "").toUpperCase().trim();
          const partnerCode = (data.partnerPairCode || "").toUpperCase().trim();
          const userName = data.userName || "You";

          const avatarId = (data.avatarId || "boy_001").trim();

          if (!code) return;

          // Check if user is blocked
          const clientProf = await dbGetProfile(code);
          if (clientProf && clientProf.isBlocked) {
            ws.send(JSON.stringify({
              type: "error",
              code: "ACCOUNT_BLOCKED",
              message: "Your Duora account has been suspended by an administrator."
            }));
            try { ws.close(4003, "Account suspended"); } catch (_) {}
            return;
          }

          registeredCode = code;
          let entry = memoryClients.get(code);
          if (!entry) {
            entry = { sockets: new Set(), partnerCode, connectedAt: Date.now() };
            memoryClients.set(code, entry);
          }
          entry.sockets.add(ws);
          entry.avatarId = avatarId;
          entry.userName = userName;
          if (partnerCode) {
            entry.partnerCode = partnerCode;
          }

          console.log(`[+] Device connected: #${code}. Target: #${partnerCode}, Avatar: ${avatarId}`);

          // Persist to Postgres
          dbSaveCouple(code, partnerCode, userName, avatarId);

          // Confirmation
          ws.send(JSON.stringify({
            type: "registered",
            pairCode: code,
            avatarId,
            avatarUrl: clientProf ? clientProf.avatarUrl : null,
            blueTick: clientProf ? !!clientProf.blueTick : false,
            greenTick: clientProf ? !!clientProf.greenTick : false,
            dbConnected: isDbConnected
          }));

          // Partner Online Check & Profile
          const partnerEntry = memoryClients.get(partnerCode);
          const isPartnerOnline = !!(partnerEntry && partnerEntry.sockets && partnerEntry.sockets.size > 0);
          const partnerProfile = await dbGetProfile(partnerCode);

          ws.send(JSON.stringify({
            type: "partner_status",
            isOnline: isPartnerOnline,
            partnerCode,
            partnerName: partnerProfile.userName,
            partnerAvatarId: partnerProfile.avatarId,
            partnerAvatarUrl: partnerProfile.avatarUrl,
            blueTick: !!partnerProfile.blueTick,
            greenTick: !!partnerProfile.greenTick
          }));

          if (isPartnerOnline) {
            forwardToTarget(partnerCode, {
              type: "partner_status",
              isOnline: true,
              partnerCode: code,
              partnerName: userName,
              partnerAvatarId: avatarId,
              partnerAvatarUrl: clientProf ? clientProf.avatarUrl : null,
              blueTick: clientProf ? !!clientProf.blueTick : false,
              greenTick: clientProf ? !!clientProf.greenTick : false
            });
          }

          // Automatically send latest active 24h shared note from Postgres if present
          if (partnerCode) {
            const activeNote = await dbGetActiveNote(code, partnerCode);
            if (activeNote) {
              ws.send(JSON.stringify({
                type: "couple_note",
                from: activeNote.author_code || activeNote.authorCode,
                noteId: activeNote.id,
                content: activeNote.content,
                authorName: activeNote.author_name || activeNote.authorName,
                createdAt: Number(activeNote.created_at || activeNote.createdAt)
              }));
            }
          }
          break;
        }

        case "check_partner": {
          const target = (data.partnerCode || "").toUpperCase().trim();
          const targetEntry = memoryClients.get(target);
          const isOnline = !!(targetEntry && targetEntry.sockets && targetEntry.sockets.size > 0);
          const targetProfile = await dbGetProfile(target);
          ws.send(JSON.stringify({
            type: "partner_status",
            isOnline,
            partnerCode: target,
            partnerName: targetProfile.userName,
            partnerAvatarId: targetProfile.avatarId
          }));
          break;
        }

        case "update_avatar": {
          const avatarId = (data.avatarId || "").trim();
          if (registeredCode && avatarId) {
            await dbUpdateAvatar(registeredCode, avatarId);
            const prof = await dbGetProfile(registeredCode);
            if (prof.partnerCode) {
              forwardToTarget(prof.partnerCode, {
                type: "partner_avatar_updated",
                partnerCode: registeredCode,
                partnerAvatarId: avatarId
              });
            }
          }
          break;
        }

        // --- REAL-TIME CHAT ---
        case "chat_message": {
          const from = (data.from || "").toUpperCase().trim();
          const to = (data.to || "").toUpperCase().trim();
          const id = data.id || `msg_${Date.now()}`;
          const content = data.content || "";
          const senderName = data.senderName || "My Love";
          const messageType = data.messageType || "text";
          const timestamp = data.timestamp || Date.now();

          // 1. Save to PostgreSQL
          await dbSaveChatMessage(id, from, to, senderName, content, messageType, timestamp);

          // 2. Deliver in real-time to partner
          const delivered = forwardToTarget(to, {
            type: "chat_message",
            id,
            from,
            to,
            senderName,
            content,
            messageType,
            timestamp
          });

          // 3. Acknowledge delivery to sender
          ws.send(JSON.stringify({
            type: "chat_delivered",
            id,
            deliveredToPeer: delivered
          }));
          break;
        }

        case "chat_typing": {
          const to = (data.to || "").toUpperCase().trim();
          forwardToTarget(to, {
            type: "chat_typing",
            from: data.from,
            isTyping: !!data.isTyping
          });
          break;
        }

        case "chat_read": {
          const from = (data.from || "").toUpperCase().trim();
          const to = (data.to || "").toUpperCase().trim();
          await dbMarkChatRead(from, to);
          forwardToTarget(to, {
            type: "chat_read",
            from
          });
          break;
        }

        case "chat_history_request": {
          const from = (data.from || "").toUpperCase().trim();
          const to = (data.to || "").toUpperCase().trim();
          const messages = await dbGetChatHistory(from, to, 100);
          ws.send(JSON.stringify({
            type: "chat_history",
            messages
          }));
          break;
        }

        // --- 24-HOUR COUPLE NOTES ---
        case "couple_note": {
          const target = (data.to || "").toUpperCase().trim();
          const noteId = data.noteId || `note_${Date.now()}`;
          const createdAt = data.createdAt || Date.now();

          // 1. Save to PostgreSQL
          await dbSaveSharedNote(noteId, data.from, target, data.authorName, data.content, createdAt);

          // 2. Relay in real-time to partner
          forwardToTarget(target, {
            type: "couple_note",
            from: data.from,
            noteId,
            content: data.content,
            authorName: data.authorName,
            createdAt
          });
          break;
        }

        case "delete_note": {
          const target = (data.to || "").toUpperCase().trim();
          await dbDeleteSharedNote(data.noteId);
          forwardToTarget(target, {
            type: "delete_note",
            from: data.from,
            noteId: data.noteId
          });
          break;
        }

        // --- CALLING SIGNALING ---
        case "call_initiate": {
          const target = (data.to || "").toUpperCase().trim();
          const from = (data.from || "").toUpperCase().trim();
          const callId = data.callId || `call_${Date.now()}_${Math.random().toString(36).substring(2, 8)}`;
          console.log(`[CALL] ${from} calling ${target} (${data.callType || "VOICE"}) [id: ${callId}]`);

          const session = {
            callId,
            callerCode: from,
            recipientCode: target,
            callType: data.callType || "VOICE",
            status: "RINGING",
            startedAt: Date.now(),
            connectedAt: null,
            lastCallerHeartbeat: Date.now(),
            lastRecipientHeartbeat: Date.now(),
            graceTimer: null,
            isCleanedUp: false
          };
          activeCallSessions.set(callId, session);

          const delivered = forwardToTarget(target, {
            type: "call_incoming",
            callId,
            from,
            callerName: data.callerName || "My Love",
            callType: data.callType || "VOICE"
          });

          if (delivered) {
            ws.send(JSON.stringify({
              type: "call_ringing",
              callId,
              from: target
            }));
          } else {
            endCallSession(session, "UNAVAILABLE", "Partner is currently offline or unreachable.");
            ws.send(JSON.stringify({
              type: "call_unavailable",
              callId,
              reason: "Partner is currently offline or unreachable."
            }));
          }
          break;
        }

        case "call_ringing": {
          const target = (data.to || "").toUpperCase().trim();
          forwardToTarget(target, {
            type: "call_ringing",
            callId: data.callId,
            from: data.from
          });
          break;
        }

        case "call_accept": {
          const target = (data.to || "").toUpperCase().trim();
          const from = (data.from || "").toUpperCase().trim();
          let session = activeCallSessions.get(data.callId);
          if (!session) {
            for (const s of activeCallSessions.values()) {
              if ((s.callerCode === target && s.recipientCode === from) || (s.callerCode === from && s.recipientCode === target)) {
                session = s;
                break;
              }
            }
          }

          if (session) {
            session.status = "CONNECTED";
            session.connectedAt = Date.now();
            session.lastCallerHeartbeat = Date.now();
            session.lastRecipientHeartbeat = Date.now();
          }

          forwardToTarget(target, {
            type: "call_accepted",
            callId: session ? session.callId : data.callId,
            from: data.from
          });
          break;
        }

        case "call_heartbeat": {
          const callId = data.callId;
          const from = (data.from || "").toUpperCase().trim();
          let session = activeCallSessions.get(callId);
          if (!session) {
            for (const s of activeCallSessions.values()) {
              if (s.callerCode === from || s.recipientCode === from) {
                session = s;
                break;
              }
            }
          }

          if (session && !session.isCleanedUp) {
            const now = Date.now();
            if (from === session.callerCode) {
              session.lastCallerHeartbeat = now;
            } else if (from === session.recipientCode) {
              session.lastRecipientHeartbeat = now;
            }

            if (session.status === "RECONNECTING") {
              if (session.graceTimer) {
                clearTimeout(session.graceTimer);
                session.graceTimer = null;
              }
              session.status = "CONNECTED";
              const partner = (from === session.callerCode) ? session.recipientCode : session.callerCode;
              forwardToTarget(partner, {
                type: "call_reconnected",
                callId: session.callId
              });
              ws.send(JSON.stringify({
                type: "call_reconnected",
                callId: session.callId
              }));
              console.log(`[CALL-SESSION] Call ${session.callId} reconnected successfully!`);
            }
          }
          break;
        }

        case "call_decline": {
          const target = (data.to || "").toUpperCase().trim();
          const from = (data.from || "").toUpperCase().trim();
          let session = activeCallSessions.get(data.callId);
          if (!session) {
            for (const s of activeCallSessions.values()) {
              if ((s.callerCode === target && s.recipientCode === from) || (s.callerCode === from && s.recipientCode === target)) {
                session = s;
                break;
              }
            }
          }
          if (session) {
            endCallSession(session, "DECLINED", "Call declined by partner");
          } else {
            forwardToTarget(target, {
              type: "call_declined",
              callId: data.callId,
              from: data.from
            });
          }
          break;
        }

        case "call_end": {
          const target = (data.to || "").toUpperCase().trim();
          const from = (data.from || "").toUpperCase().trim();
          let session = activeCallSessions.get(data.callId);
          if (!session) {
            for (const s of activeCallSessions.values()) {
              if ((s.callerCode === target && s.recipientCode === from) || (s.callerCode === from && s.recipientCode === target)) {
                session = s;
                break;
              }
            }
          }
          if (session) {
            endCallSession(session, "HANG_UP", "Call ended");
          } else {
            forwardToTarget(target, {
              type: "call_ended",
              callId: data.callId,
              from: data.from
            });
          }
          break;
        }

        case "audio_chunk": {
          const target = (data.to || "").toUpperCase().trim();
          forwardToTarget(target, {
            type: "audio_chunk",
            from: data.from,
            audioData: data.audioData
          });
          break;
        }

        case "video_frame": {
          const target = (data.to || "").toUpperCase().trim();
          forwardToTarget(target, {
            type: "video_frame",
            from: data.from,
            frameData: data.frameData
          });
          break;
        }

        case "heart_reaction": {
          const target = (data.to || "").toUpperCase().trim();
          forwardToTarget(target, {
            type: "heart_reaction",
            from: data.from
          });
          break;
        }

        case "ping": {
          ws.send(JSON.stringify({ type: "pong" }));
          break;
        }

        default:
          break;
      }
    } catch (err) {
      console.error("Failed to parse incoming message:", err.message);
    }
  });

  ws.on("close", () => {
    if (registeredCode) {
      const entry = memoryClients.get(registeredCode);
      if (entry) {
        entry.sockets.delete(ws);
        console.log(`[-] Socket closed for #${registeredCode}. Remaining: ${entry.sockets.size}`);

        if (entry.sockets.size === 0) {
          const partnerCode = entry.partnerCode;
          memoryClients.delete(registeredCode);

          // Check if this user was in an active call session
          for (const session of activeCallSessions.values()) {
            if ((session.callerCode === registeredCode || session.recipientCode === registeredCode) && !session.isCleanedUp) {
              const otherParty = (session.callerCode === registeredCode) ? session.recipientCode : session.callerCode;
              if (session.status === "CONNECTED") {
                session.status = "RECONNECTING";
                console.log(`[CALL-SESSION] User #${registeredCode} disconnected during call ${session.callId}. Starting 15s grace period.`);
                forwardToTarget(otherParty, {
                  type: "call_reconnecting",
                  callId: session.callId,
                  from: registeredCode,
                  gracePeriodSeconds: 15
                });

                if (!session.graceTimer) {
                  session.graceTimer = setTimeout(() => {
                    endCallSession(session, "DISCONNECTED_TIMEOUT", "Connection lost with partner");
                  }, 15000);
                }
              } else if (session.status === "RINGING") {
                endCallSession(session, "MISSED", "Partner disconnected");
              }
            }
          }

          forwardToTarget(partnerCode, {
            type: "partner_status",
            isOnline: false,
            partnerCode: registeredCode
          });
        }
      }
    }
  });

  ws.on("error", (err) => {
    console.error(`WebSocket error for ${registeredCode}:`, err.message);
  });
});

// Periodic call session lease reaper (every 5 seconds)
setInterval(() => {
  const now = Date.now();
  for (const session of activeCallSessions.values()) {
    if (session.isCleanedUp) continue;

    if (session.status === "CONNECTED") {
      const callerGap = now - (session.lastCallerHeartbeat || session.connectedAt || now);
      const recipientGap = now - (session.lastRecipientHeartbeat || session.connectedAt || now);

      if (callerGap > 12000 || recipientGap > 12000) {
        session.status = "RECONNECTING";
        const deadParty = (callerGap > 12000) ? session.callerCode : session.recipientCode;
        const liveParty = (deadParty === session.callerCode) ? session.recipientCode : session.callerCode;

        console.log(`[CALL-REAPER] Missing heartbeats on call ${session.callId}. Entering RECONNECTING.`);
        forwardToTarget(liveParty, {
          type: "call_reconnecting",
          callId: session.callId,
          from: deadParty,
          gracePeriodSeconds: 15
        });

        if (!session.graceTimer) {
          session.graceTimer = setTimeout(() => {
            endCallSession(session, "HEARTBEAT_TIMEOUT", "Connection lost with partner");
          }, 15000);
        }
      }
    } else if (session.status === "RINGING") {
      if (now - session.startedAt > 45000) {
        endCallSession(session, "MISSED", "Call not answered");
      }
    }
  }
}, 5000);

setInterval(() => {
  const pingPayload = JSON.stringify({ type: "ping" });
  for (const entry of memoryClients.values()) {
    if (entry.sockets) {
      for (const socket of entry.sockets) {
        if (socket.readyState === WebSocket.OPEN) {
          try {
            socket.send(pingPayload);
          } catch (_) {}
        }
      }
    }
  }
}, 25000);

server.listen(PORT, "0.0.0.0", () => {
  console.log(`🚀 Duora Signaling & PostgreSQL Server listening on port ${PORT}`);
  console.log(`❤️  Ready for calls, real-time chat, and couple notes!`);
});
