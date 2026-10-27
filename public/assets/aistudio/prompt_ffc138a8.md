Sukh, Duora ke Settings aur Avatar system ko implement karne ke liye sabse important architecture decision ye hai: avatars Node.js server par store honge, Android app unhe API se fetch karegi, aur database mein user ka sirf `avatar_id` save hoga.

Isse app ka APK lightweight rahega, avatar library server se update ho sakegi aur boy/girl avatar collections ko alag manage karna aasaan hoga.

### 1\. Updated UI design

![Duora Settings and avatar selection design](sandbox:/mnt/data/a_clean_high_resolution_ui_ux_concept_board_app.png)

Design board: Settings, Profile, Boy/Girl Avatar Gallery, Partner Pairing, Theme, Privacy, Notifications, Server Health, About, Developer Profile aur Logout.

Full design image open karo

### 2\. Avatar system ka architecture

Duora Android App — Kotlin

Profile screen · Boy/Girl filters · Avatar selection

Node.js Avatar API

List avatars · Fetch images · Validate selections

Database

user_id, avatar_id

Avatar storage

Images + metadata

Node.js controls the API; image files can be stored on the server filesystem or object storage. The database stores avatar references, not image binaries.

Important: `avatar_id` database mein save karna enough hai only if IDs are stable. Avatar delete ya replace hone par fallback avatar define karo. Public avatar images ko CDN/cache se serve kiya ja sakta hai; private user-uploaded profile photos ke liye access control zaroori hai.

### 3\. Master prompt — Duora Settings + Avatar Backend

Is prompt ko apne coding AI mein paste karo. Saath mein upar wali design image aur existing Duora project attach/open karo.

# Duora Premium Settings, Avatar Library and Partner Pairing System

## Role

Act as a senior Kotlin Android developer, Jetpack Compose UI/UX engineer and Node.js backend architect.

Upgrade the existing Duora application using the supplied design reference. Implement a premium romantic Settings experience, a remotely managed avatar library for boys and girls, profile persistence, secure partner pairing, theme settings, privacy controls, notification preferences, server health, About, Developer Profile and Logout.

Inspect the current project before making changes. Preserve existing authentication, chat, calling, notification and database functionality. Do not rewrite working modules unnecessarily.

## 1. Existing Technology

Use the technologies already present in the project:

- Android: Kotlin, Jetpack Compose, Material 3, Navigation Compose, ViewModel and StateFlow.
- Backend: existing Node.js server and its current framework.
- Database: existing Supabase/PostgreSQL database.
- Authentication: existing authentication provider.
- Avatar storage: Node.js-managed image library, or the existing object-storage service if configured.
- Networking: existing HTTP client, preferably Retrofit or Ktor where already used.
- Local settings: DataStore.
- Image loading: Coil for Compose.
- QR scanning: CameraX and a compatible barcode scanning library.

Do not add a new database or backend framework without a clear technical reason.

## 2. Design System

Follow the supplied Duora design image:

- Raspberry: #5B3DF5
- Primary pink: #FF2D8D
- Secondary pink: #FF5D88
- Pale lavender: #E8DEFF
- Background: #FFF6FA
- Card background: #FFFFFF
- Primary text: #21102E
- Secondary text: #89818E
- Success: #16A765
- Error: #E53945

Use rounded cards, soft gradients, premium icons, consistent typography, subtle shadows, clean spacing and lightweight animations.

Use the existing Duora logo and artwork. Make the layout responsive for different Android screen sizes and system font scales.

## 3. Main Settings Screen

Create a Settings screen containing:

1. Profile card with selected avatar, display name and edit action.
2. Partner Pairing.
3. App Theme.
4. Privacy & App Icon.
5. Notifications.
6. Server Health.
7. About Duora.
8. Developer Profile.
9. Log Out.

Each setting row must have a consistent icon, title, subtitle, optional current value and navigation action.

