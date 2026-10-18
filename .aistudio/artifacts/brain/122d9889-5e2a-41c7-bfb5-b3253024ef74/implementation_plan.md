# AmourCall – Personal Couple Calling App

AmourCall is an intimate, dedicated calling application crafted exclusively for two lovers. It features a luxurious modern love aesthetic, seamless 6-character couple code pairing, quick one-tap voice and video calls with real camera and audio controls, ambient couple memory photo backgrounds, incoming call notifications, and a developer profile page.

### User Review & Critical Decisions

> [!IMPORTANT]
> The following decisions were confirmed based on your answers and prompt specifications:

- **Couple Pairing Model**: 6-character private **Love Pairing Code** system allowing the two partners to link their devices securely with customizable partner nickname, relationship milestone date, and profile photo.
- **Calling Engine**: Fully functional direct peer calling engine with live camera preview (front/back camera flip), microphone capture/mute, loudspeaker toggle, active call duration timer, and animated floating love reactions during calls.
- **Memories Background System**: Personalized memories gallery supporting on-device photo picker, romantic captioning, and full-bleed ambient background slideshows on both the home dashboard and live call screens.
- **Notifications & Background State**: Android high-priority heads-up notification with custom vibration and quick "Accept" / "Decline" actions when the partner calls.
- **Developer Profile**: Dedicated Developer Info page spotlighting developer **Sukhdev Singh** (`sukhdevr898`) with direct clickable links to GitHub and Instagram.

---

### 1. Overview & Core Concept

- **What It Does**: Provides an ultra-private, distraction-free calling space designed solely for a couple. With a single tap, initiate high-quality voice or video calls surrounded by favorite romantic memory photos.
- **Target Persona**: Couples who want a private, romantic, and distraction-free calling lifeline dedicated only to each other, free from social media noise or clutter.
- **Key Value**: Instant one-tap connectivity, emotional intimacy through shared memory backgrounds and romantic visuals, and zero bloatware.

---

### 2. User Experience & Visual Design

#### Visual Identity & Theme
- **Color Palette**:
  - Primary: Romantic Crimson Rose (`#E11D48`) and Passion Pink (`#F43F5E`)
  - Accent: Soft Champagne Gold (`#FBBF24`) and Warm Blush (`#FDA4AF`)
  - Surface & Background: Deep Midnight Plum (`#0F0814`) to Dark Velvet Rose (`#1E0A1A`) gradient
  - Cards & Glass: Frosted translucent containers (`#2A1224` with 60% opacity and subtle rose borders)
- **Typography & Atmosphere**:
  - Elegant serif and rounded modern sans-serif typography hierarchy
  - Subtle floating hearts and ambient breathing light pulse effects
  - Organic rounded corners (28dp pill buttons and 24dp memory photo cards)

#### Key Screens & User Flows

```
┌────────────────────────────────────────────────────────────────────────┐
│                              AmourCall Flow                            │
├─────────────────┬──────────────────────┬───────────────────────────────┤
│ 1. Pair / Setup │ 2. Love Dashboard    │ 3. Active Call Experience     │
│                 │                      │                               │
│  [Couple Code]  │  [Memory Wallpaper]  │  [Live Camera / Memory BG]    │
│  "Link Partner" │  Partner: "My Love"  │  "Connected with My Love"     │
│  My Code: #ROSE7│  Days Together: 420  │  Timer: 14:28  ❤️ Floating    │
│  Enter: [PAIR_] │  [📞 Voice] [📹 Video]│  [🔇 Mute] [📷 Flip] [🔴 End] │
│                 │  [Memories] [Dev]    │                               │
└─────────────────┴──────────────────────┴───────────────────────────────┘
```

1. **Love Dashboard (Home)**:
   - Full-bleed ambient memory background with smooth darkening gradient for optimal text contrast.
   - Relationship Counter widget ("420 days of loving you ❤️").
   - Partner Status Badge ("Online & Ready" / "In My Heart").
   - Quick Action Hero Cards:
     - **Voice Call** (One-tap instant audio call with romantic ringing pulse).
     - **Video Call** (One-tap crystal-clear video call with camera preview).
   - Bottom Bar / Floating navigation: Memories Gallery, Call Log, and Developer Info.

