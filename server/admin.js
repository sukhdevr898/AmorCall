// Duora Admin Web App & File Management Service
const fs = require("fs");
const path = require("path");

const ADMIN_PASSWORD = process.env.ADMIN_PASSWORD || "duora2026";
const ASSETS_ROOT = path.join(__dirname, "assets");
const AVATARS_ROOT = path.join(ASSETS_ROOT, "avatars");
const CATALOG_FILE = path.join(AVATARS_ROOT, "catalog.json");

// In-Memory audit log
const auditLog = [
  { action: "SYSTEM_INIT", target: "Avatar catalog initialized", timestamp: new Date().toISOString(), user: "system" }
];

// Helper to sanitize and prevent path traversal
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

function loadCatalogFromDisk() {
  try {
    if (fs.existsSync(CATALOG_FILE)) {
      const data = fs.readFileSync(CATALOG_FILE, "utf8");
      liveCatalog = JSON.parse(data);
      console.log(`[CATALOG] Loaded ${liveCatalog.length} avatars from ${CATALOG_FILE}`);
    } else {
      saveCatalogToDisk(DEFAULT_CATALOG);
    }
  } catch (e) {
    console.warn("[CATALOG] Failed to load catalog from disk, using defaults:", e.message);
  }
}

function saveCatalogToDisk(catalog) {
  try {
    liveCatalog = catalog;
    fs.mkdirSync(AVATARS_ROOT, { recursive: true });
    fs.writeFileSync(CATALOG_FILE, JSON.stringify(catalog, null, 2), "utf8");
    console.log(`[CATALOG] Saved ${catalog.length} avatars to disk`);
  } catch (e) {
    console.warn("[CATALOG] Failed to save catalog to disk:", e.message);
  }
}

// Initialize on module load
loadCatalogFromDisk();

function getCatalog() {
  return liveCatalog;
}

