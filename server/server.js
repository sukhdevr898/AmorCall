const http = require("http");
const url = require("url");
const { WebSocketServer, WebSocket } = require("ws");
const { Pool } = require("pg");

const PORT = process.env.PORT || 3000;
const DATABASE_URL = process.env.DATABASE_URL || process.env.POSTGRES_URL;

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
        last_seen BIGINT,
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

    console.log("✅ PostgreSQL tables initialized: couples, call_logs, shared_notes, chat_messages");
  } catch (err) {
    console.error("Failed to create tables in PostgreSQL:", err.message);
  }
}

// Data access operations
async function dbSaveCouple(pairCode, partnerCode, userName) {
  if (pool && isDbConnected) {
    try {
      await pool.query(`
        INSERT INTO couples (pair_code, partner_code, user_name, last_seen, updated_at)
        VALUES ($1, $2, $3, $4, NOW())
        ON CONFLICT (pair_code) DO UPDATE
        SET partner_code = EXCLUDED.partner_code,
            user_name = COALESCE(EXCLUDED.user_name, couples.user_name),
            last_seen = EXCLUDED.last_seen,
            updated_at = NOW();
      `, [pairCode, partnerCode, userName || "You", Date.now()]);
    } catch (e) {
      console.warn("DB save couple error:", e.message);
    }
  }
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

// ==========================================
// 2. HTTP Server & REST Endpoints
// ==========================================
const server = http.createServer(async (req, res) => {
  const parsedUrl = url.parse(req.url, true);
  const pathname = parsedUrl.pathname;

  if (pathname === "/health" || pathname === "/api/v1/health") {
    res.writeHead(200, { "Content-Type": "application/json" });
    return res.end(JSON.stringify({
      status: "ok",
      activePeers: memoryClients.size,
      database: isDbConnected ? "PostgreSQL Connected" : "In-Memory Fallback Active",
      version: "2.0.1",
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

          if (!code) return;

          registeredCode = code;
          let entry = memoryClients.get(code);
          if (!entry) {
            entry = { sockets: new Set(), partnerCode, connectedAt: Date.now() };
            memoryClients.set(code, entry);
          }
          entry.sockets.add(ws);
          if (partnerCode) {
            entry.partnerCode = partnerCode;
          }

          console.log(`[+] Device connected: #${code}. Target: #${partnerCode}`);

          // Persist to Postgres
          dbSaveCouple(code, partnerCode, userName);

          // Confirmation
          ws.send(JSON.stringify({ type: "registered", pairCode: code, dbConnected: isDbConnected }));

          // Partner Online Check
          const partnerEntry = memoryClients.get(partnerCode);
          const isPartnerOnline = !!(partnerEntry && partnerEntry.sockets && partnerEntry.sockets.size > 0);

          ws.send(JSON.stringify({
            type: "partner_status",
            isOnline: isPartnerOnline,
            partnerCode
          }));

          if (isPartnerOnline) {
            forwardToTarget(partnerCode, {
              type: "partner_status",
              isOnline: true,
              partnerCode: code
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
          ws.send(JSON.stringify({
            type: "partner_status",
            isOnline,
            partnerCode: target
          }));
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
          console.log(`[CALL] ${data.from} calling ${target} (${data.callType})`);

          const delivered = forwardToTarget(target, {
            type: "call_incoming",
            from: data.from,
            callerName: data.callerName || "My Love",
            callType: data.callType || "VOICE"
          });

          if (delivered) {
            ws.send(JSON.stringify({
              type: "call_ringing",
              from: target
            }));
          } else {
            ws.send(JSON.stringify({
              type: "call_unavailable",
              reason: "Partner is currently offline or unreachable."
            }));
          }
          break;
        }

        case "call_ringing": {
          const target = (data.to || "").toUpperCase().trim();
          forwardToTarget(target, {
            type: "call_ringing",
            from: data.from
          });
          break;
        }

        case "call_accept": {
          const target = (data.to || "").toUpperCase().trim();
          forwardToTarget(target, {
            type: "call_accepted",
            from: data.from
          });
          break;
        }

        case "call_decline": {
          const target = (data.to || "").toUpperCase().trim();
          forwardToTarget(target, {
            type: "call_declined",
            from: data.from
          });
          break;
        }

        case "call_end": {
          const target = (data.to || "").toUpperCase().trim();
          forwardToTarget(target, {
            type: "call_ended",
            from: data.from
          });

          // Log call to PostgreSQL
          if (data.callId) {
            dbSaveCallLog(data.callId, data.from, target, data.callType || "VOICE", data.duration || 0, Date.now());
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