2. **Incoming & Outgoing Call Experience**:
   - Outgoing: Expanding heart wave rings, partner profile photo with glowing gold halo, and audio ringing chime.
   - Incoming: Full-screen incoming call UI with gentle heartbeat vibration and heads-up system notification with "Accept" and "Decline" buttons.
   - Connected Call:
     - Real-time call duration timer.
     - Live camera surface for video mode with small movable picture-in-picture (PiP) local preview.
     - Romantic floating heart tap interaction (tap anywhere on screen to send floating hearts).
     - Essential call controls: Mute Audio, Toggle Video, Flip Camera (Front/Back), Speakerphone, and End Call.

3. **Memories Wall & Wallpaper Selector**:
   - Curated list of couple memories with photos, dates, and sweet love notes.
   - Built-in photo picker (`PickVisualMedia`) to add new photos directly from the device.
   - Option to set any memory photo as the current app and call background wallpaper or enable auto-rotation.

4. **Developer Info Page**:
   - Polished glassmorphic card showcasing **Sukhdev Singh**.
   - Username: `@sukhdevr898`.
   - Direct intent links to GitHub (`https://github.com/sukhdevr898`) and Instagram (`https://instagram.com/sukhdevr898`).
   - Developer badge, app version, and tribute message.

---

### 3. Key Product Decisions & Trade-Offs

- **Pairing System**:
  - *Approach*: Pair devices using a lightweight 6-character Love Code stored in local persistent preferences with real-time peer state coordination.
  - *Why*: Eliminates complex multi-step passwords or registration friction while keeping the connection strictly private between the two partners.
- **Media & Hardware Pipeline**:
  - *Approach*: CameraX `PreviewView` / Compose Camera surface for local video feed, `AudioManager` for speakerphone routing, and `AudioRecord` / `MediaPlayer` for authentic ringtones and audio indicators.
  - *Why*: Provides genuine, reliable hardware access without crashing or lagging on Android 14–16 devices.
- **Photo Memories**:
  - *Approach*: Modern Android zero-permission Photo Picker (`ActivityResultContracts.PickVisualMedia`) storing URIs securely in local Room database/DataStore.
  - *Why*: Respects privacy and Google Play policy (no broad storage permissions needed) while letting users choose real personal photos.

---

### 4. Technical Architecture & System Layout

```
┌────────────────────────────────────────────────────────────────────────┐
│                        AmourCall Architecture                          │
├────────────────────────────────────────────────────────────────────────┤
│                               UI Layer                                 │
│  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────────────┐ │
│  │ LoveDashboardUI  │  │ ActiveCallScreen │  │ MemoriesGalleryScreen │ │
│  └────────┬─────────┘  └────────┬─────────┘  └───────────┬───────────┘ │
│           │                     │                        │             │
│           └──────────────┬──────┴────────────────────────┘             │
│                          ▼                                             │
│                     CallViewModel & AppState                           │
│                          │                                             │
│       ┌──────────────────┼──────────────────────────────┐              │
│       ▼                  ▼                              ▼              │
│  CallingEngine     PairingManager               MemoriesRepository     │
│  (CameraX, Audio,  (Love Code State,            (Local Room DB,        │
│   Ringtone, Haptics)Call Signaling)              Photo Picker Storage) │
└────────────────────────────────────────────────────────────────────────┘
```

#### Android Permissions Required
- `android.permission.CAMERA`: For live video calling preview and camera switching.
- `android.permission.RECORD_AUDIO`: For real microphone voice capture during calls.
- `android.permission.MODIFY_AUDIO_SETTINGS`: For toggling speakerphone and in-call audio routing.
- `android.permission.POST_NOTIFICATIONS`: For incoming call heads-up notification on Android 13+.
- `android.permission.VIBRATE`: For romantic incoming call vibration pulse.

#### Data Models & State
- `CoupleProfile`: Partner name, nickname, pairing code, anniversary date, avatar URI.
- `CallState`: `IDLE`, `OUTGOING_RINGING`, `INCOMING_RINGING`, `CONNECTED`, `ENDED`.
- `CallType`: `VOICE`, `VIDEO`.
- `MemoryPhoto`: ID, title, date, photoUri, isCurrentWallpaper.
- `CallRecord`: Timestamp, call type, duration, partner name.