// Generate simple SVG QR code for pairing invitations
function generateQrSvg(code) {
  const safeCode = (code || "DUORA").toUpperCase().trim().slice(0, 8);
  // Return a stylish high-contrast SVG with finder patterns and central heart badge
  return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 250 250" width="250" height="250">
    <rect width="250" height="250" fill="#ffffff" rx="16"/>
    <!-- Top Left Finder -->
    <rect x="20" y="20" width="60" height="60" fill="#21102E" rx="8"/>
    <rect x="30" y="30" width="40" height="40" fill="#ffffff" rx="4"/>
    <rect x="40" y="40" width="20" height="20" fill="#21102E" rx="2"/>
    <!-- Top Right Finder -->
    <rect x="170" y="20" width="60" height="60" fill="#21102E" rx="8"/>
    <rect x="180" y="30" width="40" height="40" fill="#ffffff" rx="4"/>
    <rect x="190" y="40" width="20" height="20" fill="#21102E" rx="2"/>
    <!-- Bottom Left Finder -->
    <rect x="20" y="170" width="60" height="60" fill="#21102E" rx="8"/>
    <rect x="30" y="180" width="40" height="40" fill="#ffffff" rx="4"/>
    <rect x="40" y="190" width="20" height="20" fill="#21102E" rx="2"/>
    <!-- Timing dots -->
    <circle cx="95" cy="50" r="4" fill="#21102E"/>
    <circle cx="115" cy="50" r="4" fill="#21102E"/>
    <circle cx="135" cy="50" r="4" fill="#21102E"/>
    <circle cx="155" cy="50" r="4" fill="#21102E"/>
    <circle cx="50" cy="95" r="4" fill="#21102E"/>
    <circle cx="50" cy="115" r="4" fill="#21102E"/>
    <circle cx="50" cy="135" r="4" fill="#21102E"/>
    <circle cx="50" cy="155" r="4" fill="#21102E"/>
    <!-- Data Blocks -->
    <rect x="90" y="90" width="70" height="70" fill="#FFF0F5" rx="35" stroke="#FF2D8D" stroke-width="2"/>
    <path d="M125 135 C115 125 105 115 105 105 C105 97 113 90 120 95 C123 97 125 100 125 100 C125 100 127 97 130 95 C137 90 145 97 145 105 C145 115 135 125 125 135 Z" fill="#FF2D8D"/>
    <!-- Code label at bottom -->
    <text x="125" y="240" font-family="monospace" font-size="13" font-weight="bold" fill="#21102E" text-anchor="middle">#${safeCode}</text>
  </svg>`;
}

// Single-page HTML for Admin Dashboard
function getAdminHtml(baseUrl) {
  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Duora • Admin File & Avatar Manager</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@500;700&display=swap">
  <style>
    :root {
      --bg: #0D0714;
      --card: #180D24;
      --card-border: #2C1842;
      --text: #F8F5FB;
      --text-muted: #A396B2;
      --hot-pink: #FF2D8D;
      --pink-hover: #FF5D88;
      --purple: #7C3AED;
      --green: #10B981;
      --red: #EF4444;
      --yellow: #F59E0B;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      font-family: 'Plus Jakarta Sans', sans-serif;
      background: var(--bg);
      color: var(--text);
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }
    header {
      background: rgba(24, 13, 36, 0.85);
      backdrop-filter: blur(12px);
      border-bottom: 1px solid var(--card-border);
      padding: 14px 28px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      position: sticky;
      top: 0;
      z-index: 50;
    }
    .logo-badge {
      display: flex;
      align-items: center;
      gap: 12px;
      font-weight: 800;
      font-size: 1.25rem;
      letter-spacing: -0.02em;
    }
    .heart-icon {
      width: 32px;
      height: 32px;
      background: linear-gradient(135deg, var(--hot-pink), var(--purple));
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 16px;
    }
    .nav-tabs {
      display: flex;
      gap: 8px;
      background: rgba(44, 24, 66, 0.4);
      padding: 4px;
      border-radius: 12px;
      border: 1px solid var(--card-border);
    }
    .tab-btn {
      padding: 8px 16px;
      background: transparent;
      border: none;
      color: var(--text-muted);
      font-weight: 600;
      font-size: 0.88rem;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;
    }
    .tab-btn.active {
      background: var(--hot-pink);
      color: white;
      box-shadow: 0 4px 12px rgba(255, 45, 141, 0.35);
    }
    main {
      flex: 1;
      padding: 28px;
      max-width: 1300px;
      margin: 0 auto;
      width: 100%;
    }
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 16px;
      margin-bottom: 28px;
    }
    .stat-card {
      background: var(--card);
      border: 1px solid var(--card-border);
      border-radius: 16px;
      padding: 20px;
    }
    .stat-title { font-size: 0.8rem; font-weight: 600; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em; }
    .stat-val { font-size: 1.8rem; font-weight: 800; margin-top: 6px; color: #fff; }
    .card {
      background: var(--card);
      border: 1px solid var(--card-border);
      border-radius: 20px;
      padding: 24px;
      margin-bottom: 24px;
    }
    .card-head {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 20px;
    }
    .btn {
      padding: 10px 18px;
      border-radius: 12px;
      border: none;
      font-weight: 700;
      font-size: 0.88rem;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 8px;
      transition: all 0.2s;
    }
    .btn-primary { background: var(--hot-pink); color: white; }
    .btn-primary:hover { background: var(--pink-hover); transform: translateY(-1px); }
    .btn-secondary { background: rgba(255,255,255,0.08); color: var(--text); border: 1px solid var(--card-border); }
    .btn-danger { background: rgba(239, 68, 68, 0.15); color: var(--red); border: 1px solid rgba(239,68,68,0.3); }
    .avatar-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
      gap: 16px;
    }
    .avatar-item {
      background: rgba(13, 7, 20, 0.6);
      border: 1px solid var(--card-border);
      border-radius: 16px;
      padding: 14px;
      text-align: center;
      position: relative;
    }
    .avatar-img {
      width: 100%;
      aspect-ratio: 1;
      border-radius: 12px;
      object-fit: cover;
      background: #241438;
      margin-bottom: 10px;
    }
    .avatar-id {
      font-family: 'JetBrains Mono', monospace;
      font-size: 0.75rem;
      font-weight: 700;
      color: var(--hot-pink);
      background: rgba(255, 45, 141, 0.12);
      padding: 3px 8px;
      border-radius: 6px;
      display: inline-block;
      margin-bottom: 6px;
    }
    .avatar-name { font-size: 0.9rem; font-weight: 700; margin-bottom: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .avatar-tags { font-size: 0.72rem; color: var(--text-muted); margin-bottom: 10px; }
    table { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
    th { text-align: left; padding: 12px 16px; color: var(--text-muted); border-bottom: 1px solid var(--card-border); font-size: 0.78rem; text-transform: uppercase; }
    td { padding: 14px 16px; border-bottom: 1px solid rgba(44, 24, 66, 0.5); }
    tr:hover td { background: rgba(255, 255, 255, 0.02); }
    .modal-backdrop {
      display: none;
      position: fixed;
      inset: 0;
      background: rgba(0,0,0,0.7);
      backdrop-filter: blur(8px);
      z-index: 100;
      align-items: center;
      justify-content: center;
    }
    .modal-backdrop.open { display: flex; }
    .modal {
      background: var(--card);
      border: 1px solid var(--card-border);
      border-radius: 20px;
      width: 90%;
      max-width: 520px;
      padding: 28px;
    }
    .form-group { margin-bottom: 16px; }
    .form-group label { display: block; font-size: 0.8rem; font-weight: 600; color: var(--text-muted); margin-bottom: 6px; }
    .form-input, select, textarea {
      width: 100%;
      background: rgba(13, 7, 20, 0.8);
      border: 1px solid var(--card-border);
      border-radius: 10px;
      padding: 10px 14px;
      color: white;
      font-family: inherit;
    }
    .form-input:focus, select:focus, textarea:focus { border-color: var(--hot-pink); outline: none; }
  </style>
</head>
<body>
  <header>
    <div class="logo-badge">
      <div class="heart-icon">💕</div>
      <span>Duora Admin</span>
      <span style="font-size:0.75rem; background:rgba(16,185,129,0.15); color:var(--green); padding:3px 8px; border-radius:8px;">Online • v3.0.0</span>
    </div>
    <div class="nav-tabs">
      <button class="tab-btn active" onclick="switchTab('avatars')">Avatars</button>
      <button class="tab-btn" onclick="switchTab('files')">Files</button>
      <button class="tab-btn" onclick="switchTab('audit')">Audit Log</button>
    </div>
  </header>

  <main>
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-title">Total Remote Avatars</div>
        <div class="stat-val" id="stat-total">--</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">Boy Avatars</div>
        <div class="stat-val" id="stat-boys" style="color:#60A5FA;">--</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">Girl Avatars</div>
        <div class="stat-val" id="stat-girls" style="color:var(--hot-pink);">--</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">Storage Persistence</div>
        <div class="stat-val" style="color:var(--green); font-size:1.4rem;">Disk Persistent</div>
      </div>
    </div>

    <!-- TAB 1: AVATARS -->
    <div id="tab-avatars">
      <div class="card">
        <div class="card-head">
          <div>
            <h2 style="font-size:1.3rem;">Remote Avatar Library</h2>
            <p style="color:var(--text-muted); font-size:0.85rem; margin-top:4px;">Avatars served dynamically to Android clients with zero APK rebuild.</p>
          </div>
          <button class="btn btn-primary" onclick="openUploadModal()">+ Upload New Avatar</button>
        </div>
        <div class="avatar-grid" id="avatar-grid">Loading catalogue...</div>
      </div>
    </div>

    <!-- TAB 2: FILES -->
    <div id="tab-files" style="display:none;">
      <div class="card">
        <div class="card-head">
          <div>
            <h2 style="font-size:1.3rem;">Asset File Browser</h2>
            <p style="color:var(--text-muted); font-size:0.85rem; margin-top:4px;">Safely inspect, rename, and manage permitted assets.</p>
          </div>
        </div>
        <table>
          <thead>
            <tr>
              <th>File Path</th>
              <th>Type</th>
              <th>Size</th>
              <th>Modified</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody id="file-table-body">
            <tr><td colspan="5">Loading files...</td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- TAB 3: AUDIT -->
    <div id="tab-audit" style="display:none;">
      <div class="card">
        <h2 style="font-size:1.3rem; margin-bottom:14px;">Audit & Activity Trail</h2>
        <table>
          <thead>
            <tr>
              <th>Timestamp</th>
              <th>Action</th>
              <th>Target</th>
              <th>User</th>
            </tr>
          </thead>
          <tbody id="audit-table-body"></tbody>
        </table>
      </div>
    </div>
  </main>

  <!-- Upload Avatar Modal -->
  <div class="modal-backdrop" id="upload-modal">
    <div class="modal">
      <h3 style="margin-bottom:16px;">Upload New Curated Avatar</h3>
      <div class="form-group">
        <label>Avatar Category</label>
        <select id="new-cat">
          <option value="BOY">BOY</option>
          <option value="GIRL">GIRL</option>
        </select>
      </div>
      <div class="form-group">
        <label>Display Name</label>
        <input class="form-input" id="new-name" placeholder="e.g. Leo (Anime Star)" />
      </div>
      <div class="form-group">
        <label>Stable Avatar ID</label>
        <input class="form-input" id="new-id" placeholder="e.g. boy_011 or girl_011" />
      </div>
      <div class="form-group">
        <label>Tags (Comma separated)</label>
        <input class="form-input" id="new-tags" placeholder="e.g. Anime, Stylish, Cute" />
      </div>
      <div class="form-group">
        <label>Image File (WebP, PNG, JPG)</label>
        <input type="file" id="new-file" accept="image/*" />
      </div>
      <div style="display:flex; justify-content:flex-end; gap:10px; margin-top:24px;">
        <button class="btn btn-secondary" onclick="closeUploadModal()">Cancel</button>
        <button class="btn btn-primary" onclick="submitAvatar()">Save & Commit</button>
      </div>
    </div>
  </div>

  <script>
    let catalog = [];

    async function loadData() {
      try {
        const res = await fetch('/api/avatars');
        const json = await res.json();
        catalog = json.items || [];
        renderAvatars();
      } catch (e) {
        console.error(e);
      }
    }

    function renderAvatars() {
      const grid = document.getElementById('avatar-grid');
      const boys = catalog.filter(a => a.category === 'BOY');
      const girls = catalog.filter(a => a.category === 'GIRL');

      document.getElementById('stat-total').innerText = catalog.length;
      document.getElementById('stat-boys').innerText = boys.length;
      document.getElementById('stat-girls').innerText = girls.length;

      grid.innerHTML = catalog.map(a => \`
        <div class="avatar-item">
          <img class="avatar-img" src="\${a.imageUrl}" alt="\${a.name}" onerror="this.src='\${a.thumbnailUrl}'"/>
          <span class="avatar-id">\${a.id}</span>
          <div class="avatar-name">\${a.name}</div>
          <div class="avatar-tags">\${(a.tags || []).join(' • ')}</div>
          <button class="btn btn-danger" style="padding:4px 10px; font-size:0.75rem;" onclick="deleteAvatar('\${a.id}')">Delete</button>
        </div>
      \`).join('');
    }

    async function loadFiles() {
      try {
        const res = await fetch('/api/admin/files');
        const json = await res.json();
        const tbody = document.getElementById('file-table-body');
        tbody.innerHTML = (json.files || []).map(f => \`
          <tr>
            <td><strong>\${f.path}</strong></td>
            <td><span style="font-size:0.75rem; background:rgba(255,255,255,0.1); padding:2px 6px; border-radius:4px;">\${f.type}</span></td>
            <td>\${f.size}</td>
            <td>\${f.modified}</td>
            <td>
              <button class="btn btn-secondary" style="padding:4px 8px; font-size:0.75rem;" onclick="previewFile('\${f.path}')">View</button>
            </td>
          </tr>
        \`).join('');
      } catch (e) {}
    }

    async function loadAudit() {
      try {
        const res = await fetch('/api/admin/audit');
        const json = await res.json();
        const tbody = document.getElementById('audit-table-body');
        tbody.innerHTML = (json.audit || []).map(a => \`
          <tr>
            <td style="color:var(--text-muted); font-size:0.8rem;">\${a.timestamp}</td>
            <td><strong>\${a.action}</strong></td>
            <td>\${a.target}</td>
            <td>\${a.user}</td>
          </tr>
        \`).join('');
      } catch (e) {}
    }

    function switchTab(name) {
      document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      event.target.classList.add('active');
      document.getElementById('tab-avatars').style.display = name === 'avatars' ? 'block' : 'none';
      document.getElementById('tab-files').style.display = name === 'files' ? 'block' : 'none';
      document.getElementById('tab-audit').style.display = name === 'audit' ? 'block' : 'none';
      if (name === 'files') loadFiles();
      if (name === 'audit') loadAudit();
    }

    function openUploadModal() { document.getElementById('upload-modal').classList.add('open'); }
    function closeUploadModal() { document.getElementById('upload-modal').classList.remove('open'); }

    async function submitAvatar() {
      const category = document.getElementById('new-cat').value;
      const name = document.getElementById('new-name').value.trim();
      const id = document.getElementById('new-id').value.trim();
      const tags = document.getElementById('new-tags').value.split(',').map(t => t.trim()).filter(Boolean);
      const fileInput = document.getElementById('new-file');

      if (!name || !id) {
        alert('Please fill out Name and Avatar ID');
        return;
      }

      const reader = new FileReader();
      const file = fileInput.files[0];
      let base64 = null;
      if (file) {
        base64 = await new Promise(res => {
          const r = new FileReader();
          r.onload = () => res(r.result);
          r.readAsDataURL(file);
        });
      }

      const res = await fetch('/api/admin/avatars', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ category, name, id, tags, imageBase64: base64 })
      });
      const data = await res.json();
      if (data.success) {
        closeUploadModal();
        loadData();
      } else {
        alert(data.error || 'Failed to upload avatar');
      }
    }

    async function deleteAvatar(id) {
      if (!confirm('Are you sure you want to delete avatar ' + id + '?')) return;
      const res = await fetch('/api/admin/avatars/' + id, { method: 'DELETE' });
      const data = await res.json();
      if (data.success) {
        loadData();
      } else {
        alert(data.error || 'Failed to delete');
      }
    }

    loadData();
  </script>
</body>
</html>`;
}

