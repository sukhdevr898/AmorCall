# Duora Core Logics & State Machines (`logics.md`)

## 1. Startup & Session Routing Logic

```
                    ┌─────────────┐
                    │ App Launch  │
                    └──────┬──────┘
                           │
                 [Initialize Storage]
                 - Room / SharedPreferences
                 - Check Onboarding State
                           │
             ┌─────────────┴─────────────┐
             ▼                           ▼
   [First Launch: False]       [First Launch: True]
             │                           │
   [Check Pairing State]       [Show OnboardingScreen]
     ┌───────┴───────┐                   │
     ▼               ▼                   ▼
 [Paired]        [Unpaired]      [Complete Onboarding]
     │               │                   │
     ▼               ▼                   ▼
[HomeScreen]   [PairingScreen]     [Route by Pairing]
```

1. **State Initialization**: `CoupleRepository` loads user pair code (auto-generated 6-character alphanumeric if not existing), partner pair code, user profile names, and app preferences.
2. **First-Run Guard**: If `isOnboardingCompleted` is false, user is guided through the 3-step swipeable onboarding flow.
3. **Session Reconnection**: `SignalingClient` initiates WebSocket connection to configured server with exponential backoff retry.

---

## 2. Pairing & Partner Linking State Machine

- **Code Format**: 6-character uppercase alphanumeric code (e.g., `A9F2X1`).
- **Pairing States**:
  - `IDLE`: Displaying user's code and empty partner input field.
  - `VERIFYING`: Performing REST check against `/api/v1/pairing/verify` to validate partner code.
  - `LINKED`: Mutual pairing stored in local repository and registered on signaling server.
  - `FAILED / SELF_PAIR`: Guard against pairing user code with itself; returns descriptive error toast.

---

## 3. Real-Time Chat & Receipts Logic

### 3.1 Message Lifecycle
1. User enters text or selects quick heart reaction in `ChatTab`.
2. Outgoing message created with unique UUID `id`, `timestamp = System.currentTimeMillis()`, and initial status `SENDING`.
3. Added immediately to local state for zero-latency UI responsiveness.
4. Sent via WebSocket `chat_message` payload.
5. Server saves message to PostgreSQL / in-memory store and forwards to partner.
6. When partner device receives or acknowledges message, status updates to `DELIVERED` / `READ`.

### 3.2 Typing Indicator
- Typing status emits on text change with 2.5-second debounce.
- Partner's typing indicator automatically times out after 3 seconds of inactivity.

---

## 4. Call State Machine (Voice & Video)

```
[IDLE] ─────(Initiate Call)─────► [OUTGOING_RINGING]
  ▲                                       │
  │                               (Partner Answers)
  │                                       ▼
  │                                  [CONNECTING]
(End / Decline)                           │
  │                                (WebRTC Peer Up)
  │                                       ▼
  └──────────────────────────────── [CONNECTED]
```

- **Audio Calling**: Uses `CallAudioHaptics` and `AudioStreamManager` with earpiece/speaker routing, audio focus management, and foreground notification service.
- **Video Calling**: Leverages CameraX / hardware camera preview with movable local PiP view and remote video stream rendering.
- **Background Hotline Service**: `CallForegroundService` ensures incoming call alerts trigger notification heads-up display even when app is minimized.

---

## 5. Privacy Camouflage & Activity Aliases

- Disguise icons are declared in `AndroidManifest.xml` as `<activity-alias>` entries targeting `.MainActivity`.
- When user selects a disguise (e.g. `Calculator`, `Notes`, `Clock`, `Weather`):
  1. `PrivacyIconManager.setComponentEnabledSetting()` enables selected alias and disables other 4 aliases.
  2. Flag `PackageManager.DONT_KILL_APP` ensures smooth transition without terminating ongoing calls or sessions.
  3. Preference is persisted in SharedPreferences for seamless app upgrades.
- "Restore Duora icon" instantly reactivates `.MainActivityDefault`.
