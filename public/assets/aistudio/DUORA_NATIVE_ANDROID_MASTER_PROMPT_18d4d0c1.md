# MASTER IMPLEMENTATION PROMPT — DUORA NATIVE ANDROID

Act as a senior Android architect, Kotlin engineer, backend engineer, UI/UX designer, security engineer, and QA lead. Inspect the existing repository, current `server/` code, database migrations, Gradle setup, tests, and existing `ui.md`, `server.md`, and `logics.md` before changing anything. Then implement the application, not merely a plan or mockup. Preserve working code, avoid destructive migrations, and never claim a feature or test works unless it actually does.

## 1. Product

- Name: **Duora**
- Tagline: **Two hearts. One private space.**
- Private communication app for exactly two mutually paired people.
- Native Android app: **Kotlin + Jetpack Compose + Material 3**.
- Architecture: ViewModel, Coroutines/Flow, repository/domain/UI separation.
- Backend: **Node.js + TypeScript + Express + Socket.IO + PostgreSQL + Prisma**.
- Keep backend in root `server/`; it is separate from Android code.
- No public dating feed, swipe discovery, nearby strangers, public profiles, or group chat.

## 2. Design references and brand

Use the user's three reference images as visual inspiration: rounded romantic cards, soft onboarding backgrounds, a polished top bar with a small circular avatar, simple bottom navigation, and clean chat bubbles. Do not copy dating-app discovery features or invent fake partner photos/statuses. Use initials or tasteful abstract artwork until real user images exist.

### Exact brand colors
- **Raspberry: `#5B3DF5`** — primary actions, selected navigation, key accents.
- **Pale Sky: `#E8DEFF`** — soft backgrounds, cards, chips, onboarding accents.

Suggested supporting tokens:
- Light background `#FBF9FF`; surface `#FFFFFF`; primary text `#241B3B`; secondary text `#716985`; outline `#D9CFF5`.
- Dark background `#100D18`; surface `#1B1726`; elevated surface `#262035`; primary text `#F8F5FF`; secondary text `#C0B7D2`.
- Add accessible success/warning/error colors.

Use Material 3 theme tokens, not hard-coded colors in screens. Support System/Light/Dark. Check contrast. Use rounded cards (16–26 dp), 4/8/12/16/20/24/32 dp spacing, subtle 150–250 ms transitions, 48 dp touch targets, TalkBack labels, font scaling, keyboard/safe-area insets, and proper Android back behavior. Avoid excessive gradients and emoji-as-icons.

## 3. Native Android architecture

Use compatible stable versions after inspecting existing Gradle/AGP/Kotlin configuration. Recommended:
- Jetpack Compose, Material 3, Navigation Compose, ViewModel, Coroutines/Flow
- Retrofit + OkHttp (or Ktor; choose one)
- Socket.IO Java client (or a compatible WebSocket solution)
- Room for cached messages and offline outbox
- DataStore for non-secret preferences
- Android Keystore-backed storage for refresh credentials
- WorkManager for deferrable sync only, not real-time call guarantees
- Firebase Cloud Messaging
- CameraX/maintained QR scanner if QR is implemented
- WebRTC Android library for real media
- AndroidX Biometric if app lock is included

Keep a clear feature structure: startup, onboarding, auth, home, pairing, chat, calls, profile, settings, privacy, notifications, and about. Adapt existing structure rather than duplicating it. Do not use React Native, Expo, or a WebView wrapper.

## 4. Startup and first-run onboarding

Startup:
1. Native splash with Duora branding.
2. Initialize DataStore, secure credentials, and Room.
3. Restore session safely.
4. Check update policy with a timeout if configured.
5. Show onboarding only if first-run completion is not stored.
6. Route to login if unauthenticated, pairing if authenticated/unpaired, or Home if authenticated/paired.
7. If offline, preserve safe cached data and show offline state; do not clear credentials merely because a request timed out.
8. Never leave the user on an infinite spinner or reveal private data before account/session resolution.

Onboarding is **first-run only**: three swipeable pages with progress dots and Skip/Continue:
1. “Your private space” — only you and your paired partner.
2. “Stay close” — real-time chat and truthful delivery/read state.
3. “Hear and see each other” — audio/video calls subject to permissions and connectivity.
Finish with “Get started”. Persist completion in DataStore. Add optional “Replay onboarding” in Settings/About.