// Router handler for Admin requests
async function handleAdminRoutes(req, res, parsedUrl, readJsonBody) {
  const pathname = parsedUrl.pathname;

  // 1. Pairing QR Endpoint (SVG)
  if (pathname.startsWith("/api/pairing/qr/")) {
    const code = pathname.replace(/^\/api\/pairing\/qr\//, "").replace(/\.svg$/, "").trim();
    const svg = generateQrSvg(code);
    res.writeHead(200, {
      "Content-Type": "image/svg+xml",
      "Cache-Control": "public, max-age=86400"
    });
    return res.end(svg);
  }

  // 2. Admin UI at /admin
  if (pathname === "/admin" || pathname === "/admin/") {
    const host = req.headers.host || "localhost:3000";
    const proto = req.headers["x-forwarded-proto"] || "http";
    res.writeHead(200, { "Content-Type": "text/html; charset=utf-8" });
    return res.end(getAdminHtml(`${proto}://${host}`));
  }

  // 3. Admin Files API
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

  // 4. Admin Avatars API (Add)
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
        // Fallback: copy from boy_001 or girl_001 if available
        const defaultSample = path.join(AVATARS_ROOT, subDir, `${subDir === "girls" ? "girl_001" : "boy_001"}.webp`);
        if (fs.existsSync(defaultSample)) {
          fs.copyFileSync(defaultSample, targetFile);
        }
      }

      // Update in-memory and disk catalogue
      const existingIdx = liveCatalog.findIndex(a => a.id === id);
      const newEntry = {
        id,
        name,
        category: category.toUpperCase(),
        tags: tags || ["Custom"],
        assetPath: `${subDir}/${filename}`
      };

      if (existingIdx >= 0) {
        liveCatalog[existingIdx] = newEntry;
      } else {
        liveCatalog.push(newEntry);
      }

      saveCatalogToDisk(liveCatalog);

      auditLog.unshift({
        action: existingIdx >= 0 ? "UPDATE_AVATAR" : "CREATE_AVATAR",
        target: `${id} (${name})`,
        timestamp: new Date().toISOString(),
        user: "admin"
      });

      res.writeHead(200, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: true, avatar: newEntry }));
    } catch (e) {
      res.writeHead(500, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: false, error: e.message }));
    }
  }

  // 5. Admin Avatars API (Delete)
  if (pathname.startsWith("/api/admin/avatars/") && req.method === "DELETE") {
    const avatarId = pathname.replace("/api/admin/avatars/", "").trim();
    const idx = liveCatalog.findIndex(a => a.id === avatarId);
    if (idx === -1) {
      res.writeHead(404, { "Content-Type": "application/json" });
      return res.end(JSON.stringify({ success: false, error: "Avatar not found" }));
    }

    const removed = liveCatalog.splice(idx, 1)[0];
    saveCatalogToDisk(liveCatalog);

    auditLog.unshift({
      action: "DELETE_AVATAR",
      target: `${avatarId} (${removed.name})`,
      timestamp: new Date().toISOString(),
      user: "admin"
    });

    res.writeHead(200, { "Content-Type": "application/json" });
    return res.end(JSON.stringify({ success: true, message: "Avatar deleted" }));
  }

  // 6. Admin Audit Log API
  if (pathname === "/api/admin/audit" && req.method === "GET") {
    res.writeHead(200, { "Content-Type": "application/json" });
    return res.end(JSON.stringify({ success: true, audit: auditLog }));
  }

  return false;
}

module.exports = {
  getCatalog,
  handleAdminRoutes,
  generateQrSvg
};
