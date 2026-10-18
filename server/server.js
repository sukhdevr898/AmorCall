const http = require("http");
const { WebSocketServer, WebSocket } = require("ws");

const PORT = process.env.PORT || 3000;

// In-memory registry of connected clients by Love Code: pairCode -> { ws, partnerCode, connectedAt }
const clients = new Map();

const server = http.createServer((req, res) => {
  if (req.url === "/health") {
    res.writeHead(200, { "Content-Type": "application/json" });
    return res.end(JSON.stringify({ status: "ok", activePeers: clients.size, time: new Date().toISOString() }));
  }

  res.writeHead(200, { "Content-Type": "text/html" });
  res.end(`
    <!DOCTYPE html>
    <html>
      <head>
        <title>AmourCall Signaling Server</title>
        <style>
          body { font-family: system-ui, sans-serif; background: #0E0712; color: #FFF1F2; display: flex; flex-direction: column; align-items: center; justify-content: center; height: 90vh; margin: 0; }
          .card { background: #261023; border: 1px solid #4A1E43; padding: 2rem; border-radius: 20px; text-align: center; max-width: 420px; box-shadow: 0 10px 30px rgba(0,0,0,0.5); }
          h1 { color: #F43F5E; margin-bottom: 0.5rem; }
          .badge { background: #10B981; color: #fff; padding: 4px 12px; border-radius: 20px; font-size: 0.85rem; display: inline-block; margin-bottom: 1rem; }
          p { color: #D4A3B8; font-size: 0.95rem; line-height: 1.5; }
          .stat { color: #FBBF24; font-weight: bold; font-size: 1.4rem; margin: 1rem 0; }
          .footer { margin-top: 1.5rem; font-size: 0.8rem; color: #888; }
        </style>
      </head>
      <body>
        <div class="card">
          <h1>AmourCall Server ❤️</h1>
          <div class="badge">● Online & Ready</div>
          <p>Dedicated private calling & signaling relay for AmourCall couple hotline.</p>
          <div class="stat">Connected Devices: ${clients.size}</div>
          <p>WebSocket endpoint: <code>ws://${req.headers.host || "localhost:" + PORT}</code></p>
          <div class="footer">Developer: Sukhdev Singh (@sukhdevr898)</div>
        </div>
      </body>
    </html>
  `);
});

const wss = new WebSocketServer({ server });

wss.on("connection", (ws, req) => {
  let registeredCode = null;

  ws.on("message", (rawMessage) => {
    try {
      const data = JSON.parse(rawMessage.toString());

      switch (data.type) {
        case "register": {
          const code = (data.pairCode || "").toUpperCase().trim();
          const partnerCode = (data.partnerPairCode || "").toUpperCase().trim();

          if (!code) return;

          registeredCode = code;
          clients.set(code, { ws, partnerCode, connectedAt: Date.now() });

          console.log(`[+] Device registered with code #${code}. Target partner: #${partnerCode}`);

          // Notify self of registration success
          ws.send(JSON.stringify({ type: "registered", pairCode: code }));

          // Check if partner is currently online
          const partnerSession = clients.get(partnerCode);
          const isPartnerOnline = !!(partnerSession && partnerSession.ws.readyState === WebSocket.OPEN);

          ws.send(JSON.stringify({
            type: "partner_status",
            isOnline: isPartnerOnline,
            partnerCode
          }));

          if (isPartnerOnline) {
            partnerSession.ws.send(JSON.stringify({
              type: "partner_status",
              isOnline: true,
              partnerCode: code
            }));
          }
          break;
        }

        case "call_initiate": {
          const target = (data.to || "").toUpperCase().trim();
          const targetSession = clients.get(target);

          console.log(`[CALL] ${data.from} calling ${target} (${data.callType})`);

          if (targetSession && targetSession.ws.readyState === WebSocket.OPEN) {
            targetSession.ws.send(JSON.stringify({
              type: "call_incoming",
              from: data.from,
              callerName: data.callerName || "My Love",
              callType: data.callType || "VOICE"
            }));
          } else {
            // Partner not connected right now
            ws.send(JSON.stringify({
              type: "call_unavailable",
              reason: "Partner is currently offline or unlinked."
            }));
          }
          break;
        }

        case "call_accept": {
          const target = (data.to || "").toUpperCase().trim();
          const targetSession = clients.get(target);

          if (targetSession && targetSession.ws.readyState === WebSocket.OPEN) {
            targetSession.ws.send(JSON.stringify({
              type: "call_accepted",
              from: data.from
            }));
          }
          break;
        }

        case "call_decline": {
          const target = (data.to || "").toUpperCase().trim();
          const targetSession = clients.get(target);

          if (targetSession && targetSession.ws.readyState === WebSocket.OPEN) {
            targetSession.ws.send(JSON.stringify({
              type: "call_declined",
              from: data.from
            }));
          }
          break;
        }

        case "call_end": {
          const target = (data.to || "").toUpperCase().trim();
          const targetSession = clients.get(target);

          if (targetSession && targetSession.ws.readyState === WebSocket.OPEN) {
            targetSession.ws.send(JSON.stringify({
              type: "call_ended",
              from: data.from
            }));
          }
          break;
        }

        case "heart_reaction": {
          const target = (data.to || "").toUpperCase().trim();
          const targetSession = clients.get(target);

          if (targetSession && targetSession.ws.readyState === WebSocket.OPEN) {
            targetSession.ws.send(JSON.stringify({
              type: "heart_reaction",
              from: data.from
            }));
          }
          break;
        }

        case "webrtc_signal": {
          const target = (data.to || "").toUpperCase().trim();
          const targetSession = clients.get(target);

          if (targetSession && targetSession.ws.readyState === WebSocket.OPEN) {
            targetSession.ws.send(JSON.stringify({
              type: "webrtc_signal",
              from: data.from,
              payload: data.payload
            }));
          }
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
      console.log(`[-] Device #${registeredCode} disconnected.`);
      const current = clients.get(registeredCode);
      if (current) {
        const partnerCode = current.partnerCode;
        clients.delete(registeredCode);

        // Notify partner if online
        const partnerSession = clients.get(partnerCode);
        if (partnerSession && partnerSession.ws.readyState === WebSocket.OPEN) {
          partnerSession.ws.send(JSON.stringify({
            type: "partner_status",
            isOnline: false,
            partnerCode: registeredCode
          }));
        }
      }
    }
  });

  ws.on("error", (err) => {
    console.error(`WebSocket error for ${registeredCode}:`, err.message);
  });
});

server.listen(PORT, "0.0.0.0", () => {
  console.log(`🚀 AmourCall Signaling Server listening on port ${PORT}`);
  console.log(`❤️  Ready to connect lovers!`);
});