## 5. Screen specifications

Implement every screen with relevant loading, success, empty, offline, error, permission-denied, and retry states. Every visible button must work or be disabled with a clear explanation.

### Splash
Duora wordmark and tagline, tasteful transition, bounded startup work, retryable failures.

### Auth
Register, login, logout, session refresh, and password reset only if email/reset delivery is configured. Validate inputs, show/hide password, handle keyboard actions, and never log/store raw passwords. Password recovery must not reveal account existence.

### Home
- Top-left: Duora wordmark/name.
- Top-right: small circular DP/avatar (36–44 dp) with subtle outline; tap opens Profile.
- Include an overflow menu near the top bar, while keeping the avatar circular and small.
- Personalized greeting and partner card with avatar/name and real online/last-seen state.
- Partner note/status only if implemented end-to-end.
- Quick actions: Audio Call, Video Call, Message.
- Latest message preview/unread count from real backend data.
- Honest offline/connectivity banner and retry.
- If unpaired, show pairing CTA instead of partner content.
- If no history, show a polished empty state.
- Bottom navigation: **Home, Chat, Settings**, icons and labels, Raspberry selected state. Respect insets/keyboard; hide tabs during full-screen calls and auth/onboarding.
- Overflow entries: Pairing, Theme, Server Settings, Profile, Privacy & App Icon, Notifications, Developer Info, About. All navigate to real screens.

### Pairing / QR
Create short-lived single-use invitation, show code/QR, copy/share via Android share sheet, enter code, scan QR with contextual camera permission, verify on server, display safe partner identity, require explicit confirmation, and support expired/revoked/used/invalid/self-pairing/already-paired/offline states. Revoke invitation if supported. QR must never contain password or long-lived token. Pairing must be atomic on the backend.

### Chat
Only the active paired conversation. Partner header/presence, paginated history, text composer, sending/retry states, date separators, optional search if implemented. Optional attachments/voice notes must not appear as fake controls.

Message states: queued/offline, sending, sent, delivered, read, failed. Generate a stable `clientMessageId` UUID and reuse it for retries so messages cannot duplicate. Persist outgoing messages to Room before network send. Reconnect using cursors and merge server/local records. Only mark delivered/read on authoritative acknowledgement. Typing events are transient and expire automatically.

Realtime events include `message:send`, `message:new`, `message:ack`, `message:delivered`, `message:read`, `message:receipt`, `typing:start`, `typing:stop`, `typing:update`, `presence:update`, and `server:error`. Authenticate sockets and authorize every event/room.

### Audio calls
Implement real WebRTC audio calling: outgoing/incoming screens, ringing, accept, decline, cancel, end, mute, speaker/audio route if supported, actual connected timer, busy/timeout/missed/failure states, permission handling, remote hang-up, network recovery, and cleanup of all tracks/timers/audio focus/services.

### Video calls
Full-screen remote video, movable local preview, camera toggle, microphone mute, camera switch where supported, end call, connecting/reconnecting states, contextual camera/mic permission, WebRTC with STUN/TURN, authorized SDP/ICE signaling, cleanup, and native PiP only if implemented and tested. Never send media frames/base64 through Socket.IO.

### Incoming calls
Foreground in-app UI; background FCM/notification actions and native foreground call service where supported and policy-compliant. Deduplicate events, expire stale calls, handle notification denial and remote cancellation. Do not promise ringing after force-stop, denied notifications, no network, or OS/OEM background restrictions. WorkManager is not a real-time call delivery solution.

### Settings and profile
Theme System/Light/Dark, notifications/sound, privacy, icon, account/profile, pairing, server, storage/cache, developer info, About, policy links, logout/session management. Persist preferences. Profile edits must call backend and handle errors. Do not expose tokens/internal IDs.

### Privacy & App Icon
Add a dedicated screen with user-controlled icon/label choices. Implement declared launcher activity aliases and icon resources in `AndroidManifest.xml`; switch aliases with `PackageManager.setComponentEnabledSetting` and test on real launchers. Include “Restore Duora icon”. Do not reset app data or require reinstall. Avoid impersonating real banking, government, security, or OS apps. Explain that changing icon does not encrypt data or guarantee invisibility in Settings, recents, notifications, app stores, or forensic tools. Keep deep links, notifications, and app updates working.

