# AmourCall Self-Hosted Backend Server ❤️

An ultra-lightweight, self-hosted Node.js WebSocket signaling server for the **AmourCall** Android app.

---

## 🚀 Quick Start (Local Run)

### 1. Requirements
- Node.js (version 18 or newer)
- npm

### 2. Install & Run
```bash
cd server
npm install
npm start
```

The server will start on port `3000`:
- **Web Dashboard**: `http://localhost:3000`
- **Health Check**: `http://localhost:3000/health`
- **WebSocket URL**: `ws://localhost:3000`

---

## 📱 Connecting to the Android App

In the Android app:
1. Tap the **Link / Pairing** icon in the top right.
2. Under **Server Configuration**, enter your server address:
   - **Android Emulator**: `ws://10.0.2.2:3000`
   - **Same Wi-Fi Network**: `ws://<YOUR_COMPUTER_LOCAL_IP>:3000` (e.g. `ws://192.168.1.15:3000`)
   - **Cloud Hosted**: `wss://your-amourcall-server.onrender.com`
3. Tap **Save & Connect**. The indicator will turn **Green (Connected 🟢)**!

---

## ☁️ 1-Click Free Cloud Deployment

### Option A: Render.com (Recommended - Free)
1. Fork or push this repository to GitHub.
2. In [Render Dashboard](https://dashboard.render.com), create a **New Web Service**.
3. Set:
   - Root Directory: `server`
   - Build Command: `npm install`
   - Start Command: `node server.js`
4. Copy your service URL (e.g., `https://amourcall-xxx.onrender.com`).
5. In the Android app, enter: `wss://amourcall-xxx.onrender.com`.

### Option B: Railway.app / Fly.io / VPS
Simply set:
- Port: `3000` or `$PORT`
- Start Command: `node server.js`

---

## 🔒 Security & Privacy
- Zero persistent storage of chat or call content.
- Devices are paired directly via private 6-character Love Codes.
- In-flight messages and WebSockets use TLS (`wss://`) when deployed to cloud.

Developer: **Sukhdev Singh** ([@sukhdevr898](https://github.com/sukhdevr898))