Keep the existing bottom navigation on the main Settings screen if that is how the app is currently structured. Use a back button on secondary screens.

Do not expose server URLs, API keys, database connection strings or internal endpoints.

## 4. Remote Avatar Library — Core Requirement

The boy and girl avatar libraries must be managed by the Node.js backend.

The Android app must fetch the avatar catalogue and images from the backend instead of bundling the entire library in the APK.

### Avatar categories

Support these categories:
- BOY
- GIRL

Optionally support filters such as:
- All
- Anime
- Cute
- Stylish
- Minimal

Filters must reflect real catalogue metadata, not hardcoded assumptions.

Each avatar record returned by the API should include:
- Stable avatar ID.
- Display name.
- Category.
- Image URL or authorized image reference.
- Optional thumbnail URL.
- Optional style tags.
- Optional version or update timestamp.

The API may return image URLs, but the user's database profile must store only the selected `avatar_id`, not the image binary or a copied image URL.

The backend must resolve the avatar ID to its current image asset when serving the catalogue or profile.

### Storage rules

Store curated avatar image files on the Node.js server's managed storage or existing object storage.

For a small, static library, the server can serve versioned images from a dedicated read-only asset directory. For larger libraries or production traffic, use object storage/CDN while retaining Node.js as the API and authorization layer.

Do not place uploaded files in a temporary directory or inside a deployment directory that is routinely deleted during redeployment.

Keep catalogue metadata in a suitable backend data source. If the existing database is used for catalogue metadata, keep that separate from the user's selected-avatar field.

Do not save image binaries in PostgreSQL.

### Database profile rule

The user's profile must store:
- User ID.
- Display name.
- Selected avatar ID.

Do not store a duplicate avatar image, base64 data, or image URL in the user's profile record.

Use a stable identifier such as `boy_001` or `girl_014`. The ID must remain stable when the catalogue is reordered.

Validate the chosen ID against the server-side catalogue before updating the profile.

### Boy/Girl selection UI

Create a premium avatar-selection screen with:
- Boy Avatars and Girl Avatars tabs.
- Category-specific filters.
- A responsive grid of rounded avatar thumbnails.
- Loading skeletons.
- A selected border and checkmark.
- Avatar preview.
- Save Changes button.
- Retry, empty and error states.

When a user selects an avatar, update the local selection preview. Do not change the persistent profile until the user saves, unless the existing app explicitly uses immediate selection.

On Save:
1. Submit the selected avatar ID to the authenticated backend.
2. Validate the ID and category.
3. Update the user's profile.
4. Return the canonical saved profile.
5. Update the Android UI from the response.
6. Refresh or invalidate profile caches.
7. Show success feedback only after persistence succeeds.

If saving fails, preserve the current saved avatar and allow the user to retry.

### Profile avatar rendering

Use the same profile repository across Home, Settings, Chat and call screens.

The profile model should contain the selected avatar ID. The UI may resolve that ID to a cached image URL using the avatar catalogue.

Do not make each screen independently download or resolve the avatar. Share the repository and image cache.

If the selected avatar is missing or unavailable, display a safe default avatar and allow the user to choose another.

## 5. Node.js Avatar API

Inspect the existing server framework and conventions first. Adapt endpoint names to the existing API where appropriate.

Implement equivalent functionality to:

GET /api/avatars?category=BOY
GET /api/avatars?category=GIRL
GET /api/avatars/:avatarId
GET /api/profile/me
PATCH /api/profile/me/avatar

The catalogue endpoints should support pagination if the library becomes large. Return stable IDs, categories, display names and image references.

The profile endpoint should return the user's selected avatar ID and the associated profile data.

The avatar-selection endpoint should accept a selected avatar ID and validate:
- Authenticated user identity.
- Avatar existence.
- Active/available catalogue status.
- Permitted category.
- Request format and size.

