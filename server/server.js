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

// Helper to broadcast message to all active sockets for a given pair code
function forwardToTarget(targetCode, messageObj) {
  if (!targetCode) return false;
  const targetEntry = clients.get(targetCode.toUpperCase().trim());
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

  ws.on("message", (rawMessage) => {
    try {
      const data = JSON.parse(rawMessage.toString());

      switch (data.type) {
        case "register": {
          const code = (data.pairCode || "").toUpperCase().trim();
          const partnerCode = (data.partnerPairCode || "").toUpperCase().trim();

          if (!code) return;

          registeredCode = code;
          let entry = clients.get(code);
          if (!entry) {
            entry = { sockets: new Set(), partnerCode, connectedAt: Date.now() };
            clients.set(code, entry);
          }
          entry.sockets.add(ws);
          if (partnerCode) {
            entry.partnerCode = partnerCode;
          }

          console.log(`[+] Device connected for code #${code} (Total open sockets: ${entry.sockets.size}). Target partner: #${partnerCode}`);

          // Notify self of registration confirmation
          ws.send(JSON.stringify({ type: "registered", pairCode: code }));

          // Check if partner is currently online
          const partnerEntry = clients.get(partnerCode);
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
          break;
        }

        case "check_partner": {
          const target = (data.partnerCode || "").toUpperCase().trim();
          const targetEntry = clients.get(target);
          const isOnline = !!(targetEntry && targetEntry.sockets && targetEntry.sockets.size > 0);
          ws.send(JSON.stringify({
            type: "partner_status",
            isOnline,
            partnerCode: target
          }));
          break;
        }

        case "call_initiate": {
          const target = (data.to || "").toUpperCase().trim();
          console.log(`[CALL] ${data.from} calling ${target} (${data.callType})`);

          const delivered = forwardToTarget(target, {
            type: "call_incoming",
            from: data.from,
            callerName: data.callerName || "My Love",
            callType: data.callType || "VOICE"
          });

          if (!delivered) {
            ws.send(JSON.stringify({
              type: "call_unavailable",
              reason: "Partner is currently offline or unreachable."
            }));
          }
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
          break;
        }

        case "audio_chunk": {
          // Relay real voice audio PCM chunk to partner with minimal latency
          const target = (data.to || "").toUpperCase().trim();
          forwardToTarget(target, {
            type: "audio_chunk",
            from: data.from,
            audioData: data.audioData
          });
          break;
        }

        case "video_frame": {
          // Relay real camera video frame to partner
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

        case "webrtc_signal": {
          const target = (data.to || "").toUpperCase().trim();
          forwardToTarget(target, {
            type: "webrtc_signal",
            from: data.from,
            payload: data.payload
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
      const entry = clients.get(registeredCode);
      if (entry) {
        entry.sockets.delete(ws);
        console.log(`[-] Socket closed for #${registeredCode}. Remaining sockets: ${entry.sockets.size}`);

        if (entry.sockets.size === 0) {
          console.log(`[-] Device #${registeredCode} fully disconnected.`);
          const partnerCode = entry.partnerCode;
          clients.delete(registeredCode);

          // Notify partner if online
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

// Keep-alive heartbeat: ping all open connections every 25 seconds
setInterval(() => {
  const pingPayload = JSON.stringify({ type: "ping" });
  for (const entry of clients.values()) {
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
  console.log(`🚀 AmourCall Signaling Server listening on port ${PORT}`);
  console.log(`❤️  Ready to connect lovers!`);
});