Additional privacy features:
- Hide message text in notifications by default/setting.
- Optional app lock with AndroidX Biometric/device credential.
- Optional sensitive-content privacy overlay or `FLAG_SECURE` with a clear explanation of screenshot/accessibility trade-offs.
- Logout all sessions and clear local cache with confirmation.
- Never claim E2EE unless a real, reviewed protocol is implemented.

### Server Settings
Show API base URL and environment, test `/health` and optionally `/ready` with timeout, display actual result/latency, require HTTPS/WSS in production, confirm server changes, clear server-specific cached state safely, and re-authenticate if necessary. No secrets in UI. On physical Android, `localhost` refers to the phone itself, not a PC.

### Developer Info/About/Update
Show version/build variant, environment, cache status, REST and socket state separately, last sync, and a diagnostics copy action that redacts tokens, passwords, pairing codes, message bodies, identifiers, SDP/ICE. Update check uses a configured endpoint, timeout, retry, and real release URL only; never block forever or fabricate a version.

## 6. Backend and database

Inspect existing `server/` code and migrations first. Update in place; do not drop/reset tables or existing data. Use additive/reversible migrations and document backup steps.

Stack: Node.js LTS, TypeScript strict mode, Express, Socket.IO, PostgreSQL, Prisma, Zod, Argon2id or a reviewed password-hashing library, hashed/rotating refresh sessions, FCM if configured, redacted structured logging, tests.

Minimum entities:
- `User`
- `RefreshSession`
- `Couple`
- `CoupleMember`
- `PairingInvitation`
- `Conversation`
- `Message`
- `MessageReceipt`
- `DeviceToken`
- `CallSession`
- `UserPreference`
- `PartnerNote` only if feature is implemented

Use UUIDs, UTC timestamps, foreign keys, indexes, unique constraints, and explicit delete rules. Enforce exactly two members per active couple and prevent conflicting active pairings transactionally.

API prefix `/api/v1`:
- Auth: `POST /auth/register`, `/auth/login`, `/auth/refresh`, `/auth/logout`, `/auth/logout-all`, `/auth/forgot-password`, `/auth/reset-password` only when delivery exists.
- Profile/device: `GET /users/me`, `PATCH /users/me`, `POST /devices/push-token`, `DELETE /devices/push-token`.
- Pairing: `POST /pairing/invitations`, `/pairing/verify`, `/pairing/accept`, `GET /pairing/current`, `DELETE /pairing/invitations/:id`; unpair only with defined policy.
- Messages: `GET /conversations`, `GET /conversations/:id/messages?cursor=&limit=`, `POST /conversations/:id/messages`, receipts endpoints.
- Calls: `POST /calls`, `GET /calls/:id`, `POST /calls/:id/accept`, `/decline`, `/cancel`, `/end`; optional short-lived TURN credentials.
- Health: `GET /health`, `GET /ready`.

All requests/events require schema validation and object-level authorization. Derive sender/caller/couple identity from authenticated credentials; never trust IDs sent by the client. Use consistent error envelopes and request IDs.

Socket server events: `message:new`, `message:ack`, `message:receipt`, `typing:update`, `presence:update`, `pairing:update`, `call:incoming`, `call:accepted`, `call:declined`, `call:cancelled`, `call:ended`, `call:offer`, `call:answer`, `call:ice-candidate`, `server:error`. Validate payload sizes, authenticate sockets, authorize rooms, rate-limit, acknowledge important events, and handle reconnect/replay.

Security:
- HTTPS/WSS production.
- Short-lived access tokens and rotating/revocable refresh tokens; store only refresh-token hashes.
- Rate limits for auth, pairing, messages, and calls.
- Hash invitation secrets; single-use and expiry.
- Redact credentials, message bodies, pairing secrets, and private signaling from logs.
- `.env.example` contains placeholders only; never commit secrets.
- No hard-coded TURN secrets in APK; use short-lived TURN credentials.
- Define deletion/retention/backups. TLS is not E2EE.

## 7. Resilient logic