The server must derive the user ID from the authenticated session or verified token. Do not trust an arbitrary user ID submitted by the Android client.

Use parameterized database queries or the existing ORM safely. Add suitable indexes and database constraints.

Return consistent HTTP status codes and safe error messages.

### Example response shapes

Avatar catalogue:

{
  "items": [
    {
      "id": "boy_001",
      "name": "Stylish Boy 1",
      "category": "BOY",
      "imageUrl": "https://your-configured-host/assets/avatars/boys/boy_001.webp",
      "thumbnailUrl": "https://your-configured-host/assets/avatars/boys/thumbs/boy_001.webp"
    }
  ]
}

Saved profile:

{
  "displayName": "Sukhdev",
  "avatarId": "boy_001"
}

These are illustrative response shapes, not actual deployed URLs or existing API contracts. Use the project's configured API base URL and naming conventions. The database profile must store the avatar ID, not the image URL.

## 6. Avatar Performance and Reliability

Implement:
- Thumbnail-first loading for the grid.
- Full-resolution loading for the selected preview.
- Coil caching.
- Stable image dimensions to prevent layout jumps.
- Appropriate image compression and WebP/AVIF where supported.
- Pagination or lazy loading for large catalogues.
- ETag or version-based cache validation where supported.
- A bounded network timeout.
- Retry with backoff for transient failures.
- Cancellation of obsolete requests.
- Error placeholders for unavailable images.

Avoid downloading the entire catalogue's full-resolution images at once.

Cache catalogue metadata and image thumbnails locally. The cache must not become the source of truth for the selected avatar. On successful profile update, invalidate or update the relevant profile cache.

## 7. Profile and Avatar Screen

Include:
- Current selected avatar.
- Edit display name.
- Boy/Girl avatar tabs.
- Avatar category filters.
- Avatar grid.
- Optional custom photo selection only if supported by the existing product.
- Save Changes.
- Cancel.

For custom photos, use Android Photo Picker and a separate authorized upload flow. Curated avatars and user-uploaded photos must have clearly defined storage rules.

Do not store custom image bytes or image URLs in the profile row if the product's chosen profile schema requires an ID/reference instead. If custom photos are supported, create a corresponding media record and store its stable media ID.

## 8. Partner Pairing

Implement pairing by code or QR scan.

### Request workflow

1. The requester enters a valid partner code or scans a valid QR code.
2. The backend verifies the token and its expiry.
3. The backend rejects self-pairing, invalid codes, duplicate requests and accounts already paired with someone else.
4. The backend creates a pending pairing request.
5. The recipient receives an in-app or push notification.
6. The recipient accepts or declines.
7. On acceptance, the backend atomically creates the mutual pairing relationship.
8. Both users' partner profiles are refreshed.
9. Partner names and avatars update throughout Home, Chat and Calls.
10. Both users receive a pairing-success state.

Never treat sending a request as a successful pairing. Only the recipient's accepted request and confirmed backend relationship may establish the connection.

Use short-lived pairing tokens, rate limits, authenticated requests and database constraints.

## 9. App Theme

Implement:
- Light.
- Dark.
- System default.
- Accent-color selection if supported.

Persist preferences in DataStore or the existing settings repository. Apply changes consistently across the app without losing navigation state.

## 10. Privacy and App Icon

Provide privacy settings such as:
- App icon selection using valid Android launcher aliases.
- Lock-screen notification preview controls.
- App lock or biometric lock only if actually implemented.
- Privacy policy access.

Changing the launcher icon is a presentation feature, not a security mechanism. Do not claim that it hides the app from system settings or security tools.

## 11. Notification Settings

Provide:
- Message notification toggle.
- Call notification toggle.
- Missed-call notification toggle.
- In-app notification toggle.
- Sound and vibration preferences.
- Lock-screen preview preference where supported.

Persist preferences and apply them to the existing FCM and in-app notification systems.

