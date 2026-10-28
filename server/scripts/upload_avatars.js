const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');
const { UTApi } = require('uploadthing/server');

// Safely load environment variables from uncommitted local .env if present
try {
  const envPath = path.resolve(__dirname, '..', '..', '.env');
  if (fs.existsSync(envPath)) {
    const envContent = fs.readFileSync(envPath, 'utf8');
    for (const line of envContent.split('\n')) {
      const trimmed = line.trim();
      if (!trimmed || trimmed.startsWith('#')) continue;
      const eqIdx = trimmed.indexOf('=');
      if (eqIdx > 0) {
        const k = trimmed.substring(0, eqIdx).trim();
        const v = trimmed.substring(eqIdx + 1).trim().replace(/^['"]|['"]$/g, '');
        if (!process.env[k]) process.env[k] = v;
      }
    }
  }
} catch (_) {}

const UPLOADTHING_TOKEN = process.env.UPLOADTHING_TOKEN || '';

const utapi = new UTApi({ token: UPLOADTHING_TOKEN });

const BASE_DIR = path.resolve(__dirname, '..');
const ASSETS_DIR = path.join(BASE_DIR, 'assets', 'avatars');
const PUBLIC_ZIP_DIR = path.resolve(BASE_DIR, '..', 'public', 'assets', 'aistudio');

const zipSpecs = [
  {
    zip: path.join(PUBLIC_ZIP_DIR, 'Admin_avatars_96b11a64.zip'),
    targetSubdir: 'admin',
    category: 'ADMIN',
    prefix: 'admin_',
    tags: ['Admin', 'Official', 'Staff']
  },
  {
    zip: path.join(PUBLIC_ZIP_DIR, 'boys_Avatar_4e7d9ec8.zip'),
    targetSubdir: 'boys_pack',
    category: 'BOY',
    prefix: 'boy_pack_',
    tags: ['Boy', 'Anime', 'Stylish']
  },
  {
    zip: path.join(PUBLIC_ZIP_DIR, '24_circular_avatars_0be21faf.zip'),
    targetSubdir: 'circular_24',
    category: 'GIRL',
    prefix: 'girl_circ_',
    tags: ['Girl', 'Cute', 'Aesthetic']
  },
  {
    zip: path.join(PUBLIC_ZIP_DIR, '20_circular_avatars_33c49423.zip'),
    targetSubdir: 'circular_20',
    category: 'COUPLE',
    prefix: 'couple_circ_',
    tags: ['Couple', 'Romantic', 'Love']
  }
];

async function main() {
  console.log('🚀 Starting Avatar Extraction and UploadThing Sync...');

  if (!fs.existsSync(ASSETS_DIR)) {
    fs.mkdirSync(ASSETS_DIR, { recursive: true });
  }

  const catalog = [];

  for (const spec of zipSpecs) {
    if (!fs.existsSync(spec.zip)) {
      console.warn(`⚠️ Zip file not found: ${spec.zip}`);
      continue;
    }

    const outDir = path.join(ASSETS_DIR, spec.targetSubdir);
    if (!fs.existsSync(outDir)) {
      fs.mkdirSync(outDir, { recursive: true });
    }

    console.log(`📦 Unzipping ${path.basename(spec.zip)} into ${spec.targetSubdir}...`);
    try {
      execSync(`unzip -o -q "${spec.zip}" -d "${outDir}"`);
    } catch (e) {
      console.error(`Error unzipping ${spec.zip}:`, e.message);
      continue;
    }

    const files = fs.readdirSync(outDir).filter(f => f.toLowerCase().endsWith('.png') || f.toLowerCase().endsWith('.webp') || f.toLowerCase().endsWith('.jpg'));
    console.log(`Found ${files.length} images in ${spec.targetSubdir}`);

    // Batch upload to UploadThing in chunks of 5 to avoid overloading
    const CHUNK_SIZE = 5;
    for (let i = 0; i < files.length; i += CHUNK_SIZE) {
      const chunk = files.slice(i, i + CHUNK_SIZE);
      const fileObjects = [];
      const fileMeta = [];

      for (const fileName of chunk) {
        const filePath = path.join(outDir, fileName);
        const buffer = fs.readFileSync(filePath);
        const cleanName = `${spec.prefix}${fileName.replace(/[^a-zA-Z0-9._-]/g, '_')}`;
        const fileObj = new File([buffer], cleanName, { type: 'image/png' });
        fileObjects.push(fileObj);
        fileMeta.push({
          fileName,
          cleanName,
          baseId: path.basename(fileName, path.extname(fileName)),
          localRelPath: `/assets/avatars/${spec.targetSubdir}/${fileName}`
        });
      }

      console.log(`  ⬆️ Uploading batch ${Math.floor(i / CHUNK_SIZE) + 1} (${chunk.join(', ')})...`);
      let uploadResults = [];
      try {
        uploadResults = await utapi.uploadFiles(fileObjects);
      } catch (err) {
        console.error(`  ❌ Upload failed for chunk:`, err.message);
      }

      for (let j = 0; j < fileMeta.length; j++) {
        const meta = fileMeta[j];
        const res = uploadResults[j];
        let remoteUrl = null;
        if (res && res.data) {
          remoteUrl = res.data.ufsUrl || res.data.url;
        }

        const id = `${spec.prefix}${meta.baseId}`.toLowerCase();
        let displayName = meta.baseId.replace(/_/g, ' ').replace(/-/g, ' ');
        displayName = displayName.charAt(0).toUpperCase() + displayName.slice(1);

        catalog.push({
          id,
          name: displayName,
          category: spec.category,
          imageUrl: remoteUrl || meta.localRelPath,
          localUrl: meta.localRelPath,
          uploadThingUrl: remoteUrl,
          tags: spec.tags
        });
      }
    }
  }

  const catalogPath = path.join(BASE_DIR, 'avatar_catalog.json');
  fs.writeFileSync(catalogPath, JSON.stringify(catalog, null, 2), 'utf8');
  console.log(`✅ UploadThing Sync Complete! Catalog saved to ${catalogPath} (${catalog.length} avatars registered)`);
}

main().catch(err => {
  console.error('Fatal error:', err);
  process.exit(1);
});