- Keep REST reachability and socket connection state separate.
- Bounded exponential backoff with jitter; cancel retries on logout.
- Avoid concurrent refresh-token races.
- Reconnect by syncing cursors and refreshing pairing/call state.
- Server is authoritative; stale local/socket events must not overwrite newer state.
- Room stores cached messages and outbox; DataStore stores non-secret preferences; Keystore-backed storage holds credentials.
- Namespace local records by account and isolate/clear on account switch.
- Presence comes from authenticated active sessions/heartbeat timeout; handle multiple devices. Offline UI says “Last known”, not live “Online”.
- Call state transitions are validated server-side; only participants can signal. Use WebRTC media, Socket.IO signaling, STUN/TURN relay testing, cleanup on all terminal paths.
- FCM is best-effort. Notification tap fetches current authorized state. Minimal payloads and deduplication by call/message ID.
- Map offline, timeout, 401, 403, 404, 409, 422, 429, 5xx, socket disconnect, permission denial, expired invitation, stale call, invalid push token, and ICE failure to useful messages and safe retry behavior.

## 8. Update documentation

Update root `ui.md`, `server.md`, and `logics.md` to reflect **native Kotlin**, not React Native:
- `ui.md`: Raspberry/Pale Sky tokens, Compose screens, navigation, state handling, icon privacy, calls, accessibility, acceptance criteria.
- `server.md`: actual Node/Express/Socket.IO/PostgreSQL/Prisma schema, APIs, security, FCM, TURN, migrations, deployment, tests.
- `logics.md`: startup/auth states, pairing, offline outbox/sync, presence, call state machines, icon alias behavior, errors, tests.
If files are missing, create them. Do not document a feature as implemented until code and tests exist; mark incomplete work clearly.

## 9. Developer setup

- Inspect repository before adding files; keep Gradle/npm lockfiles consistent.
- Add `.env.example` placeholders only, README setup commands, migration/backup instructions, and troubleshooting.
- Backend scripts should include suitable `dev`, `build`, `start`, `test`, `lint`, and migration scripts.
- Add Android unit/UI/instrumentation tests and backend unit/API/realtime/database tests.
- Do not assume Android Studio/SDK is installed in Termux. Run whatever checks are actually available, report exact outcomes, and provide accurate build steps for a compatible Android SDK/CI environment. This is native Kotlin; Expo Go does not apply.

## 10. Acceptance criteria

- First launch shows onboarding; later launches skip it.
- Correct routing for signed-out, unpaired, paired, and offline users.
- Home top-left brand, top-right circular DP, overflow menu, bottom Home/Chat/Settings.
- Theme persists.
- All buttons work; no fake statuses or mocked success in production.
- Valid invitation pairs exactly two accounts; expired/reused/self-pairing/concurrent attempts fail safely.
- Offline messages queue, sync once, paginate, and show truthful receipts.
- Calls accept/decline/cancel/end, timer starts only after actual connection, TURN relay is tested, and media resources are released.
- Icon switching/restoration works on supported launchers without deleting data.
- Diagnostics redact sensitive data.
- Migrations preserve existing data; every resource has authorization tests.
- `/health` and `/ready` work; socket auth and room access are tested.
- Build/test results are real and accurately reported.

## 11. Execution order and final report

1. Inspect repository, Gradle modules, server, migrations, docs, dependencies, and tests.
2. Produce a concise gap analysis and flag migration risks.
3. Update the three docs.
4. Implement theme, splash, onboarding, navigation, persistent theme.
5. Implement auth/session handling and secure routing.
6. Implement pairing and QR with backend validation.
7. Implement database models, safe migrations, APIs, and tests.
8. Implement Room outbox, realtime chat, receipts, pagination, reconnect sync.
9. Implement presence and FCM.
10. Implement WebRTC audio/video, STUN/TURN, incoming call service, and PiP where feasible.
11. Implement icon aliases and privacy settings.
12. Implement server settings, diagnostics, About/update.
13. Run formatting, lint, unit/integration tests, migrations, and available Android builds.
14. Fix discovered failures; don't stop at the first compile error.
15. Report changed files, completed/blocked features, commands actually run and results, environment variables needed, migration/backup notes, and a real-device manual test checklist.

Non-negotiable: native Kotlin Android, Node/npm backend in `server/`, exact brand colors, no data destruction, no hard-coded secrets, no fabricated test results, no fake chat/calls, no unsupported E2EE or guaranteed background ringing claims. Start by inspecting the existing repository now.