Reflect Android notification permission and channel settings accurately. Do not claim notifications are enabled when the operating system blocks them.

## 12. Server Health

Create a polished health dashboard showing:
- Overall status.
- Database.
- Realtime messaging.
- Calling/signaling.
- Avatar/media storage.
- Push notifications.

Use real capability checks where available. Show Connected, Degraded, Disconnected, Checking or Unknown appropriately.

Never show server URLs, IP addresses, API keys, connection strings or private infrastructure details.

Do not report all services as online merely because the main server responds to an HTTP request.

Use bounded timeouts, manual refresh and sensible retry behavior.

## 13. About, Developer Profile and Logout

About Duora:
- Logo and tagline.
- Actual app version.
- What's New.
- Terms and Privacy Policy.
- Help and support links.

Developer Profile:
- Configured developer name and avatar.
- Role or short biography.
- Verified project and contact links.

Do not invent developer details or links.

Logout:
- Show confirmation.
- Cancel or confirm.
- Revoke/clear the local session according to the existing auth provider.
- Cancel user-specific realtime subscriptions.
- Clear protected local state and navigate to Login.
- Do not delete account data or unpair users when logging out.

## 14. Remove Old Onboarding

Remove obsolete onboarding screens and routes without deleting authentication screens.

Update the startup flow:
- Unauthenticated user → Login/Sign Up.
- Authenticated user → Home or the existing required setup route.

Preserve existing accounts, profiles, partner relationships and app settings. Remove unused onboarding files only after checking their dependencies.

## 15. Architecture and File Organization

Follow the existing project structure. Suggested modules:

Android:
- ui/settings/SettingsScreen.kt
- ui/settings/ProfileAvatarScreen.kt
- ui/settings/AvatarGrid.kt
- ui/settings/ThemeSettingsScreen.kt
- ui/settings/PrivacySettingsScreen.kt
- ui/settings/NotificationSettingsScreen.kt
- ui/settings/ServerHealthScreen.kt
- ui/settings/AboutScreen.kt
- ui/settings/DeveloperProfileScreen.kt
- ui/pairing/PartnerPairingScreen.kt
- data/avatar/AvatarRepository.kt
- data/avatar/Avatar.kt
- data/profile/ProfileRepository.kt
- viewmodel/SettingsViewModel.kt
- viewmodel/AvatarViewModel.kt

Node.js:
- routes/avatarRoutes.js
- controllers/avatarController.js
- services/avatarService.js
- repositories/avatarRepository.js
- routes/profileRoutes.js
- middleware/authentication.js
- middleware/errorHandler.js

Adapt filenames and language conventions to the existing server. Do not create duplicate implementations where equivalent modules already exist.

## 16. Security

- Authenticate profile update requests.
- Derive user identity from a verified session.
- Validate avatar IDs server-side.
- Do not expose storage credentials.
- Restrict write access to curated avatar assets.
- Prevent directory traversal and arbitrary filesystem access.
- Validate custom uploads if supported.
- Enforce authorization through the existing database policies.
- Rate-limit sensitive pairing endpoints.
- Avoid logging access tokens or private profile data.

## 17. Testing and Deliverables

Test:
1. Boy and girl catalogues load from the Node.js API.
2. Avatar selection and save persist correctly.
3. The database profile stores only the selected avatar ID.
4. Updated avatars appear consistently in Home, Chat, Calls and Settings.
5. Unavailable avatars fall back gracefully.
6. Catalogue caching and retry work.
7. Pairing requires recipient acceptance.
8. Both users receive the confirmed partner profile.
9. Theme and notification preferences persist.
10. Server health never exposes private URLs.
11. Obsolete onboarding is removed without breaking login.
12. The app and server build and run successfully.

Provide complete code changes, required database migrations, API contracts, environment configuration, test results and deployment instructions. Do not fabricate test results or claim that an endpoint works before it has been tested.

### 4\. Step-by-step guide — Android + Node.js

