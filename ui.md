# Duora UI Architecture Documentation (Native Kotlin & Jetpack Compose)

## 1. Overview & Brand Philosophy
**Duora** — *"Two hearts. One private space."*
Duora is an intimate, private communication platform crafted exclusively for two mutually paired individuals. It provides an exclusive sanctuary free from public discovery, feeds, ads, or third-party interference.

### Visual Foundations & Design Language
- **Rounded Romantic Geometry**: Card curvature of 16dp to 26dp, soft container corners (12–20dp), circular badges and status pills.
- **Micro-interactions & Spring Transitions**: Subtle 150–250 ms transitions for bottom bar switching, message arrivals, and connection heart pulsing.
- **Touch & Accessibility Standards**:
  - Minimum 48dp x 48dp touch targets on all interactive buttons, icon buttons, and navigation elements.
  - Descriptive `contentDescription` on every image, icon, and button for TalkBack screen reader support.
  - Scalable font units (`sp`) and layout dimensions in `dp`.
  - Android WindowInsets handling with `statusBarsPadding()`, `navigationBarsPadding()`, and `imePadding()` on text composer inputs.

---

## 2. Color Tokens & Material 3 Palette

### Exact Brand Palette (Master Specification)
- **Raspberry (`#5B3DF5`)**: Primary actions, active navigation icons/labels, CTA buttons, key romantic accents.
- **Pale Sky (`#E8DEFF`)**: Soft container backgrounds, badge containers, navigation pill indicators, chips, and onboarding highlights.

### Supporting Color System
| Role | Light Theme Token | Dark Theme Token | Semantic Meaning |
|---|---|---|---|
| Background | `#FBF9FF` | `#100D18` | Screen backdrop |
| Surface | `#FFFFFF` | `#1B1726` | Card backgrounds, dialog containers |
| Elevated Surface | `#F3EFFF` | `#262035` | Popups, sheets, dialog surfaces |
| Primary Text | `#241B3B` | `#F8F5FF` | High-contrast headers and titles |
| Secondary Text | `#716985` | `#C0B7D2` | Subtitles, timestamps, captions |
| Outline / Borders | `#D9CFF5` | `#3A3150` | Dividers, card strokes, avatar rings |
| Accent Emerald | `#10B981` | `#34D399` | Online presence, active connection |
| Accent Amber | `#F59E0B` | `#FBBF24` | 24-Hour notes, anniversary countdowns |
| Accent Rose | `#F43F5E` | `#FB7185` | Call actions, missed alerts, end call |

Both `DuoraDarkColorScheme` and `DuoraLightColorScheme` are centralized in `Theme.kt` and adapt dynamically to `AppThemeSetting.SYSTEM`, `AppThemeSetting.LIGHT`, and `AppThemeSetting.DARK`.

---

## 3. Screen Specifications & Compose Hierarchy

### 3.1 Splash Screen (`SplashScreen.kt`)
- **Branding**: Centered Duora wordmark with pulsing romantic heart icon and tagline *"Two hearts. One private space."*
- **Startup Pipeline**:
  1. Initialize local persistent repositories (DataStore / SharedPreferences, credentials, call logs).
  2. Restore pairing credentials and cached user profile.
  3. Validate first-run onboarding completion state.
  4. Perform non-blocking server health ping with timeout.
  5. Route deterministically to `ONBOARDING` (first launch), `PAIRING` (if unpaired), or `HOME` (if paired).

### 3.2 First-Run Onboarding (`OnboardingScreen.kt`)
- **First-run only**: Driven by persistent state `isOnboardingCompleted`.
- **Swipeable 3-Page Experience**:
  1. **Page 1: "Your private space"**: Exclusively for you and your partner, encrypted and private with double-heart lock imagery.
  2. **Page 2: "Stay close"**: Real-time conversation, 24-hour shared love notes, instant reactions, delivery receipts.
  3. **Page 3: "Hear and see each other"**: High-definition audio and video calling, movable camera preview, picture-in-picture mode.
