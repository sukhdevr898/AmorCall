// Duora Admin Web App, User Management, QR Pairing & UploadThing Service
const fs = require("fs");
const path = require("path");
const https = require("https");

// Safely load environment variables from uncommitted local .env if present
try {
  const envPath = path.resolve(__dirname, "..", ".env");
  if (fs.existsSync(envPath)) {
    const envContent = fs.readFileSync(envPath, "utf8");
    for (const line of envContent.split("\n")) {
      const trimmed = line.trim();
      if (!trimmed || trimmed.startsWith("#")) continue;
      const eqIdx = trimmed.indexOf("=");
      if (eqIdx > 0) {
        const k = trimmed.substring(0, eqIdx).trim();
        const v = trimmed.substring(eqIdx + 1).trim().replace(/^['"]|['"]$/g, "");
        if (!process.env[k]) {
          process.env[k] = v;
        }
      }
    }
  }
} catch (_) {}

const ADMIN_PASSWORD = process.env.ADMIN_PASSWORD || "duora2026";
const UPLOADTHING_SECRET = process.env.UPLOADTHING_SECRET || "";
const UPLOADTHING_APP_ID = process.env.UPLOADTHING_APP_ID || "";
let UPLOADTHING_TOKEN = process.env.UPLOADTHING_TOKEN || "";

const ASSETS_ROOT = path.join(__dirname, "assets");
const AVATARS_ROOT = path.join(ASSETS_ROOT, "avatars");
const CATALOG_FILE = path.join(AVATARS_ROOT, "catalog.json");
const UPLOADS_ROOT = path.join(AVATARS_ROOT, "uploads");

fs.mkdirSync(AVATARS_ROOT, { recursive: true });
fs.mkdirSync(UPLOADS_ROOT, { recursive: true });

// In-Memory audit log
const auditLog = [
  { action: "SYSTEM_INIT", target: "Server and Admin Engine ready", timestamp: new Date().toISOString(), user: "system" }
];

function logAudit(action, target, user = "admin") {
  auditLog.unshift({ action, target, timestamp: new Date().toISOString(), user });
  if (auditLog.length > 200) auditLog.pop();
}

// Path safety check
function resolveSafePath(relativePath) {
  const normalized = path.normalize(relativePath || "").replace(/^(\.\.[\/\\])+/, "");
  const fullPath = path.resolve(ASSETS_ROOT, normalized);
  if (!fullPath.startsWith(ASSETS_ROOT)) {
    throw new Error("Access Denied: Path traversal detected");
  }
  return fullPath;
}

// Default initial catalogue
const DEFAULT_CATALOG = [
  { id: "boy_001", name: "Kai (Anime Classic)", category: "BOY", tags: ["Anime", "Stylish"], assetPath: "boys/boy_001.webp" },
  { id: "boy_002", name: "Ren (Cool Jacket)", category: "BOY", tags: ["Stylish", "Minimal"], assetPath: "boys/boy_002.webp" },
  { id: "boy_003", name: "Leo (Warm Smile)", category: "BOY", tags: ["Cute", "Anime"], assetPath: "boys/boy_003.webp" },
  { id: "boy_004", name: "Haru (Playful)", category: "BOY", tags: ["Cute", "Stylish"], assetPath: "boys/boy_004.webp" },
  { id: "boy_005", name: "Zane (Mystic)", category: "BOY", tags: ["Anime", "Minimal"], assetPath: "boys/boy_005.webp" },
  { id: "boy_006", name: "Tora (Casual Boy)", category: "BOY", tags: ["Minimal", "Stylish"], assetPath: "boys/boy_006.webp" },
  { id: "boy_007", name: "Kenji (Heart Eyes)", category: "BOY", tags: ["Cute", "Anime"], assetPath: "boys/boy_007.webp" },
  { id: "boy_008", name: "Ryu (Night City)", category: "BOY", tags: ["Stylish", "Anime"], assetPath: "boys/boy_008.webp" },
  { id: "boy_009", name: "Yuki (Anime Prince)", category: "BOY", tags: ["Anime", "Cute"], assetPath: "boys/boy_009.webp" },
  { id: "boy_010", name: "Sora (Sweetheart)", category: "BOY", tags: ["Cute", "Minimal"], assetPath: "boys/boy_010.webp" },
  { id: "girl_001", name: "Aria (Sweet Bow)", category: "GIRL", tags: ["Cute", "Anime"], assetPath: "girls/girl_001.webp" },
  { id: "girl_002", name: "Mei (Summer Hat)", category: "GIRL", tags: ["Stylish", "Minimal"], assetPath: "girls/girl_002.webp" },
  { id: "girl_003", name: "Yuna (Soft Glow)", category: "GIRL", tags: ["Cute", "Stylish"], assetPath: "girls/girl_003.webp" },
  { id: "girl_004", name: "Luna (Cat Ears)", category: "GIRL", tags: ["Cute", "Anime"], assetPath: "girls/girl_004.webp" },
  { id: "girl_005", name: "Hana (Pastel Bloom)", category: "GIRL", tags: ["Anime", "Minimal"], assetPath: "girls/girl_005.webp" },
  { id: "girl_006", name: "Chloe (Sunny Beret)", category: "GIRL", tags: ["Stylish", "Cute"], assetPath: "girls/girl_006.webp" },
  { id: "girl_007", name: "Kira (Stylish Ribbon)", category: "GIRL", tags: ["Stylish", "Anime"], assetPath: "girls/girl_007.webp" },
  { id: "girl_008", name: "Miku (Warm Knitted)", category: "GIRL", tags: ["Cute", "Minimal"], assetPath: "girls/girl_008.webp" },
  { id: "girl_009", name: "Emi (Heart Smile)", category: "GIRL", tags: ["Cute", "Anime"], assetPath: "girls/girl_009.webp" },
  { id: "girl_010", name: "Rin (Anime Idol)", category: "GIRL", tags: ["Anime", "Stylish"], assetPath: "girls/girl_010.webp" }
];

let liveCatalog = [...DEFAULT_CATALOG];
const SYNCED_CATALOG_FILE = path.join(__dirname, "avatar_catalog.json");

let utapiClient = null;
try {
  const { UTApi } = require("uploadthing/server");
  utapiClient = new UTApi({ token: UPLOADTHING_TOKEN });
} catch (e) {
  console.warn("[UploadThing] UTApi initialization note:", e.message);
}

function loadCatalogFromDisk() {
  try {
    if (fs.existsSync(SYNCED_CATALOG_FILE)) {
      const data = fs.readFileSync(SYNCED_CATALOG_FILE, "utf8");
      liveCatalog = JSON.parse(data);
    } else if (fs.existsSync(CATALOG_FILE)) {
      const data = fs.readFileSync(CATALOG_FILE, "utf8");
      liveCatalog = JSON.parse(data);
    } else {
      saveCatalogToDisk(DEFAULT_CATALOG);
    }
  } catch (e) {
    console.warn("[CATALOG] Fallback to default catalog:", e.message);
  }
}

function saveCatalogToDisk(catalog) {
  try {
    liveCatalog = catalog;
    fs.mkdirSync(AVATARS_ROOT, { recursive: true });
    fs.writeFileSync(CATALOG_FILE, JSON.stringify(catalog, null, 2), "utf8");
  } catch (e) {
    console.warn("[CATALOG] Save failure:", e.message);
  }
}

loadCatalogFromDisk();

function getCatalog() {
  return liveCatalog;
}

// -------------------------------------------------------------
// UploadThing integration for Avatar / Profile Picture Uploads
// -------------------------------------------------------------
async function uploadToUploadThing(fileBuffer, fileName, mimeType = "image/png") {
  const safeName = `dp_${Date.now()}_${path.basename(fileName).replace(/[^a-zA-Z0-9._-]/g, "")}`;
  
  if (utapiClient) {
    try {
      const fileObj = new File([fileBuffer], safeName, { type: mimeType });
      const uploadResults = await utapiClient.uploadFiles([fileObj]);
      if (uploadResults && uploadResults[0] && uploadResults[0].data) {
        const ufsUrl = uploadResults[0].data.ufsUrl || uploadResults[0].data.url;
        logAudit("UPLOADTHING_UPLOAD", `Uploaded ${safeName} (${uploadResults[0].data.key})`, "app");
        return {
          service: "uploadthing",
          url: ufsUrl,
          ufsUrl,
          key: uploadResults[0].data.key
        };
      }
    } catch (err) {
      console.warn("[UploadThing] Upload failed, falling back to local:", err.message);
    }
  }

  // Local fallback: save to persistent storage in assets/avatars/uploads
  const localPath = path.join(UPLOADS_ROOT, safeName);
  fs.writeFileSync(localPath, fileBuffer);
  logAudit("LOCAL_DP_SAVE", `Saved ${safeName} locally`, "app");
  return {
    service: "local-fallback",
    url: `/assets/avatars/uploads/${safeName}`,
    key: safeName
  };
}

let qrcodeLib = null;
try {
  qrcodeLib = require("qrcode");
} catch (_) {}

// Generate real, standard ISO-18004 SVG QR Code
async function generateQrSvg(code) {
  const cleanCode = (code || "DUORA").toUpperCase().trim();
  const payload = `DUORA:PAIR:${cleanCode}`;
  if (qrcodeLib) {
    try {
      const svg = await qrcodeLib.toString(payload, {
        type: "svg",
        errorCorrectionLevel: "M",
        margin: 2,
        color: {
          dark: "#21102E",
          light: "#FFFFFF"
        }
      });
      return svg;
    } catch (e) {
      console.warn("[QR] SVG generation fallback:", e.message);
    }
  }
  // Standard fallback SVG with embedded code text
  return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 250 250" width="250" height="250">
    <rect width="250" height="250" fill="#ffffff" rx="16"/>
    <rect x="25" y="25" width="50" height="50" fill="#21102E" rx="8"/>
    <rect x="35" y="35" width="30" height="30" fill="#ffffff" rx="4"/>
    <rect x="42" y="42" width="16" height="16" fill="#21102E" rx="2"/>
    <rect x="175" y="25" width="50" height="50" fill="#21102E" rx="8"/>
    <rect x="185" y="35" width="30" height="30" fill="#ffffff" rx="4"/>
    <rect x="192" y="42" width="16" height="16" fill="#21102E" rx="2"/>
    <rect x="25" y="175" width="50" height="50" fill="#21102E" rx="8"/>
    <rect x="35" y="185" width="30" height="30" fill="#ffffff" rx="4"/>
    <rect x="42" y="192" width="16" height="16" fill="#21102E" rx="2"/>
    <text x="125" y="130" font-family="monospace" font-size="20" font-weight="bold" fill="#FF2D8D" text-anchor="middle">#${cleanCode}</text>
  </svg>`;
}

// Responsive, Mobile-friendly Single-Page Admin Web App
function getAdminHtml(baseUrl) {
  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>Duora • Admin & User Manager</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@500;700&display=swap">
  <style>
    :root {
      --bg: #0E0713;
      --card: #190E22;
      --card-border: #2E1A3D;
      --text: #F8F5FB;
      --text-muted: #9E91AC;
      --hot-pink: #FF2D8D;
      --hot-pink-glow: rgba(255, 45, 141, 0.35);
      --purple: #8B5CF6;
      --blue-tick: #38BDF8;
      --green-tick: #22C55E;
      --red: #EF4444;
      --yellow: #F59E0B;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; -webkit-tap-highlight-color: transparent; }
    body {
      font-family: 'Plus Jakarta Sans', sans-serif;
      background: var(--bg);
      color: var(--text);
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      padding-bottom: 72px;
    }
    header {
      background: rgba(25, 14, 34, 0.9);
      backdrop-filter: blur(12px);
      border-bottom: 1px solid var(--card-border);
      padding: 12px 18px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      position: sticky;
      top: 0;
      z-index: 50;
    }
    .brand {
      display: flex;
      align-items: center;
      gap: 10px;
      font-weight: 800;
      font-size: 1.15rem;
    }
    .heart-logo {
      width: 32px;
      height: 32px;
      background: linear-gradient(135deg, var(--hot-pink), var(--purple));
      border-radius: 9px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 16px;
    }
    .desktop-nav { display: flex; gap: 6px; }
    @media (max-width: 768px) {
      .desktop-nav { display: none; }
    }
    .nav-btn {
      padding: 7px 14px;
      background: transparent;
      border: none;
      color: var(--text-muted);
      font-weight: 600;
      font-size: 0.85rem;
      border-radius: 10px;
      cursor: pointer;
      transition: all 0.2s;
    }
    .nav-btn.active {
      background: var(--hot-pink);
      color: white;
      box-shadow: 0 4px 12px var(--hot-pink-glow);
    }
    main {
      flex: 1;
      padding: 16px;
      max-width: 1200px;
      margin: 0 auto;
      width: 100%;
    }
    .stats-bar {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
      gap: 10px;
      margin-bottom: 18px;
    }
    .stat-pill {
      background: var(--card);
      border: 1px solid var(--card-border);
      border-radius: 14px;
      padding: 12px 14px;
    }
    .stat-label { font-size: 0.72rem; color: var(--text-muted); font-weight: 600; text-transform: uppercase; }
    .stat-num { font-size: 1.4rem; font-weight: 800; color: white; margin-top: 2px; }
    .card {
      background: var(--card);
      border: 1px solid var(--card-border);
      border-radius: 18px;
      padding: 18px;
      margin-bottom: 16px;
    }
    .card-title-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 14px;
      flex-wrap: wrap;
      gap: 8px;
    }
    .btn {
      padding: 8px 14px;
      border-radius: 10px;
      border: none;
      font-weight: 700;
      font-size: 0.82rem;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 6px;
      transition: transform 0.15s;
    }
    .btn:active { transform: scale(0.96); }
    .btn-pink { background: var(--hot-pink); color: white; }
    .btn-blue { background: rgba(56, 189, 248, 0.15); color: var(--blue-tick); border: 1px solid rgba(56,189,248,0.4); }
    .btn-green { background: rgba(34, 197, 94, 0.15); color: var(--green-tick); border: 1px solid rgba(34,197,94,0.4); }
    .btn-danger { background: rgba(239, 68, 68, 0.15); color: var(--red); border: 1px solid rgba(239,68,68,0.4); }
    .btn-outline { background: rgba(255,255,255,0.06); color: var(--text); border: 1px solid var(--card-border); }
    
    /* Users Grid / Card View (Mobile Friendly) */
    .user-list {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(310px, 1fr));
      gap: 12px;
    }
    .user-card {
      background: rgba(14, 7, 19, 0.65);
      border: 1px solid var(--card-border);
      border-radius: 16px;
      padding: 14px;
      display: flex;
      flex-direction: column;
      gap: 10px;
    }
    .user-card.blocked {
      border-color: rgba(239, 68, 68, 0.5);
      background: rgba(239, 68, 68, 0.05);
    }
    .user-top {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .user-avatar {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      object-fit: cover;
      background: #2A173B;
      border: 2px solid var(--card-border);
    }
    .user-info { flex: 1; overflow: hidden; }
    .user-name {
      font-size: 0.95rem;
      font-weight: 700;
      display: flex;
      align-items: center;
      gap: 6px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .tick-blue { color: var(--blue-tick); font-size: 0.85rem; }
    .tick-green { color: var(--green-tick); font-size: 0.85rem; }
    .user-code {
      font-family: 'JetBrains Mono', monospace;
      font-size: 0.75rem;
      color: var(--hot-pink);
      background: rgba(255, 45, 141, 0.12);
      padding: 2px 6px;
      border-radius: 5px;
      display: inline-block;
      margin-top: 2px;
    }
    .user-actions {
      display: flex;
      gap: 6px;
      flex-wrap: wrap;
    }
    .user-actions .btn { flex: 1; min-width: 80px; justify-content: center; }

    /* Mobile Bottom Navigation */
    .mobile-nav {
      display: none;
      position: fixed;
      bottom: 0;
      left: 0;
      right: 0;
      background: rgba(25, 14, 34, 0.95);
      backdrop-filter: blur(14px);
      border-top: 1px solid var(--card-border);
      padding: 6px 12px;
      z-index: 90;
      justify-content: space-around;
    }
    @media (max-width: 768px) {
      .mobile-nav { display: flex; }
    }
    .m-tab {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 2px;
      font-size: 0.7rem;
      font-weight: 600;
      color: var(--text-muted);
      background: transparent;
      border: none;
      padding: 6px 10px;
      border-radius: 8px;
      cursor: pointer;
    }
    .m-tab.active { color: var(--hot-pink); }
    .m-tab-icon { font-size: 1.15rem; }

    /* Modal */
    .modal-wrap {
      display: none;
      position: fixed;
      inset: 0;
      background: rgba(0,0,0,0.75);
      backdrop-filter: blur(8px);
      z-index: 100;
      align-items: center;
      justify-content: center;
      padding: 16px;
    }
    .modal-wrap.open { display: flex; }
    .modal-box {
      background: var(--card);
      border: 1px solid var(--card-border);
      border-radius: 20px;
      width: 100%;
      max-width: 480px;
      padding: 22px;
    }
    .input-box {
      width: 100%;
      background: rgba(14, 7, 19, 0.85);
      border: 1px solid var(--card-border);
      border-radius: 10px;
      padding: 10px 14px;
      color: white;
      font-size: 0.9rem;
      margin-top: 6px;
      margin-bottom: 12px;
    }
    .input-box:focus { border-color: var(--hot-pink); outline: none; }
  </style>
</head>
<body>
  <header>
    <div class="brand">
      <div class="heart-logo">💕</div>
      <span>Duora Cloud Hub</span>
    </div>
    <div class="desktop-nav">
      <button class="nav-btn active" onclick="goTab('users')">Users & Verification</button>
      <button class="nav-btn" onclick="goTab('avatars')">Avatars & UploadThing</button>
      <button class="nav-btn" onclick="goTab('files')">Files</button>
      <button class="nav-btn" onclick="goTab('audit')">Audit</button>
    </div>
  </header>

  <main>
    <div class="stats-bar">
      <div class="stat-pill">
        <div class="stat-label">Total Users</div>
        <div class="stat-num" id="stat-users">--</div>
      </div>
      <div class="stat-pill">
        <div class="stat-label">Active Pairs</div>
        <div class="stat-num" id="stat-pairs" style="color:var(--hot-pink);">--</div>
      </div>
      <div class="stat-pill">
        <div class="stat-label">Avatars</div>
        <div class="stat-num" id="stat-avatars">--</div>
      </div>
      <div class="stat-pill">
        <div class="stat-label">UploadThing</div>
        <div class="stat-num" id="stat-ut" style="font-size:1rem; color:var(--green-tick); margin-top:6px;">Ready</div>
      </div>
    </div>

    <!-- TAB 1: USERS & VERIFICATION -->
    <div id="tab-users">
      <div class="card">
        <div class="card-title-row">
          <div>
            <h2 style="font-size:1.15rem;">Registered Users & Accounts</h2>
            <p style="color:var(--text-muted); font-size:0.8rem;">Manage Blue Ticks, Green Ticks & Account Suspension</p>
          </div>
          <button class="btn btn-outline" onclick="loadUsers()">↻ Refresh</button>
        </div>
        <div class="user-list" id="users-container">Loading users...</div>
      </div>
    </div>

    <!-- TAB 2: AVATARS & UPLOADTHING -->
    <div id="tab-avatars" style="display:none;">
      <div class="card">
        <div class="card-title-row">
          <div>
            <h2 style="font-size:1.15rem;">Remote Avatar & DP Hub</h2>
            <p style="color:var(--text-muted); font-size:0.8rem;">Serve curated avatars or upload to UploadThing CDN</p>
          </div>
          <button class="btn btn-pink" onclick="openUploadModal()">+ Upload Avatar</button>
        </div>
        <div style="display:grid; grid-template-columns:repeat(auto-fill, minmax(135px, 1fr)); gap:10px;" id="avatar-grid">
          Loading avatars...
        </div>
      </div>
    </div>

    <!-- TAB 3: FILES -->
    <div id="tab-files" style="display:none;">
      <div class="card">
        <h2 style="font-size:1.15rem; margin-bottom:12px;">Storage File Browser</h2>
        <div id="files-list">Loading files...</div>
      </div>
    </div>

    <!-- TAB 4: AUDIT -->
    <div id="tab-audit" style="display:none;">
      <div class="card">
        <h2 style="font-size:1.15rem; margin-bottom:12px;">Audit & Security Log</h2>
        <div id="audit-list">Loading audit trail...</div>
      </div>
    </div>
  </main>

  <!-- Mobile Bottom Tab Bar -->
  <div class="mobile-nav">
    <button class="m-tab active" onclick="goTab('users', this)">
      <span class="m-tab-icon">👥</span>
      <span>Users</span>
    </button>
    <button class="m-tab" onclick="goTab('avatars', this)">
      <span class="m-tab-icon">🎨</span>
      <span>Avatars</span>
    </button>
    <button class="m-tab" onclick="goTab('files', this)">
      <span class="m-tab-icon">📁</span>
      <span>Files</span>
    </button>
    <button class="m-tab" onclick="goTab('audit', this)">
      <span class="m-tab-icon">📝</span>
      <span>Audit</span>
    </button>
  </div>

  <!-- Upload Avatar Modal -->
  <div class="modal-wrap" id="upload-modal">
    <div class="modal-box">
      <h3 style="margin-bottom:12px;">Upload Avatar (UploadThing / Local)</h3>
      <label style="font-size:0.8rem; color:var(--text-muted);">Category</label>
      <select class="input-box" id="new-cat">
        <option value="BOY">BOY</option>
        <option value="GIRL">GIRL</option>
      </select>
      <label style="font-size:0.8rem; color:var(--text-muted);">Display Name</label>
      <input class="input-box" id="new-name" placeholder="e.g. Leo (Cool Smile)" />
      <label style="font-size:0.8rem; color:var(--text-muted);">Avatar ID</label>
      <input class="input-box" id="new-id" placeholder="e.g. boy_011 or girl_011" />
      <label style="font-size:0.8rem; color:var(--text-muted);">Select Image</label>
      <input type="file" id="new-file" accept="image/*" class="input-box" />
      <div style="display:flex; justify-content:flex-end; gap:8px; margin-top:10px;">
        <button class="btn btn-outline" onclick="closeUploadModal()">Cancel</button>
        <button class="btn btn-pink" onclick="submitAvatar()">Save Avatar</button>
      </div>
    </div>
  </div>

  <script>
    let users = [];
    let avatars = [];

    function goTab(name, elem) {
      document.querySelectorAll('.nav-btn, .m-tab').forEach(b => b.classList.remove('active'));
      if (elem) elem.classList.add('active');
      document.querySelectorAll('.desktop-nav button').forEach(b => {
        if (b.innerText.toLowerCase().includes(name)) b.classList.add('active');
      });
      ['users', 'avatars', 'files', 'audit'].forEach(t => {
        document.getElementById('tab-' + t).style.display = (t === name) ? 'block' : 'none';
      });
      if (name === 'users') loadUsers();
      if (name === 'avatars') loadAvatars();
      if (name === 'files') loadFiles();
      if (name === 'audit') loadAudit();
    }

    async function loadUsers() {
      try {
        const res = await fetch('/api/admin/users');
        const json = await res.json();
        users = json.users || [];
        document.getElementById('stat-users').innerText = users.length;
        document.getElementById('stat-pairs').innerText = users.filter(u => u.partnerCode).length;

        const container = document.getElementById('users-container');
        if (users.length === 0) {
          container.innerHTML = '<p style="color:var(--text-muted); padding:10px;">No users registered yet.</p>';
          return;
        }

        container.innerHTML = users.map(u => \`
          <div class="user-card \${u.isBlocked ? 'blocked' : ''}">
            <div class="user-top">
              <img class="user-avatar" src="\${u.avatarUrl || '/assets/avatars/' + (u.avatarId.startsWith('girl') ? 'girls/' : 'boys/') + u.avatarId + '.webp'}" onerror="this.src='/assets/avatars/boys/boy_001.webp'"/>
              <div class="user-info">
                <div class="user-name">
                  <span>\${u.userName || 'User'}</span>
                  \${u.blueTick ? '<span class="tick-blue" title="Blue Tick Verified">🔵</span>' : ''}
                  \${u.greenTick ? '<span class="tick-green" title="Green Tick Verified">🟢</span>' : ''}
                  \${u.isBlocked ? '<span style="color:var(--red); font-size:0.75rem; font-weight:800;">[BLOCKED]</span>' : ''}
                </div>
                <div>
                  <span class="user-code">#\${u.pairCode}</span>
                  \${u.partnerCode ? '<span style="font-size:0.72rem; color:var(--text-muted); margin-left:6px;">❤️ Linked to #' + u.partnerCode + '</span>' : '<span style="font-size:0.72rem; color:var(--text-muted); margin-left:6px;">Unpaired</span>'}
                </div>
              </div>
            </div>
            <div class="user-actions">
              <button class="btn \${u.blueTick ? 'btn-outline' : 'btn-blue'}" onclick="toggleTick('\${u.pairCode}', 'blue', \${!u.blueTick})">
                \${u.blueTick ? 'Remove 🔵' : 'Verify 🔵'}
              </button>
              <button class="btn \${u.greenTick ? 'btn-outline' : 'btn-green'}" onclick="toggleTick('\${u.pairCode}', 'green', \${!u.greenTick})">
                \${u.greenTick ? 'Remove 🟢' : 'Verify 🟢'}
              </button>
              <button class="btn \${u.isBlocked ? 'btn-green' : 'btn-danger'}" onclick="toggleBlock('\${u.pairCode}', \${!u.isBlocked})">
                \${u.isBlocked ? 'Unblock' : 'Block ⛔'}
              </button>
            </div>
          </div>
        \`).join('');
      } catch (e) {
        console.error(e);
      }
    }

    async function toggleTick(pairCode, type, status) {
      await fetch('/api/admin/users/verify', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ pairCode, type, status })
      });
      loadUsers();
    }

    async function toggleBlock(pairCode, isBlocked) {
      if (!confirm((isBlocked ? 'Block' : 'Unblock') + ' user #' + pairCode + '?')) return;
      await fetch('/api/admin/users/block', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ pairCode, isBlocked })
      });
      loadUsers();
    }

    async function loadAvatars() {
      try {
        const res = await fetch('/api/avatars');
        const json = await res.json();
        avatars = json.items || [];
        document.getElementById('stat-avatars').innerText = avatars.length;

        document.getElementById('avatar-grid').innerHTML = avatars.map(a => \`
          <div style="background:rgba(14,7,19,0.7); border:1px solid var(--card-border); border-radius:12px; padding:8px; text-align:center;">
            <img src="\${a.imageUrl}" style="width:100%; aspect-ratio:1; border-radius:8px; object-fit:cover;" onerror="this.src='\${a.thumbnailUrl}'"/>
            <div style="font-size:0.75rem; font-weight:700; margin-top:4px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">\${a.name}</div>
            <div style="font-size:0.65rem; color:var(--hot-pink); font-family:monospace;">\${a.id}</div>
          </div>
        \`).join('');
      } catch (e) {}
    }

    async function loadFiles() {
      try {
        const res = await fetch('/api/admin/files');
        const json = await res.json();
        document.getElementById('files-list').innerHTML = (json.files || []).map(f => \`
          <div style="display:flex; justify-content:space-between; padding:8px 0; border-bottom:1px solid rgba(255,255,255,0.05); font-size:0.82rem;">
            <span>\${f.path}</span>
            <span style="color:var(--text-muted);">\${f.size}</span>
          </div>
        \`).join('');
      } catch (e) {}
    }

    async function loadAudit() {
      try {
        const res = await fetch('/api/admin/audit');
        const json = await res.json();
        document.getElementById('audit-list').innerHTML = (json.audit || []).map(a => \`
          <div style="padding:8px 0; border-bottom:1px solid rgba(255,255,255,0.05); font-size:0.8rem;">
            <div style="font-weight:700; color:var(--hot-pink);">\${a.action} • \${a.target}</div>
            <div style="color:var(--text-muted); font-size:0.72rem;">\${a.timestamp} by \${a.user}</div>
          </div>
        \`).join('');
      } catch (e) {}
    }

    function openUploadModal() { document.getElementById('upload-modal').classList.add('open'); }
    function closeUploadModal() { document.getElementById('upload-modal').classList.remove('open'); }

    async function submitAvatar() {
      const category = document.getElementById('new-cat').value;
      const name = document.getElementById('new-name').value.trim();
      const id = document.getElementById('new-id').value.trim();
      const fileInput = document.getElementById('new-file');
      if (!name || !id) { alert('Fill out Name and ID'); return; }

      let base64 = null;
      if (fileInput.files[0]) {
        base64 = await new Promise(res => {
          const r = new FileReader();
          r.onload = () => res(r.result);
          r.readAsDataURL(fileInput.files[0]);
        });
      }

      const res = await fetch('/api/admin/avatars', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ category, name, id, imageBase64: base64 })
      });
      const data = await res.json();
      if (data.success) {
        closeUploadModal();
        loadAvatars();
      } else {
        alert(data.error || 'Failed');
      }
    }

    loadUsers();
    loadAvatars();
  </script>
</body>
</html>`;
}

// Router handler for Admin and Pairing endpoints
async function handleAdminRoutes(req, res, parsedUrl, readJsonBody, dbContext) {
  const { pool, isDbConnected, memoryClients, wss, forwardToTarget, dbGetProfile } = dbContext || {};
  const pathname = parsedUrl.pathname;

  // 1. Pairing QR Generator (SVG)
  if (pathname.startsWith("/api/pairing/qr/")) {
    const code = pathname.replace(/^\/api\/pairing\/qr\//, "").replace(/\.svg$/, "").trim();
    const svg = await generateQrSvg(code);
    res.writeHead(200, { "Content-Type": "image/svg+xml", "Cache-Control": "public, max-age=86400" });
    return res.end(svg);
  }

  // 1.1. QR Generate API
  if (pathname === "/api/pairing/qr/generate" && req.method === "POST") {
    const body = await readJsonBody(req);
    const code = (body.myCode || body.code || "").toUpperCase().trim();
    if (!code) {
      res.writeHead(400, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: false, error: "Missing myCode" }));
    }
    const token = `DUORA-${code}-${Date.now()}`;
    const svg = await generateQrSvg(code);
    res.writeHead(200, { "Content-Type": "application/json" });
    return res.end(JSON.stringify({
      success: true,
      code,
      token,
      payload: `DUORA:PAIR:${code}`,
      expiresAt: Date.now() + 600000,
      qrSvg: svg
    }));
  }

  // 1.2. QR Scan & Verification API
  if (pathname === "/api/pairing/qr/scan" && req.method === "POST") {
    const body = await readJsonBody(req);
    let raw = (body.qrPayload || body.token || body.code || body.partnerCode || "").trim();

    // Check if payload is a JSON string
    if (raw.startsWith("{") && raw.endsWith("}")) {
      try {
        const parsed = JSON.parse(raw);
        raw = parsed.code || parsed.pairCode || parsed.partnerCode || raw;
      } catch (_) {}
    }

    // Strip scheme prefixes
    raw = raw.replace(/^DUORA:PAIR:/i, "")
             .replace(/^duora:\/\/pair\//i, "")
             .replace(/^DUORA-/i, "")
             .replace(/^#/, "");

    // If timestamp suffix exists (e.g. CODE-1712345678), remove timestamp
    if (raw.includes("-")) {
      const parts = raw.split("-");
      if (parts.length > 1 && /^\d{9,}$/.test(parts[parts.length - 1])) {
        parts.pop();
        raw = parts.join("");
      }
    }

    const target = raw.toUpperCase().replace(/[^A-Z0-9]/g, "").slice(0, 16);

    if (!target || target.length < 4) {
      res.writeHead(400, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: false, error: "Invalid QR pairing payload" }));
    }

    const prof = await dbGetProfile(target);
    if (prof.isBlocked) {
      res.writeHead(403, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: false, error: "This partner account has been suspended" }));
    }

    // Resolve DP url from catalog if empty
    let partnerDpUrl = prof.avatarUrl || "";
    if (!partnerDpUrl) {
      const match = liveCatalog.find(a => a.id === prof.avatarId);
      if (match && (match.uploadThingUrl || match.imageUrl)) {
        partnerDpUrl = match.uploadThingUrl || match.imageUrl;
      }
    }

    res.writeHead(200, { "Content-Type": "application/json" });
    return res.end(JSON.stringify({
      success: true,
      partnerCode: prof.pairCode,
      partnerName: prof.userName,
      partnerAvatarId: prof.avatarId,
      partnerAvatarUrl: partnerDpUrl,
      blueTick: !!prof.blueTick,
      greenTick: !!prof.greenTick,
      partner: {
        code: prof.pairCode,
        name: prof.userName,
        avatarId: prof.avatarId,
        avatarUrl: partnerDpUrl,
        blueTick: !!prof.blueTick,
        greenTick: !!prof.greenTick,
        status: prof.partnerStatus,
        isPaired: !!prof.partnerCode
      }
    }));
  }

  // 2. Direct DP Fetching API (/api/profile/dp/:code)
  if (pathname.startsWith("/api/profile/dp/") && req.method === "GET") {
    const code = pathname.replace("/api/profile/dp/", "").toUpperCase().trim();
    const prof = await dbGetProfile(code);
    if (prof.avatarUrl && prof.avatarUrl.startsWith("http")) {
      res.writeHead(302, { Location: prof.avatarUrl });
      return res.end();
    }
    const catMatch = liveCatalog.find(a => a.id === prof.avatarId);
    if (catMatch && (catMatch.uploadThingUrl || catMatch.imageUrl)) {
      res.writeHead(302, { Location: catMatch.uploadThingUrl || catMatch.imageUrl });
      return res.end();
    }
    const sub = prof.avatarId.startsWith("girl") ? "girls" : "boys";
    const file = path.join(AVATARS_ROOT, sub, `${prof.avatarId}.webp`);
    if (fs.existsSync(file)) {
      res.writeHead(200, { "Content-Type": "image/webp", "Cache-Control": "public, max-age=86400" });
      return fs.createReadStream(file).pipe(res);
    }
    res.writeHead(302, { Location: `/assets/avatars/${sub}/${prof.avatarId}.webp` });
    return res.end();
  }

  // 3. UploadThing / Custom DP Upload Endpoint
  if (pathname === "/api/profile/dp/upload" && req.method === "POST") {
    try {
      const body = await readJsonBody(req);
      const code = (body.pairCode || body.code || "").toUpperCase().trim();
      const imageBase64 = body.imageBase64 || "";
      if (!code || !imageBase64) {
        res.writeHead(400, { "Content-Type": "application/json" });
        return res.end(JSON.stringify({ success: false, error: "Missing code or imageBase64" }));
      }

      const buffer = Buffer.from(imageBase64.replace(/^data:image\/\w+;base64,/, ""), "base64");
      const uploadRes = await uploadToUploadThing(buffer, `dp_${code}.webp`, "image/webp");

      // Save avatar_url to profile
      if (pool && isDbConnected) {
        await pool.query(`UPDATE couples SET avatar_url = $1, updated_at = NOW() WHERE pair_code = $2;`, [uploadRes.url, code]);
      }
      const entry = memoryClients.get(code);
      if (entry) entry.avatarUrl = uploadRes.url;

      const prof = await dbGetProfile(code);
      if (prof.partnerCode && forwardToTarget) {
        forwardToTarget(prof.partnerCode, {
          type: "partner_avatar_updated",
          partnerCode: code,
          partnerAvatarId: prof.avatarId,
          partnerAvatarUrl: uploadRes.url
        });
      }

      logAudit("UPLOAD_DP", `Custom DP uploaded for #${code} (${uploadRes.service})`);

      res.writeHead(200, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({
        success: true,
        url: uploadRes.url,
        service: uploadRes.service
      }));
    } catch (e) {
      res.writeHead(500, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: false, error: e.message }));
    }
  }

  // 4. Admin Web UI at /admin
  if (pathname === "/admin" || pathname === "/admin/") {
    const host = req.headers.host || "localhost:3000";
    const proto = req.headers["x-forwarded-proto"] || "http";
    res.writeHead(200, { "Content-Type": "text/html; charset=utf-8" });
    return res.end(getAdminHtml(`${proto}://${host}`));
  }

  // 5. Admin Users List
  if (pathname === "/api/admin/users" && req.method === "GET") {
    try {
      const userList = [];
      const seen = new Set();

      if (pool && isDbConnected) {
        const result = await pool.query(`
          SELECT pair_code AS "pairCode", partner_code AS "partnerCode", user_name AS "userName",
                 COALESCE(avatar_id, 'boy_001') AS "avatarId", COALESCE(avatar_url, '') AS "avatarUrl",
                 COALESCE(is_blocked, FALSE) AS "isBlocked", COALESCE(blue_tick, FALSE) AS "blueTick",
                 COALESCE(green_tick, FALSE) AS "greenTick", last_seen AS "lastSeen"
          FROM couples ORDER BY updated_at DESC;
        `);
        for (const r of result.rows) {
          seen.add(r.pairCode);
          userList.push({
            ...r,
            isOnline: memoryClients && memoryClients.has(r.pairCode) && memoryClients.get(r.pairCode).sockets.size > 0
          });
        }
      }

      if (memoryClients) {
        for (const [code, entry] of memoryClients.entries()) {
          if (!seen.has(code)) {
            userList.push({
              pairCode: code,
              partnerCode: entry.partnerCode || "",
              userName: entry.userName || "You",
              avatarId: entry.avatarId || "boy_001",
              avatarUrl: entry.avatarUrl || "",
              isBlocked: !!entry.isBlocked,
              blueTick: !!entry.blueTick,
              greenTick: !!entry.greenTick,
              isOnline: entry.sockets && entry.sockets.size > 0
            });
          }
        }
      }

      res.writeHead(200, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: true, count: userList.length, users: userList }));
    } catch (e) {
      res.writeHead(500, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: false, error: e.message }));
    }
  }

  // 6. Admin User Block / Unblock
  if (pathname === "/api/admin/users/block" && req.method === "POST") {
    try {
      const body = await readJsonBody(req);
      const { pairCode, isBlocked } = body;
      const code = (pairCode || "").toUpperCase().trim();
      if (!code) {
        res.writeHead(400, { "Content-Type": "application/json" });
        return res.end(JSON.stringify({ success: false, error: "Missing pairCode" }));
      }

      if (pool && isDbConnected) {
        await pool.query(`UPDATE couples SET is_blocked = $1, updated_at = NOW() WHERE pair_code = $2;`, [!!isBlocked, code]);
      }
      const entry = memoryClients ? memoryClients.get(code) : null;
      if (entry) {
        entry.isBlocked = !!isBlocked;
        if (isBlocked && entry.sockets) {
          for (const s of entry.sockets) {
            try {
              s.send(JSON.stringify({ type: "account_blocked", message: "Account suspended by administration" }));
              s.close();
            } catch (_) {}
          }
        }
      }

      logAudit(isBlocked ? "BLOCK_USER" : "UNBLOCK_USER", `User #${code}`);
      res.writeHead(200, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: true, isBlocked: !!isBlocked, code }));
    } catch (e) {
      res.writeHead(500, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: false, error: e.message }));
    }
  }

  // 7. Admin User Verify (Blue Tick / Green Tick)
  if (pathname === "/api/admin/users/verify" && req.method === "POST") {
    try {
      const body = await readJsonBody(req);
      const { pairCode, type, status } = body;
      const code = (pairCode || "").toUpperCase().trim();
      const col = type === "green" ? "green_tick" : "blue_tick";

      if (pool && isDbConnected) {
        await pool.query(`UPDATE couples SET ${col} = $1, updated_at = NOW() WHERE pair_code = $2;`, [!!status, code]);
      }
      const entry = memoryClients ? memoryClients.get(code) : null;
      if (entry) {
        if (type === "green") entry.greenTick = !!status;
        else entry.blueTick = !!status;
      }

      logAudit("VERIFY_USER", `${type.toUpperCase()} tick set to ${status} for #${code}`);
      res.writeHead(200, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: true, code, type, status: !!status }));
    } catch (e) {
      res.writeHead(500, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: false, error: e.message }));
    }
  }

  // 8. Admin Files API
  if (pathname === "/api/admin/files" && req.method === "GET") {
    try {
      const files = [];
      function walk(dir, rel = "") {
        if (!fs.existsSync(dir)) return;
        const entries = fs.readdirSync(dir, { withFileTypes: true });
        for (const e of entries) {
          const subRel = path.join(rel, e.name);
          const subFull = path.join(dir, e.name);
          if (e.isDirectory()) {
            walk(subFull, subRel);
          } else {
            const stat = fs.statSync(subFull);
            files.push({
              path: subRel,
              type: path.extname(e.name).replace(".", "").toUpperCase() || "FILE",
              size: (stat.size / 1024).toFixed(1) + " KB",
              modified: stat.mtime.toISOString().split("T")[0]
            });
          }
        }
      }
      walk(AVATARS_ROOT, "avatars");
      res.writeHead(200, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: true, files }));
    } catch (e) {
      res.writeHead(500, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: false, error: e.message }));
    }
  }

  // 9. Admin Avatars API (Add)
  if (pathname === "/api/admin/avatars" && req.method === "POST") {
    try {
      const body = await readJsonBody(req);
      const { id, name, category, tags, imageBase64 } = body;
      if (!id || !name || !category) {
        res.writeHead(400, { "Content-Type": "application/json" });
        return res.end(JSON.stringify({ success: false, error: "Missing required avatar fields" }));
      }

      const subDir = category.toUpperCase() === "GIRL" ? "girls" : "boys";
      const filename = `${id}.webp`;
      const targetDir = path.join(AVATARS_ROOT, subDir);
      const targetFile = path.join(targetDir, filename);
      fs.mkdirSync(targetDir, { recursive: true });

      if (imageBase64) {
        const base64Data = imageBase64.replace(/^data:image\/\w+;base64,/, "");
        fs.writeFileSync(targetFile, Buffer.from(base64Data, "base64"));
      } else if (!fs.existsSync(targetFile)) {
        const defaultSample = path.join(AVATARS_ROOT, subDir, `${subDir === "girls" ? "girl_001" : "boy_001"}.webp`);
        if (fs.existsSync(defaultSample)) fs.copyFileSync(defaultSample, targetFile);
      }

      const existingIdx = liveCatalog.findIndex(a => a.id === id);
      const newEntry = {
        id,
        name,
        category: category.toUpperCase(),
        tags: tags || ["Custom"],
        assetPath: `${subDir}/${filename}`
      };

      if (existingIdx >= 0) liveCatalog[existingIdx] = newEntry;
      else liveCatalog.push(newEntry);

      saveCatalogToDisk(liveCatalog);
      logAudit(existingIdx >= 0 ? "UPDATE_AVATAR" : "CREATE_AVATAR", `${id} (${name})`);

      res.writeHead(200, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: true, avatar: newEntry }));
    } catch (e) {
      res.writeHead(500, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: false, error: e.message }));
    }
  }

  // 10. Admin Audit Log API
  if (pathname === "/api/admin/audit" && req.method === "GET") {
    res.writeHead(200, { "Content-Type": "application/json" });
    return res.end(JSON.stringify({ success: true, audit: auditLog }));
  }

  return false;
}

module.exports = {
  getCatalog,
  handleAdminRoutes,
  uploadToUploadThing,
  generateQrSvg
};