* Step 1 — Inspect your existing code

    Find the current Settings screen, user profile model, Node.js server routes, database profile table and authentication middleware. Reuse those modules rather than making a second avatar system.

* Step 2 — Prepare avatar assets

    Organize your boy and girl avatar images into separate collections. Use unique, permanent IDs such as `boy_001`, `boy_002`, `girl_001` and `girl_002`. Generate small thumbnails for the selection grid and higher-resolution images for profile display.

    Example server asset layout:

    ```text
    server/
      assets/
        avatars/
          boys/
            boy_001.webp
            boy_002.webp
            thumbs/
          girls/
            girl_001.webp
            girl_002.webp
            thumbs/
    ```

    Keep this directory read-only for ordinary API requests. For persistent production hosting, use durable storage or your existing object-storage service.

* Step 3 — Create the avatar catalogue API

    Implement `GET /api/avatars?category=BOY` and `GET /api/avatars?category=GIRL`. Return the avatar IDs, names, categories and image references.

    Test both endpoints before integrating Android. Verify that missing IDs return a proper 404 rather than an internal server error.

* Step 4 — Add the database profile field

    Add or reuse a profile field named `avatar_id`. Keep the authenticated user's profile linked to the selected avatar ID. Use a foreign key or equivalent validation where practical.

    Do not copy image binaries or avatar URLs into the profile row. If your existing schema stores a URL, migrate it carefully and retain backward compatibility until old clients no longer depend on it.

* Step 5 — Implement avatar selection in Kotlin

    Use a ViewModel to fetch the catalogue, maintain the selected ID, apply filters and expose loading, success and error states. Use Coil to render image URLs with caching.

    A basic UI state can look like this:

    ```kotlin
    data class Avatar(
        val id: String,
        val name: String,
        val category: String,
        val imageUrl: String,
        val thumbnailUrl: String?
    )
    ```

    The `imageUrl` belongs to the fetched catalogue model, not the saved user profile model.

* Step 6 — Save the selection

    When the user taps Save, send the selected ID to the authenticated profile endpoint. The backend verifies that the avatar exists and updates the user's profile.

    On success, refresh the shared profile state so the same avatar appears in Home, Chat, Settings and call screens. On failure, preserve the previous saved selection.

* Step 7 — Implement pairing

    Add code generation/validation, QR scanning, request creation, accept/decline and mutual pairing updates. Use a server-side transaction or equivalent atomic operation for acceptance. Test with two separate accounts on two devices.

* Step 8 — Finish the remaining settings

    Add theme preferences, notification switches, privacy settings, server-health checks, About, Developer Profile and Logout. Remove obsolete onboarding routes only after testing fresh-install and returning-user flows.

* Step 9 — Test and deploy

    Test avatar selection, API errors, offline mode, caching, pairing and profile synchronization. Deploy the Node.js server and verify that its avatar files remain available after restart or redeployment.

### 5\. Database design example

A minimal profile table could contain:

```sql
CREATE TABLE profiles (
    user_id UUID PRIMARY KEY,
    display_name TEXT NOT NULL,
    avatar_id TEXT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

This is an illustrative starting point, not necessarily a migration to run directly against your existing schema. Preserve your existing foreign keys, RLS policies and profile columns.

For the curated catalogue, use either a server-managed manifest or a separate catalogue table:

```text
avatar_id       boy_001
display_name    Stylish Boy 1
category        BOY
asset_key       avatars/boys/boy_001.webp
is_active       true
```

The asset key is managed by the backend, not stored in the user's profile. The Android app receives the appropriate image reference from the catalogue API.

One final recommendation: pehle sirf boy/girl avatar API aur profile `avatar_id` persistence ko end-to-end complete karo. Jab ye two-device/profile-refresh tests pass kar le, tab baaki Settings screens implement karo. Isse UI aur backend bugs ko ek saath debug nahi karna padega.
