# Duora Backend & Server Architecture (`server/`)

## 1. Overview
The **Duora Server** is a high-performance, resilient backend service designed for real-time couple signaling, WebRTC session negotiation, chat synchronization, 24-hour shared notes persistence, and system telemetry.

### Technology Stack
- **Runtime**: Node.js (v18+ LTS)
- **Framework & API**: Node.js HTTP + REST endpoints + WebSocket (`ws`)
- **Database Engine**: PostgreSQL with automated schema migration + In-Memory zero-crash fallback buffer
- **Communication Protocol**: Full-duplex JSON WebSockets + RESTful HTTP endpoints
- **Location**: Standalone root directory `/server`

---

## 2. Database Schema (PostgreSQL)

When `DATABASE_URL` or `POSTGRES_URL` is configured, PostgreSQL connects automatically with SSL support. The service initializes 4 persistent tables:

### 2.1 `couples` Table
```sql
CREATE TABLE IF NOT EXISTS couples (
  pair_code VARCHAR(32) PRIMARY KEY,
  partner_code VARCHAR(32),
  user_name VARCHAR(128),
  partner_status TEXT DEFAULT 'Thinking of you always ❤️',
  last_seen BIGINT,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 2.2 `chat_messages` Table
```sql
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
```

### 2.3 `shared_notes` (24-Hour Couple Notes) Table
```sql
CREATE TABLE IF NOT EXISTS shared_notes (
  id VARCHAR(64) PRIMARY KEY,
  author_code VARCHAR(32),
  recipient_code VARCHAR(32),
  author_name VARCHAR(128),
  content TEXT,
  created_at BIGINT,
  is_deleted BOOLEAN DEFAULT FALSE
);
```

### 2.4 `call_logs` Table
```sql
CREATE TABLE IF NOT EXISTS call_logs (
  id VARCHAR(64) PRIMARY KEY,
  from_code VARCHAR(32),
  to_code VARCHAR(32),
  call_type VARCHAR(16),
  duration_seconds INT DEFAULT 0,
  created_at BIGINT
);
```

---

## 3. REST API Endpoints

All endpoints support JSON responses and CORS headers:

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/health` / `/api/v1/health` | Service health status, active peers count, database connectivity status |
| `GET` | `/ready` / `/api/v1/ready` | Readiness check for containerized deployments |
| `GET` | `/api/db-status` | Detailed database status (PostgreSQL vs In-Memory engine, tables list) |
| `GET` | `/api/chat` | Retrieve chat message history between `myCode` and `partnerCode` |
| `GET` | `/api/note` | Fetch active (unexpired, <24h) shared note between paired partners |
| `GET` | `/api/v1/pairing/verify` | Verify existence and online status of a given pair code |
| `GET` | `/api/v1/calls` | Fetch recent call telemetry and active call status |

---

## 4. WebSocket Real-Time Protocols

WebSocket endpoint: `ws://<host>:<port>`

### 4.1 Client-to-Server Events
- **`register`**: Register device socket under a pair code and target partner code:
  `{ type: "register", pairCode: "ABC123", partnerPairCode: "XYZ789", userName: "Romeo" }`
- **`chat_message`**: Send a message to paired partner:
  `{ type: "chat_message", id: "uuid", to: "XYZ789", from: "ABC123", content: "Love you!", timestamp: 123456789 }`
- **`typing`**: Broadcast real-time typing indicator:
  `{ type: "typing", to: "XYZ789", isTyping: true }`
- **`couple_note`**: Broadcast a 24-hour shared note:
  `{ type: "couple_note", id: "uuid", to: "XYZ789", content: "Thinking of you", authorName: "Romeo" }`
- **`delete_couple_note`**: Soft-delete a shared note:
  `{ type: "delete_couple_note", id: "uuid", to: "XYZ789" }`
- **`call_offer` / `call_answer` / `ice_candidate`**: WebRTC signaling for audio and video calls.
- **`call_end` / `call_decline`**: Terminate or reject an incoming call.

### 4.2 Server-to-Client Events
- **`registered`**: Confirmation of device registration and DB state.
- **`partner_status`**: Real-time notification of partner online/offline state.
- **`chat_message` / `chat_ack`**: Forwarded messages and delivery confirmations.
- **`couple_note`**: Delivered active shared notes.

---

## 5. Security & Deployment

1. **Environment Variables**:
   - `PORT`: Server listen port (default `3000`).
   - `DATABASE_URL`: PostgreSQL connection string.
2. **Redacted Logging**: All logs omit private credentials, passwords, and message contents.
3. **Graceful Fallback**: If PostgreSQL is temporarily unreachable, memory buffers guarantee continuous, crash-free operation.