- **Controls**: Progress indicator dots, "Skip" action, "Continue" step button, and final "Get Started" CTA button.
- **Replay Feature**: Accessible from Settings tab > About Duora > Replay Onboarding.

### 3.3 Main Container & Navigation (`HomeScreen.kt`)
- **Top Bar**:
  - Left: Duora wordmark, romantic heart icon, tagline, and real-time live server connectivity dot (green/rose).
  - Right: Small circular avatar DP (38dp) with user initial and outline; single-tap opens Profile & Relationship edit modal.
  - Three-dot overflow menu: Pairing, App Theme, Server Settings, Profile & Relationship, Privacy & App Icon, Mute Alerts, Developer Info, About Duora.
- **Bottom Navigation Bar**:
  - Exactly 3 tabs: **Home**, **Chat**, **Settings**.
  - Raspberry (`#5B3DF5`) selected indicator and tint, Pale Sky pill highlight.
  - Hidden automatically during active full-screen calls, onboarding, and splash.

### 3.4 Home Tab (`HomeScreen.kt`)
- **Partner Card**: Shows mutual pair codes, user and partner avatars, partner status ("Thinking of you always ❤️"), and real-time online state.
- **24-Hour Shared Note Card**: Displays active couple note, countdown timer (`remainingFormatted`), delete action, and write/edit note dialog with quick romantic presets.
- **Quick Call Bar**: Large, tactile buttons for Voice Call (Raspberry) and Video Call (Rose).
- **Recent Call History**: Log of past voice and video sessions, duration formatting, outgoing/incoming indicators, and "Clear Logs" action.
- **Unpaired State**: Shows contextual pairing CTA card ("Link Partner Code Now") guiding user to link pair codes.

### 3.5 Chat Tab (`ChatTab.kt`)
- **Header**: Partner nickname, pair code, and real-time online status dot.
- **Message List**: Auto-scrolling list of bubble messages grouped by timestamp.
  - Outgoing bubbles: Raspberry background (`#5B3DF5`), white text, timestamp, delivery ticks (sent/delivered/read).
  - Incoming bubbles: Pale Sky / surface container background, dark text, timestamp.
- **Typing Indicator**: Transient indicator showing when partner is actively composing a note/message.
- **Composer**: Rounded input field with romantic heart reaction buttons, text field, and tactile send button.

### 3.6 Settings Tab (`SettingsTab.kt`)
- **Profile & Relationship**: Edit display name, partner nickname, view mutual codes, anniversary countdown.
- **Theme Selection**: System Default, Pale Sky Light, Midnight Dark.
- **Privacy & Disguise Icon**: Direct navigation to launcher disguise dialog.
- **Alerts & Notifications**: Toggle vibration, ringing, and muted states.
- **Server Configuration**: View active WebSocket / HTTP host, test latency, customize URL.
- **Diagnostic Information & Replay**: Redacted diagnostic copy, onboarding replay, clear cache.

### 3.7 Audio & Video Call Screens (`CallScreen.kt`)
- **Audio Call**: High-contrast dark romantic backdrop, pulsing heart rings (`PulsingHeartRing.kt`), partner avatar, call duration timer, mute toggle, speakerphone toggle, and red end-call button.
- **Video Call**: Full-screen remote video stream, floating draggable local camera preview (`CameraPreview.kt`), camera flip, mic mute, PiP mode toggle, and end-call action.

---

## 4. Privacy & Stealth Disguise Architecture
Duora provides stealth launcher camouflage to protect user discretion:
- Configured via Android `activity-alias` components in `AndroidManifest.xml`.
- Options:
  1. **Duora (Original)** — Default icon & label.
  2. **Calculator** — Stealth calculator icon & label.
  3. **Notes** — Stealth notepad icon & label.
  4. **Clock** — Stealth minimalist dial icon & label.
  5. **Weather** — Stealth sun icon & label.
- Managed by `PrivacyIconManager.kt` via `PackageManager.setComponentEnabledSetting` using `DONT_KILL_APP` flag.
- "Restore Duora icon" button always accessible from Settings and Privacy dialog.
