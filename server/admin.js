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
// Responsive, Mobile-friendly Single-Page Admin Web App with Pure Vector SVG Icons
function getAdminHtml(baseUrl) {
  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>Duora • Cloud Hub & User Manager</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@500;700&display=swap">
  <style>
    :root {
      --bg: #09040D;
      --bg-gradient: radial-gradient(circle at 50% -20%, #2A0E38 0%, #09040D 70%);
      --card: rgba(22, 12, 31, 0.75);
      --card-hover: rgba(30, 16, 42, 0.9);
      --card-border: rgba(255, 255, 255, 0.08);
      --card-border-focus: rgba(255, 45, 141, 0.5);
      --text: #F8F5FB;
      --text-muted: #9E91AC;
      --hot-pink: #FF2D8D;
      --hot-pink-glow: rgba(255, 45, 141, 0.35);
      --purple: #8B5CF6;
      --blue-tick: #38BDF8;
      --green-tick: #22C55E;
      --red: #EF4444;
      --yellow: #F59E0B;
      --radius-lg: 18px;
      --radius-md: 12px;
      --radius-sm: 8px;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; -webkit-tap-highlight-color: transparent; }
    body {
      font-family: 'Plus Jakarta Sans', sans-serif;
      background: var(--bg);
      background-image: var(--bg-gradient);
      background-attachment: fixed;
      color: var(--text);
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      padding-bottom: 78px;
    }
    header {
      background: rgba(14, 7, 20, 0.85);
      backdrop-filter: blur(16px);
      -webkit-backdrop-filter: blur(16px);
      border-bottom: 1px solid var(--card-border);
      padding: 12px 20px;
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
      gap: 12px;
      font-weight: 800;
      font-size: 1.15rem;
      letter-spacing: -0.3px;
    }
    .brand-icon {
      width: 36px;
      height: 36px;
      background: linear-gradient(135deg, var(--hot-pink), var(--purple));
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      box-shadow: 0 4px 14px var(--hot-pink-glow);
    }
    .brand-text { display: flex; flex-direction: column; }
    .brand-title { font-size: 1.05rem; font-weight: 800; line-height: 1.2; }
    .brand-sub { font-size: 0.68rem; color: var(--text-muted); font-weight: 600; text-transform: uppercase; letter-spacing: 0.8px; }
    
    .desktop-nav { display: flex; gap: 6px; }
    @media (max-width: 768px) {
      .desktop-nav { display: none; }
    }
    .nav-btn {
      padding: 8px 16px;
      background: transparent;
      border: 1px solid transparent;
      color: var(--text-muted);
      font-weight: 600;
      font-size: 0.84rem;
      border-radius: var(--radius-md);
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 8px;
      transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
    }
    .nav-btn:hover {
      color: var(--text);
      background: rgba(255, 255, 255, 0.04);
    }
    .nav-btn.active {
      background: var(--hot-pink);
      color: white;
      box-shadow: 0 4px 14px var(--hot-pink-glow);
    }
    main {
      flex: 1;
      padding: 20px 16px;
      max-width: 1240px;
      margin: 0 auto;
      width: 100%;
    }
    .stats-bar {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
      gap: 12px;
      margin-bottom: 20px;
    }
    .stat-pill {
      background: var(--card);
      backdrop-filter: blur(10px);
      border: 1px solid var(--card-border);
      border-radius: var(--radius-md);
      padding: 14px 16px;
      display: flex;
      flex-direction: column;
      transition: border-color 0.2s;
    }
    .stat-pill:hover { border-color: rgba(255,255,255,0.18); }
    .stat-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      color: var(--text-muted);
    }
    .stat-label { font-size: 0.72rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.6px; }
    .stat-num { font-size: 1.55rem; font-weight: 800; color: white; margin-top: 4px; font-feature-settings: "tnum"; }
    
    .card {
      background: var(--card);
      backdrop-filter: blur(12px);
      -webkit-backdrop-filter: blur(12px);
      border: 1px solid var(--card-border);
      border-radius: var(--radius-lg);
      padding: 20px;
      margin-bottom: 20px;
      box-shadow: 0 8px 24px rgba(0,0,0,0.3);
    }
    .card-title-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 16px;
      flex-wrap: wrap;
      gap: 12px;
    }
    .btn {
      padding: 8px 14px;
      border-radius: var(--radius-md);
      border: none;
      font-weight: 700;
      font-size: 0.82rem;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 7px;
      transition: all 0.18s cubic-bezier(0.16, 1, 0.3, 1);
      user-select: none;
    }
    .btn:active { transform: scale(0.97); }
    .btn-pink { background: var(--hot-pink); color: white; box-shadow: 0 3px 10px var(--hot-pink-glow); }
    .btn-pink:hover { background: #FF479D; }
    .btn-blue { background: rgba(56, 189, 248, 0.12); color: var(--blue-tick); border: 1px solid rgba(56,189,248,0.35); }
    .btn-blue:hover { background: rgba(56, 189, 248, 0.22); }
    .btn-green { background: rgba(34, 197, 94, 0.12); color: var(--green-tick); border: 1px solid rgba(34,197,94,0.35); }
    .btn-green:hover { background: rgba(34, 197, 94, 0.22); }
    .btn-danger { background: rgba(239, 68, 68, 0.12); color: var(--red); border: 1px solid rgba(239,68,68,0.35); }
    .btn-danger:hover { background: rgba(239, 68, 68, 0.22); }
    .btn-outline { background: rgba(255,255,255,0.04); color: var(--text); border: 1px solid var(--card-border); }
    .btn-outline:hover { background: rgba(255,255,255,0.08); border-color: rgba(255,255,255,0.2); }
    
    /* Search & Filter Bar */
    .filter-bar {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
      margin-bottom: 16px;
    }
    .filter-chip {
      padding: 6px 12px;
      border-radius: 20px;
      font-size: 0.75rem;
      font-weight: 700;
      color: var(--text-muted);
      background: rgba(255, 255, 255, 0.04);
      border: 1px solid var(--card-border);
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 5px;
      transition: all 0.15s;
    }
    .filter-chip.active {
      color: white;
      background: var(--hot-pink);
      border-color: var(--hot-pink);
      box-shadow: 0 2px 8px var(--hot-pink-glow);
    }
    
    /* Search input box with icon */
    .search-wrap {
      position: relative;
      display: flex;
      align-items: center;
      min-width: 220px;
      flex: 1;
      max-width: 320px;
    }
    .search-icon {
      position: absolute;
      left: 12px;
      color: var(--text-muted);
      pointer-events: none;
      display: flex;
    }
    .search-input {
      width: 100%;
      background: rgba(14, 7, 20, 0.85);
      border: 1px solid var(--card-border);
      border-radius: var(--radius-md);
      padding: 9px 12px 9px 36px;
      color: white;
      font-size: 0.82rem;
      transition: border-color 0.2s, box-shadow 0.2s;
    }
    .search-input:focus {
      border-color: var(--hot-pink);
      outline: none;
      box-shadow: 0 0 0 3px var(--hot-pink-glow);
    }
    
    /* Users Grid */
    .user-list {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: 14px;
    }
    .user-card {
      background: rgba(14, 7, 20, 0.65);
      border: 1px solid var(--card-border);
      border-radius: var(--radius-md);
      padding: 16px;
      display: flex;
      flex-direction: column;
      gap: 12px;
      transition: border-color 0.2s, transform 0.2s;
    }
    .user-card:hover {
      border-color: rgba(255, 255, 255, 0.18);
      transform: translateY(-2px);
    }
    .user-card.blocked {
      border-color: rgba(239, 68, 68, 0.5);
      background: rgba(239, 68, 68, 0.06);
    }
    .user-top {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .avatar-wrap {
      position: relative;
      flex-shrink: 0;
    }
    .user-avatar {
      width: 52px;
      height: 52px;
      border-radius: 50%;
      object-fit: cover;
      background: #251336;
      border: 2px solid var(--card-border);
      display: block;
    }
    .status-badge {
      position: absolute;
      bottom: 0;
      right: 0;
      width: 13px;
      height: 13px;
      border-radius: 50%;
      border: 2px solid #0E0714;
    }
    .status-badge.online { background: var(--green-tick); box-shadow: 0 0 6px rgba(34, 197, 94, 0.8); }
    .status-badge.offline { background: #64748B; }
    
    .user-info { flex: 1; overflow: hidden; }
    .user-name-row {
      display: flex;
      align-items: center;
      gap: 6px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .user-name {
      font-size: 0.96rem;
      font-weight: 700;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .badge-icon {
      width: 17px;
      height: 17px;
      flex-shrink: 0;
      vertical-align: middle;
      display: inline-flex;
    }
    .user-handle {
      font-size: 0.75rem;
      color: var(--text-muted);
      font-weight: 500;
    }
    .user-meta-row {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-top: 4px;
      flex-wrap: wrap;
    }
    .code-chip {
      font-family: 'JetBrains Mono', monospace;
      font-size: 0.74rem;
      font-weight: 700;
      color: var(--hot-pink);
      background: rgba(255, 45, 141, 0.12);
      border: 1px solid rgba(255, 45, 141, 0.25);
      padding: 2px 7px;
      border-radius: 6px;
      display: inline-flex;
      align-items: center;
      gap: 4px;
      cursor: pointer;
    }
    .code-chip:hover { background: rgba(255, 45, 141, 0.2); }
    
    .partner-box {
      background: rgba(255, 255, 255, 0.025);
      border: 1px solid var(--card-border);
      border-radius: 10px;
      padding: 8px 10px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-size: 0.76rem;
    }
    .partner-info {
      display: flex;
      align-items: center;
      gap: 6px;
      color: var(--hot-pink);
      font-weight: 600;
    }
    .partner-unpaired {
      color: var(--text-muted);
      font-style: italic;
    }
    
    .user-actions {
      display: flex;
      gap: 6px;
      flex-wrap: wrap;
      margin-top: 2px;
    }
    .user-actions .btn { flex: 1; min-width: 90px; justify-content: center; font-size: 0.76rem; padding: 7px 10px; }
    
    /* Mobile Bottom Navigation */
    .mobile-nav {
      display: none;
      position: fixed;
      bottom: 0;
      left: 0;
      right: 0;
      background: rgba(14, 7, 20, 0.94);
      backdrop-filter: blur(18px);
      -webkit-backdrop-filter: blur(18px);
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
      gap: 3px;
      font-size: 0.7rem;
      font-weight: 600;
      color: var(--text-muted);
      background: transparent;
      border: none;
      padding: 6px 12px;
      border-radius: var(--radius-sm);
      cursor: pointer;
      transition: color 0.15s;
    }
    .m-tab.active { color: var(--hot-pink); }
    .m-tab-icon { display: flex; align-items: center; justify-content: center; }
    
    /* Modal */
    .modal-wrap {
      display: none;
      position: fixed;
      inset: 0;
      background: rgba(0,0,0,0.8);
      backdrop-filter: blur(10px);
      -webkit-backdrop-filter: blur(10px);
      z-index: 100;
      align-items: center;
      justify-content: center;
      padding: 16px;
    }
    .modal-wrap.open { display: flex; }
    .modal-box {
      background: #170C22;
      border: 1px solid var(--card-border);
      border-radius: var(--radius-lg);
      width: 100%;
      max-width: 480px;
      padding: 24px;
      box-shadow: 0 12px 36px rgba(0,0,0,0.5);
    }
    .input-box {
      width: 100%;
      background: rgba(10, 4, 15, 0.85);
      border: 1px solid var(--card-border);
      border-radius: var(--radius-md);
      padding: 10px 14px;
      color: white;
      font-size: 0.88rem;
      margin-top: 6px;
      margin-bottom: 12px;
      transition: border-color 0.2s;
    }
    .input-box:focus { border-color: var(--hot-pink); outline: none; }
    
    /* Toast System */
    #toast-container {
      position: fixed;
      bottom: 24px;
      right: 24px;
      z-index: 1000;
      display: flex;
      flex-direction: column;
      gap: 8px;
      pointer-events: none;
    }
    @media (max-width: 768px) {
      #toast-container { bottom: 84px; right: 16px; left: 16px; }
    }
    .toast {
      background: rgba(26, 14, 38, 0.95);
      border: 1px solid var(--card-border);
      backdrop-filter: blur(12px);
      color: white;
      padding: 12px 18px;
      border-radius: var(--radius-md);
      font-size: 0.84rem;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 10px;
      box-shadow: 0 8px 24px rgba(0,0,0,0.4);
      animation: slideIn 0.25s ease-out;
      pointer-events: auto;
    }
    .toast.success { border-color: rgba(34, 197, 94, 0.5); color: #86EFAC; }
    .toast.error { border-color: rgba(239, 68, 68, 0.5); color: #FCA5A5; }
    @keyframes slideIn { from { transform: translateY(12px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
  </style>
</head>
<body>
  <div id="toast-container"></div>

  <!-- Confirmation Modal -->
  <div class="modal-wrap" id="confirm-modal">
    <div class="modal-box">
      <div style="display:flex; align-items:center; gap:10px; margin-bottom:12px;">
        <div id="confirm-icon" style="color:var(--hot-pink);"></div>
        <h3 id="confirm-title" style="font-size:1.1rem; font-weight:700;">Confirm Action</h3>
      </div>
      <p id="confirm-message" style="color:var(--text-muted); font-size:0.86rem; line-height:1.45; margin-bottom:18px;"></p>
      <div style="display:flex; justify-content:flex-end; gap:8px;">
        <button class="btn btn-outline" onclick="closeConfirmModal()">Cancel</button>
        <button class="btn btn-pink" id="confirm-btn">Confirm</button>
      </div>
    </div>
  </div>

  <header>
    <div class="brand">
      <div class="brand-icon">
        <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
        </svg>
      </div>
      <div class="brand-text">
        <span class="brand-title">Duora Cloud Hub</span>
        <span class="brand-sub">Admin Engine</span>
      </div>
    </div>
    <div class="desktop-nav">
      <button class="nav-btn active" onclick="goTab('users', this)">
        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
        Users & Verification
      </button>
      <button class="nav-btn" onclick="goTab('avatars', this)">
        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="13.5" cy="6.5" r=".5" fill="currentColor"/><circle cx="17.5" cy="10.5" r=".5" fill="currentColor"/><circle cx="8.5" cy="7.5" r=".5" fill="currentColor"/><circle cx="6.5" cy="12.5" r=".5" fill="currentColor"/><path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c.926 0 1.648-.746 1.648-1.688 0-.437-.18-.835-.437-1.125-.29-.289-.438-.652-.438-1.125a1.64 1.64 0 0 1 1.668-1.668h1.996c3.051 0 5.555-2.503 5.555-5.554C21.965 6.012 17.461 2 12 2z"/></svg>
        Avatars & CDN
      </button>
      <button class="nav-btn" onclick="goTab('files', this)">
        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
        Files
      </button>
      <button class="nav-btn" onclick="goTab('audit', this)">
        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/><polyline points="9 12 11 14 15 10"/></svg>
        Audit Log
      </button>
    </div>
  </header>

  <main>
    <div class="stats-bar">
      <div class="stat-pill">
        <div class="stat-header">
          <span class="stat-label">Total Users</span>
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
        </div>
        <div class="stat-num" id="stat-users">--</div>
      </div>
      <div class="stat-pill">
        <div class="stat-header">
          <span class="stat-label">Paired Couples</span>
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="var(--hot-pink)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path></svg>
        </div>
        <div class="stat-num" id="stat-pairs" style="color:var(--hot-pink);">--</div>
      </div>
      <div class="stat-pill">
        <div class="stat-header">
          <span class="stat-label">Verified</span>
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="var(--blue-tick)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
        </div>
        <div class="stat-num" id="stat-verified" style="color:var(--blue-tick);">--</div>
      </div>
      <div class="stat-pill">
        <div class="stat-header">
          <span class="stat-label">Avatars</span>
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/></svg>
        </div>
        <div class="stat-num" id="stat-avatars">--</div>
      </div>
      <div class="stat-pill">
        <div class="stat-header">
          <span class="stat-label">UploadThing</span>
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="var(--green-tick)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
        </div>
        <div class="stat-num" id="stat-ut" style="font-size:1rem; color:var(--green-tick); margin-top:8px;">Ready</div>
      </div>
    </div>

    <!-- TAB 1: USERS & VERIFICATION -->
    <div id="tab-users">
      <!-- REAL-TIME NOTICE CARD -->
      <div class="card">
        <div class="card-title-row">
          <div>
            <h2 style="font-size:1.1rem; display:flex; align-items:center; gap:8px;">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="var(--hot-pink)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m3 11 18-5v12L3 14v-3z"/><path d="M11.6 16.8a3 3 0 1 1-5.8-1.6"/></svg>
              Real-Time Notice & Push Broadcast
            </h2>
            <p style="color:var(--text-muted); font-size:0.8rem; margin-top:2px;">Instantly send official notices or alert banners to all connected couple devices.</p>
          </div>
        </div>
        <div style="display:flex; flex-direction:column; gap:10px;">
          <input class="input-box" id="broadcast-title" placeholder="Notice Title (e.g. Duora Team Announcement)" style="margin:0;" />
          <textarea class="input-box" id="broadcast-msg" rows="2" placeholder="Write notice content to broadcast to all apps in real-time..." style="margin:0; resize:vertical; font-family:inherit;"></textarea>
          <div style="display:flex; align-items:center; justify-content:space-between; flex-wrap:wrap; gap:10px; margin-top:2px;">
            <label style="font-size:0.82rem; color:var(--text-muted); display:inline-flex; align-items:center; gap:6px; cursor:pointer;">
              <input type="checkbox" id="broadcast-alert" style="accent-color:var(--hot-pink);" />
              <span>Send as priority alert popup in app</span>
            </label>
            <button class="btn btn-pink" onclick="sendBroadcastNotice()">
              <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
              Broadcast Notice
            </button>
          </div>
        </div>
      </div>

      <!-- USERS MANAGEMENT CARD -->
      <div class="card">
        <div class="card-title-row">
          <div>
            <h2 style="font-size:1.1rem; display:flex; align-items:center; gap:8px;">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="var(--purple)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
              Registered Users & Accounts
            </h2>
            <p style="color:var(--text-muted); font-size:0.8rem; margin-top:2px;">Verified Blue Ticks, Admin Green Ticks, Account Suspension & Single-Click Unpairing</p>
          </div>
          <div style="display:flex; gap:8px; align-items:center; flex-wrap:wrap;">
            <div class="search-wrap">
              <span class="search-icon">
                <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
              </span>
              <input id="search-users" class="search-input" placeholder="Search name, code, partner..." oninput="filterUsers()" />
            </div>
            <button class="btn btn-outline" onclick="loadUsers()" title="Refresh User List">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.5 2v6h-6M21.34 15.57a10 10 0 1 1-.57-8.38l5.67-5.67"/></svg>
              Refresh
            </button>
            <button class="btn btn-outline" style="border-color:rgba(245,158,11,0.4); color:var(--yellow);" onclick="cleanDuplicates()" title="Clean Duplicate Codes">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 14a1 1 0 0 1-.78-1.63l9.9-10.2a.5.5 0 0 1 .86.46l-1.92 6.02A1 1 0 0 0 13 10h7a1 1 0 0 1 .78 1.63l-9.9 10.2a.5.5 0 0 1-.86-.46l1.92-6.02A1 1 0 0 0 11 14z"/></svg>
              Clean Stale
            </button>
            <button class="btn btn-danger" onclick="wipeDatabase()" title="Wipe all data for fresh testing">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="7.86 2 16.14 2 22 7.86 22 16.14 16.14 22 7.86 22 2 16.14 2 7.86 7.86 2"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              Wipe (Fresh Start)
            </button>
          </div>
        </div>

        <!-- Filter Chips Row -->
        <div class="filter-bar">
          <button class="filter-chip active" data-filter="all" onclick="setFilter('all', this)">All Accounts</button>
          <button class="filter-chip" data-filter="online" onclick="setFilter('online', this)">
            <span style="display:inline-block; width:7px; height:7px; border-radius:50%; background:var(--green-tick);"></span>
            Online
          </button>
          <button class="filter-chip" data-filter="paired" onclick="setFilter('paired', this)">
            <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg>
            Paired
          </button>
          <button class="filter-chip" data-filter="verified" onclick="setFilter('verified', this)">
            <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="var(--blue-tick)" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
            Verified Blue
          </button>
          <button class="filter-chip" data-filter="admin" onclick="setFilter('admin', this)">
            <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="var(--green-tick)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
            Admin Green
          </button>
          <button class="filter-chip" data-filter="suspended" onclick="setFilter('suspended', this)">
            <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="var(--red)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/></svg>
            Suspended
          </button>
        </div>

        <div class="user-list" id="users-container">Loading users...</div>
      </div>
    </div>

    <!-- TAB 2: AVATARS & UPLOADTHING -->
    <div id="tab-avatars" style="display:none;">
      <div class="card">
        <div class="card-title-row">
          <div>
            <h2 style="font-size:1.1rem; display:flex; align-items:center; gap:8px;">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="var(--hot-pink)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="13.5" cy="6.5" r=".5" fill="currentColor"/><circle cx="17.5" cy="10.5" r=".5" fill="currentColor"/><circle cx="8.5" cy="7.5" r=".5" fill="currentColor"/><circle cx="6.5" cy="12.5" r=".5" fill="currentColor"/><path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c.926 0 1.648-.746 1.648-1.688 0-.437-.18-.835-.437-1.125-.29-.289-.438-.652-.438-1.125a1.64 1.64 0 0 1 1.668-1.668h1.996c3.051 0 5.555-2.503 5.555-5.554C21.965 6.012 17.461 2 12 2z"/></svg>
              Remote Avatar & DP Catalog
            </h2>
            <p style="color:var(--text-muted); font-size:0.8rem; margin-top:2px;">Curate avatar collections or upload new styles directly to UploadThing CDN</p>
          </div>
          <button class="btn btn-pink" onclick="openUploadModal()">
            <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            Upload Avatar
          </button>
        </div>
        <div style="display:grid; grid-template-columns:repeat(auto-fill, minmax(140px, 1fr)); gap:12px;" id="avatar-grid">
          Loading avatars...
        </div>
      </div>
    </div>

    <!-- TAB 3: FILES -->
    <div id="tab-files" style="display:none;">
      <div class="card">
        <h2 style="font-size:1.1rem; margin-bottom:12px; display:flex; align-items:center; gap:8px;">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="var(--blue-tick)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
          Storage File Browser
        </h2>
        <div id="files-list">Loading files...</div>
      </div>
    </div>

    <!-- TAB 4: AUDIT -->
    <div id="tab-audit" style="display:none;">
      <div class="card">
        <h2 style="font-size:1.1rem; margin-bottom:12px; display:flex; align-items:center; gap:8px;">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="var(--green-tick)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/><polyline points="9 12 11 14 15 10"/></svg>
          Audit & Security Activity Log
        </h2>
        <div id="audit-list">Loading audit trail...</div>
      </div>
    </div>
  </main>

  <!-- Mobile Bottom Tab Bar -->
  <div class="mobile-nav">
    <button class="m-tab active" onclick="goTab('users', this)">
      <span class="m-tab-icon">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
      </span>
      <span>Users</span>
    </button>
    <button class="m-tab" onclick="goTab('avatars', this)">
      <span class="m-tab-icon">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="13.5" cy="6.5" r=".5" fill="currentColor"/><circle cx="17.5" cy="10.5" r=".5" fill="currentColor"/><circle cx="8.5" cy="7.5" r=".5" fill="currentColor"/><circle cx="6.5" cy="12.5" r=".5" fill="currentColor"/><path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c.926 0 1.648-.746 1.648-1.688 0-.437-.18-.835-.437-1.125-.29-.289-.438-.652-.438-1.125a1.64 1.64 0 0 1 1.668-1.668h1.996c3.051 0 5.555-2.503 5.555-5.554C21.965 6.012 17.461 2 12 2z"/></svg>
      </span>
      <span>Avatars</span>
    </button>
    <button class="m-tab" onclick="goTab('files', this)">
      <span class="m-tab-icon">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
      </span>
      <span>Files</span>
    </button>
    <button class="m-tab" onclick="goTab('audit', this)">
      <span class="m-tab-icon">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/><polyline points="9 12 11 14 15 10"/></svg>
      </span>
      <span>Audit</span>
    </button>
  </div>

  <!-- Upload Avatar Modal -->
  <div class="modal-wrap" id="upload-modal">
    <div class="modal-box">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:14px;">
        <h3 style="font-size:1.1rem; font-weight:700;">Upload Avatar (UploadThing / Local)</h3>
        <button onclick="closeUploadModal()" style="background:none; border:none; color:var(--text-muted); cursor:pointer;">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
      </div>
      <label style="font-size:0.78rem; color:var(--text-muted); font-weight:600;">Category</label>
      <select class="input-box" id="new-cat">
        <option value="BOY">BOY</option>
        <option value="GIRL">GIRL</option>
      </select>
      <label style="font-size:0.78rem; color:var(--text-muted); font-weight:600;">Display Name</label>
      <input class="input-box" id="new-name" placeholder="e.g. Leo (Cool Jacket)" />
      <label style="font-size:0.78rem; color:var(--text-muted); font-weight:600;">Avatar ID</label>
      <input class="input-box" id="new-id" placeholder="e.g. boy_011 or girl_011" />
      <label style="font-size:0.78rem; color:var(--text-muted); font-weight:600;">Select Image</label>
      <input type="file" id="new-file" accept="image/*" class="input-box" />
      <div style="display:flex; justify-content:flex-end; gap:8px; margin-top:14px;">
        <button class="btn btn-outline" onclick="closeUploadModal()">Cancel</button>
        <button class="btn btn-pink" onclick="submitAvatar()">
          <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          Save Avatar
        </button>
      </div>
    </div>
  </div>

  <script>
    let users = [];
    let avatars = [];
    let currentFilter = 'all';

    function showToast(message, type = 'info') {
      const container = document.getElementById('toast-container');
      const toast = document.createElement('div');
      toast.className = 'toast ' + type;
      toast.innerText = message;
      container.appendChild(toast);
      setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(8px)';
        toast.style.transition = 'all 0.2s';
        setTimeout(() => toast.remove(), 250);
      }, 3500);
    }

    function showConfirm(title, message, btnText, isDanger, onConfirm) {
      document.getElementById('confirm-title').innerText = title;
      document.getElementById('confirm-message').innerText = message;
      const btn = document.getElementById('confirm-btn');
      btn.innerText = btnText;
      btn.className = 'btn ' + (isDanger ? 'btn-danger' : 'btn-pink');
      btn.onclick = () => {
        closeConfirmModal();
        onConfirm();
      };
      document.getElementById('confirm-modal').classList.add('open');
    }

    function closeConfirmModal() {
      document.getElementById('confirm-modal').classList.remove('open');
    }

    function goTab(name, elem) {
      document.querySelectorAll('.nav-btn, .m-tab').forEach(b => b.classList.remove('active'));
      if (elem) elem.classList.add('active');
      document.querySelectorAll('.desktop-nav button').forEach(b => {
        if (b.innerText.toLowerCase().includes(name)) b.classList.add('active');
      });
      ['users', 'avatars', 'files', 'audit'].forEach(t => {
        const el = document.getElementById('tab-' + t);
        if (el) el.style.display = (t === name) ? 'block' : 'none';
      });
      if (name === 'users') loadUsers();
      if (name === 'avatars') loadAvatars();
      if (name === 'files') loadFiles();
      if (name === 'audit') loadAudit();
    }

    function setFilter(filter, elem) {
      currentFilter = filter;
      document.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
      if (elem) elem.classList.add('active');
      filterUsers();
    }

    function copyCode(code) {
      navigator.clipboard.writeText(code).then(() => {
        showToast('Pair code #' + code + ' copied to clipboard', 'success');
      }).catch(() => {
        showToast('Code: #' + code, 'info');
      });
    }

    async function loadUsers() {
      try {
        const res = await fetch('/api/admin/users');
        const json = await res.json();
        users = json.users || [];
        document.getElementById('stat-users').innerText = users.length;
        document.getElementById('stat-pairs').innerText = users.filter(u => u.partnerCode).length;
        document.getElementById('stat-verified').innerText = users.filter(u => u.blueTick || u.isVerified || u.greenTick).length;

        renderUserCards();
      } catch (e) {
        showToast('Error loading users: ' + e.message, 'error');
      }
    }

    function renderUserCards() {
      const container = document.getElementById('users-container');
      if (users.length === 0) {
        container.innerHTML = '<p style="color:var(--text-muted); padding:16px;">No users registered yet. Launch the Duora Android app to create an account.</p>';
        return;
      }

      const q = (document.getElementById('search-users')?.value || '').toLowerCase().trim();

      const filtered = users.filter(u => {
        if (currentFilter === 'online' && !u.isOnline) return false;
        if (currentFilter === 'paired' && !u.partnerCode) return false;
        if (currentFilter === 'verified' && !(u.blueTick || u.isVerified)) return false;
        if (currentFilter === 'admin' && !u.greenTick) return false;
        if (currentFilter === 'suspended' && !u.isBlocked) return false;

        if (q) {
          const hay = ((u.fullName || '') + ' ' + (u.userName || '') + ' ' + (u.username || '') + ' ' + (u.pairCode || '') + ' ' + (u.partnerCode || '')).toLowerCase();
          if (!hay.includes(q)) return false;
        }
        return true;
      });

      if (filtered.length === 0) {
        container.innerHTML = '<p style="color:var(--text-muted); padding:16px;">No matching accounts found for the current filter/search.</p>';
        return;
      }

      container.innerHTML = filtered.map(u => {
        const userHandle = u.username ? ('@' + u.username) : ('@' + (u.userName || 'user').toLowerCase().replace(/\s+/g, '_'));
        const displayName = (u.fullName && u.fullName !== 'You') ? u.fullName : ((u.userName && u.userName !== 'You') ? u.userName : ('User #' + u.pairCode));
        const partnerName = u.partnerName || (u.partnerCode ? ('#' + u.partnerCode) : 'Single');
        let avatarSrc = u.avatarUrl;
        if (!avatarSrc) {
          const match = liveCatalog.find(a => a.id === u.avatarId);
          avatarSrc = match ? (match.uploadThingUrl || match.imageUrl) : 'https://v61nfvcr2l.ufs.sh/f/KhRWxmH15t2WQ0vA94V9fO0xUu14o8YqB2eZlFwXp6Ldrk5T';
        }

        const blueBadge = (u.blueTick || u.isVerified)
          ? '<img class="badge-icon" src="/assets/blue_tick.png" title="Verified Blue Badge" onerror="this.style.display=\'none\'"/>'
          : '';
        const greenBadge = u.greenTick
          ? '<img class="badge-icon" src="/assets/green_tick.png" title="Official Admin Green Badge" onerror="this.style.display=\'none\'"/>'
          : '';

        let partnerRow = '';
        if (u.partnerCode) {
          partnerRow = '<div class="partner-box">' +
            '<div class="partner-info">' +
              '<svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg>' +
              '<span>' + partnerName + '</span>' +
              '<span style="font-family:JetBrains Mono, monospace; font-size:0.72rem; color:var(--text-muted);">#' + u.partnerCode + '</span>' +
            '</div>' +
            (u.partnerCode ? '<button class="btn btn-outline" style="padding:4px 8px; font-size:0.7rem; color:var(--hot-pink);" onclick="unpairUser(\'' + u.pairCode + '\')">Unlink</button>' : '') +
          '</div>';
        } else {
          partnerRow = '<div class="partner-box">' +
            '<span class="partner-unpaired">Single / Not Paired</span>' +
          '</div>';
        }

        return '<div class="user-card ' + (u.isBlocked ? 'blocked' : '') + '">' +
          '<div class="user-top">' +
            '<div class="avatar-wrap">' +
              '<img class="user-avatar" src="' + avatarSrc + '" onerror="this.src=\'https://v61nfvcr2l.ufs.sh/f/KhRWxmH15t2WQ0vA94V9fO0xUu14o8YqB2eZlFwXp6Ldrk5T\'"/>' +
              '<span class="status-badge ' + (u.isOnline ? 'online' : 'offline') + '" title="' + (u.isOnline ? 'Online' : 'Offline') + '"></span>' +
            '</div>' +
            '<div class="user-info">' +
              '<div class="user-name-row">' +
                '<span class="user-name">' + displayName + '</span>' +
                blueBadge +
                greenBadge +
                (u.isBlocked ? '<span style="color:var(--red); font-size:0.72rem; font-weight:800; background:rgba(239,68,68,0.15); padding:1px 6px; border-radius:4px;">SUSPENDED</span>' : '') +
              '</div>' +
              '<div class="user-meta-row">' +
                '<span class="user-handle">' + userHandle + '</span>' +
                '<span class="code-chip" onclick="copyCode(\\'' + u.pairCode + '\\')" title="Click to copy pair code">#' + u.pairCode +
                  '<svg viewBox="0 0 24 24" width="11" height="11" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>' +
                '</span>' +
              '</div>' +
            '</div>' +
          '</div>' +
          partnerRow +
          '<div class="user-actions">' +
            '<button class="btn ' + ((u.blueTick || u.isVerified) ? 'btn-outline' : 'btn-blue') + '" onclick="toggleTick(\\'' + u.pairCode + '\\', \\'blue\\', ' + (!(u.blueTick || u.isVerified)) + ')">' +
              '<svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>' +
              ((u.blueTick || u.isVerified) ? 'Remove Blue' : 'Verify Blue') +
            '</button>' +
            '<button class="btn ' + (u.greenTick ? 'btn-outline' : 'btn-green') + '" onclick="toggleTick(\\'' + u.pairCode + '\\', \\'green\\', ' + (!u.greenTick) + ')">' +
              '<svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>' +
              (u.greenTick ? 'Remove Admin' : 'Admin Tick') +
            '</button>' +
            '<button class="btn ' + (u.isBlocked ? 'btn-green' : 'btn-danger') + '" onclick="toggleBlock(\\'' + u.pairCode + '\\', ' + (!u.isBlocked) + ')">' +
              '<svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/></svg>' +
              (u.isBlocked ? 'Unsuspend' : 'Suspend') +
            '</button>' +
            '<button class="btn btn-outline" style="color:var(--red); border-color:rgba(239,68,68,0.3); min-width:38px; flex:0;" title="Delete User Permanently" onclick="deleteUser(\\'' + u.pairCode + '\\')">' +
              '<svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>' +
            '</button>' +
          '</div>' +
        '</div>';
      }).join('');
    }

    function filterUsers() {
      renderUserCards();
    }

    function unpairUser(pairCode) {
      showConfirm('Unlink Partner', 'Unlink user #' + pairCode + ' from their partner? Both users can re-pair anytime.', 'Unlink Partner', true, async () => {
        try {
          const res = await fetch('/api/admin/users/unpair', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ pairCode })
          });
          const data = await res.json();
          if (data.success) {
            showToast('User #' + pairCode + ' unlinked successfully', 'success');
            loadUsers();
          } else {
            showToast('Unlink failed: ' + (data.error || 'Unknown error'), 'error');
          }
        } catch (e) {
          showToast('Unlink failed: ' + e.message, 'error');
        }
      });
    }

    function deleteUser(pairCode) {
      showConfirm('Delete User', 'Delete user #' + pairCode + ' permanently? This cannot be undone.', 'Delete Permanently', true, async () => {
        try {
          const res = await fetch('/api/admin/users/delete', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ pairCode })
          });
          const data = await res.json();
          if (data.success) {
            showToast('User #' + pairCode + ' deleted', 'success');
            loadUsers();
          } else {
            showToast('Delete failed: ' + (data.error || 'Unknown error'), 'error');
          }
        } catch (e) {
          showToast('Delete failed: ' + e.message, 'error');
        }
      });
    }

    function wipeDatabase() {
      const conf = prompt('WARNING: This will permanently wipe ALL users, pairings, call logs, notes, and messages for a fresh registration! Type WIPE to confirm:');
      if (conf !== 'WIPE') return;
      fetch('/api/admin/database/clear-all', { method: 'POST' })
        .then(r => r.json())
        .then(data => {
          showToast(data.message || 'Database wiped successfully', 'success');
          loadUsers();
        })
        .catch(e => showToast('Wipe failed: ' + e.message, 'error'));
    }

    async function cleanDuplicates() {
      try {
        const res = await fetch('/api/admin/clean-duplicates', { method: 'POST' });
        const data = await res.json();
        showToast(data.message || 'Cleaned stale records', 'success');
        loadUsers();
      } catch (e) {
        showToast('Clean failed: ' + e.message, 'error');
      }
    }

    async function sendBroadcastNotice() {
      const title = document.getElementById('broadcast-title').value.trim();
      const message = document.getElementById('broadcast-msg').value.trim();
      const isAlert = document.getElementById('broadcast-alert').checked;
      if (!message) {
        showToast('Please enter a notice message', 'error');
        return;
      }
      try {
        const res = await fetch('/api/admin/broadcast', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ title, message, isAlert })
        });
        const data = await res.json();
        if (data.success) {
          showToast('Notice broadcasted to ' + (data.sentCount || 0) + ' active client(s)', 'success');
          document.getElementById('broadcast-title').value = '';
          document.getElementById('broadcast-msg').value = '';
          document.getElementById('broadcast-alert').checked = false;
        } else {
          showToast('Broadcast error: ' + (data.error || 'Failed'), 'error');
        }
      } catch (err) {
        showToast('Broadcast failed: ' + err.message, 'error');
      }
    }

    async function toggleTick(pairCode, type, status) {
      try {
        await fetch('/api/admin/users/verify', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ pairCode, type, status })
        });
        showToast('Updated ' + (type === 'blue' ? 'Blue' : 'Admin') + ' tick for #' + pairCode, 'success');
        loadUsers();
      } catch (e) {
        showToast('Failed to update verification tick', 'error');
      }
    }

    function toggleBlock(pairCode, isBlocked) {
      const action = isBlocked ? 'Suspend' : 'Unsuspend';
      showConfirm(action + ' User', 'Are you sure you want to ' + action.toLowerCase() + ' user #' + pairCode + '?', action, isBlocked, async () => {
        try {
          await fetch('/api/admin/users/block', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ pairCode, isBlocked })
          });
          showToast('User #' + pairCode + ' ' + (isBlocked ? 'suspended' : 'unsuspended'), 'success');
          loadUsers();
        } catch (e) {
          showToast('Failed to change suspension state', 'error');
        }
      });
    }

    async function loadAvatars() {
      try {
        const res = await fetch('/api/avatars');
        const json = await res.json();
        avatars = json.items || [];
        document.getElementById('stat-avatars').innerText = avatars.length;

        document.getElementById('avatar-grid').innerHTML = avatars.map(a =>
          '<div style="background:rgba(14,7,20,0.7); border:1px solid var(--card-border); border-radius:12px; padding:10px; text-align:center; transition:transform 0.2s;" onmouseenter="this.style.transform=\\'translateY(-2px)\\'" onmouseleave="this.style.transform=\\'none\\'">' +
            '<img src="' + a.imageUrl + '" style="width:100%; aspect-ratio:1; border-radius:10px; object-fit:cover;" onerror="this.src=\\'' + a.thumbnailUrl + '\\'"/>' +
            '<div style="font-size:0.78rem; font-weight:700; margin-top:6px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">' + a.name + '</div>' +
            '<div style="font-size:0.68rem; color:var(--hot-pink); font-family:JetBrains Mono, monospace; margin-top:2px;">' + a.id + '</div>' +
          '</div>'
        ).join('');
      } catch (e) {}
    }

    async function loadFiles() {
      try {
        const res = await fetch('/api/admin/files');
        const json = await res.json();
        const files = json.files || [];
        if (files.length === 0) {
          document.getElementById('files-list').innerHTML = '<p style="color:var(--text-muted); padding:10px;">No static files found.</p>';
          return;
        }
        document.getElementById('files-list').innerHTML = files.map(f =>
          '<div style="display:flex; justify-content:space-between; align-items:center; padding:10px 0; border-bottom:1px solid rgba(255,255,255,0.06); font-size:0.84rem;">' +
            '<div style="display:flex; align-items:center; gap:8px;">' +
              '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="var(--blue-tick)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>' +
              '<span>' + f.path + '</span>' +
            '</div>' +
            '<span style="color:var(--text-muted); font-family:JetBrains Mono, monospace; font-size:0.75rem;">' + f.size + '</span>' +
          '</div>'
        ).join('');
      } catch (e) {}
    }

    async function loadAudit() {
      try {
        const res = await fetch('/api/admin/audit');
        const json = await res.json();
        const logs = json.audit || [];
        if (logs.length === 0) {
          document.getElementById('audit-list').innerHTML = '<p style="color:var(--text-muted); padding:10px;">No audit events yet.</p>';
          return;
        }
        document.getElementById('audit-list').innerHTML = logs.map(a =>
          '<div style="padding:10px 0; border-bottom:1px solid rgba(255,255,255,0.06); font-size:0.82rem;">' +
            '<div style="font-weight:700; color:var(--hot-pink); display:flex; align-items:center; gap:6px;">' +
              '<svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>' +
              '<span>' + a.action + '</span> • <span style="color:var(--text);">' + a.target + '</span>' +
            '</div>' +
            '<div style="color:var(--text-muted); font-size:0.72rem; margin-top:2px;">' + a.timestamp + ' by ' + a.user + '</div>' +
          '</div>'
        ).join('');
      } catch (e) {}
    }

    function openUploadModal() { document.getElementById('upload-modal').classList.add('open'); }
    function closeUploadModal() { document.getElementById('upload-modal').classList.remove('open'); }

    async function submitAvatar() {
      const category = document.getElementById('new-cat').value;
      const name = document.getElementById('new-name').value.trim();
      const id = document.getElementById('new-id').value.trim();
      const fileInput = document.getElementById('new-file');
      if (!name || !id) { showToast('Please enter both Avatar Name and ID', 'error'); return; }

      let base64 = null;
      if (fileInput.files[0]) {
        base64 = await new Promise(res => {
          const r = new FileReader();
          r.onload = () => res(r.result);
          r.readAsDataURL(fileInput.files[0]);
        });
      }

      try {
        const res = await fetch('/api/admin/avatars', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ category, name, id, imageBase64: base64 })
        });
        const data = await res.json();
        if (data.success) {
          closeUploadModal();
          showToast('Avatar ' + name + ' saved successfully', 'success');
          loadAvatars();
        } else {
          showToast(data.error || 'Failed to save avatar', 'error');
        }
      } catch (err) {
        showToast('Upload error: ' + err.message, 'error');
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
                 COALESCE(username, '') AS "username", COALESCE(full_name, '') AS "fullName",
                 COALESCE(avatar_id, 'boy_001') AS "avatarId", COALESCE(avatar_url, '') AS "avatarUrl",
                 COALESCE(is_blocked, FALSE) AS "isBlocked", COALESCE(blue_tick, FALSE) AS "blueTick",
                 COALESCE(green_tick, FALSE) AS "greenTick", COALESCE(is_verified, FALSE) AS "isVerified",
                 last_seen AS "lastSeen"
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
              username: entry.username || "",
              fullName: entry.fullName || entry.userName || "You",
              avatarId: entry.avatarId || "boy_001",
              avatarUrl: entry.avatarUrl || "",
              isBlocked: !!entry.isBlocked,
              blueTick: !!entry.blueTick,
              greenTick: !!entry.greenTick,
              isVerified: !!entry.isVerified || !!entry.blueTick,
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
        if (type === "blue") {
          await pool.query(`UPDATE couples SET blue_tick = $1, is_verified = $1, updated_at = NOW() WHERE pair_code = $2;`, [!!status, code]);
        } else {
          await pool.query(`UPDATE couples SET ${col} = $1, updated_at = NOW() WHERE pair_code = $2;`, [!!status, code]);
        }
      }
      const entry = memoryClients ? memoryClients.get(code) : null;
      if (entry) {
        if (type === "green") entry.greenTick = !!status;
        else {
          entry.blueTick = !!status;
          entry.isVerified = !!status;
        }
      }

      if (forwardToTarget) {
        const clientBlue = type === "blue" ? !!status : !!entry?.blueTick;
        const clientGreen = type === "green" ? !!status : !!entry?.greenTick;
        forwardToTarget(code, {
          type: "registered",
          pairCode: code,
          blueTick: clientBlue,
          greenTick: clientGreen,
          isVerified: clientBlue
        });
        const pCode = entry?.partnerCode;
        if (pCode) {
          forwardToTarget(pCode, {
            type: "partner_status",
            isOnline: true,
            partnerCode: code,
            blueTick: clientBlue,
            greenTick: clientGreen,
            isVerified: clientBlue
          });
        }
      }

      logAudit("VERIFY_USER", `${type.toUpperCase()} tick set to ${status} for #${code}`);
      res.writeHead(200, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: true, code, type, status: !!status }));
    } catch (e) {
      res.writeHead(500, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: false, error: e.message }));
    }
  }

  // 7.1 Admin User Unpair
  if (pathname === "/api/admin/users/unpair" && req.method === "POST") {
    try {
      const body = await readJsonBody(req);
      const code = (body.pairCode || "").toUpperCase().trim();
      if (!code) {
        res.writeHead(400, { "Content-Type": "application/json" });
        return res.end(JSON.stringify({ success: false, error: "Missing pairCode" }));
      }
      let partnerCode = "";
      if (pool && isDbConnected) {
        const row = await pool.query("SELECT partner_code FROM couples WHERE pair_code = $1;", [code]);
        if (row.rows.length > 0) partnerCode = row.rows[0].partner_code || "";
        await pool.query("UPDATE couples SET partner_code = '', updated_at = NOW() WHERE pair_code = $1;", [code]);
        if (partnerCode) {
          await pool.query("UPDATE couples SET partner_code = '', updated_at = NOW() WHERE pair_code = $1;", [partnerCode]);
        }
      }
      const entry = memoryClients ? memoryClients.get(code) : null;
      if (entry) {
        if (!partnerCode) partnerCode = entry.partnerCode || "";
        entry.partnerCode = "";
      }
      if (partnerCode) {
        const partnerEntry = memoryClients ? memoryClients.get(partnerCode) : null;
        if (partnerEntry) partnerEntry.partnerCode = "";
        if (forwardToTarget) {
          forwardToTarget(partnerCode, { type: "unpaired", byCode: code, message: "Unpaired by Administrator." });
        }
      }
      if (forwardToTarget) {
        forwardToTarget(code, { type: "unpaired", byCode: "admin", message: "Unpaired by Administrator." });
      }
      logAudit("UNPAIR_USER", `User #${code} and #${partnerCode} unlinked`);
      res.writeHead(200, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: true, code, partnerCode }));
    } catch (e) {
      res.writeHead(500, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: false, error: e.message }));
    }
  }

  // 7.2 Admin User Delete
  if (pathname === "/api/admin/users/delete" && req.method === "POST") {
    try {
      const body = await readJsonBody(req);
      const code = (body.pairCode || "").toUpperCase().trim();
      if (!code) {
        res.writeHead(400, { "Content-Type": "application/json" });
        return res.end(JSON.stringify({ success: false, error: "Missing pairCode" }));
      }
      if (pool && isDbConnected) {
        await pool.query("DELETE FROM couples WHERE pair_code = $1;", [code]);
      }
      if (memoryClients && memoryClients.has(code)) {
        const entry = memoryClients.get(code);
        if (entry.sockets) {
          for (const s of entry.sockets) {
            try { s.close(); } catch (_) {}
          }
        }
        memoryClients.delete(code);
      }
      logAudit("DELETE_USER", `User #${code} deleted by admin`);
      res.writeHead(200, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: true, code }));
    } catch (e) {
      res.writeHead(500, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: false, error: e.message }));
    }
  }

  // 7.3 Admin Database Clear All (Fresh Registration)
  if (pathname === "/api/admin/database/clear-all" && req.method === "POST") {
    try {
      if (pool && isDbConnected) {
        await pool.query(`
          DELETE FROM pairing_requests;
          DELETE FROM call_logs;
          DELETE FROM shared_notes;
          DELETE FROM chat_messages;
          DELETE FROM couples;
        `);
      }
      if (memoryClients) memoryClients.clear();
      logAudit("WIPE_DATABASE", "All users, pairs, call logs and chat messages wiped for fresh start");
      res.writeHead(200, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: true, message: "Database completely cleared. Ready for fresh registration." }));
    } catch (e) {
      res.writeHead(500, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: false, error: e.message }));
    }
  }

  // 7.4 Clean Duplicate / Orphaned Unlinked User Codes
  if (pathname === "/api/admin/clean-duplicates" && req.method === "POST") {
    try {
      let removedCount = 0;
      if (pool && isDbConnected) {
        const resClean = await pool.query(`
          DELETE FROM couples
          WHERE partner_code = '' AND user_name = 'You' AND updated_at < NOW() - INTERVAL '2 hours';
        `);
        removedCount = resClean.rowCount || 0;
      }
      logAudit("CLEAN_DUPLICATES", `Cleaned ${removedCount} duplicate / abandoned records`);
      res.writeHead(200, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: true, removedCount, message: `Removed ${removedCount} stale duplicate records.` }));
    } catch (e) {
      res.writeHead(500, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: false, error: e.message }));
    }
  }
  if (pathname === "/api/admin/broadcast" && req.method === "POST") {
    try {
      const body = await readJsonBody(req);
      const { title, message, isAlert } = body;
      if (!message || !message.trim()) {
        res.writeHead(400, { "Content-Type": "application/json" });
        return res.end(JSON.stringify({ success: false, error: "Message cannot be empty" }));
      }

      const noticeId = "notice_" + Date.now();
      const timestamp = Date.now();
      const noticePayload = {
        type: isAlert ? "admin_alert" : "admin_notice",
        id: noticeId,
        title: (title || "Official Duora Notice").trim(),
        message: message.trim(),
        timestamp,
        isAlert: !!isAlert
      };

      let sentCount = 0;
      if (wss && wss.clients) {
        const msgStr = JSON.stringify(noticePayload);
        for (const ws of wss.clients) {
          if (ws.readyState === 1) { // WebSocket.OPEN
            try {
              ws.send(msgStr);
              sentCount++;
            } catch (_) {}
          }
        }
      }

      logAudit("ADMIN_BROADCAST", `${isAlert ? "ALERT" : "NOTICE"}: "${(title || '').slice(0, 30)}" to ${sentCount} clients`);
      res.writeHead(200, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: true, noticeId, sentCount }));
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
