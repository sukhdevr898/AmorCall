Sukh, Duora ke liye dono changes ko ek hi implementation plan mein cover karte hain:

* Pairing system: exactly 6-character alphanumeric code, automatic QR generation, QR scanning, code copy/share aur QR download.

* Server File Manager Web App: browser se avatars aur server files dekhna, upload, rename, edit aur delete karna — bina har baar Node.js server rebuild kiye.

Ek important distinction: avatar add karne ke liye server restart ya rebuild ki zaroorat nahi honi chahiye. Agar Node.js server par persistent storage aur dynamic avatar catalogue use karte ho, to naya avatar upload hote hi app ki catalogue refresh ho sakti hai. Lekin server ke executable code ko edit karne par restart/redeploy ki zaroorat pad sakti hai.

### 1\. Updated pairing UI and logic

![Dating App flutter UI Kit | Happn | Tinder Clone by mvp_minds | CodeCanyon](https://images.openai.com/static-rsc-4/sZhczkLOAQx44_M_BHgMRmX4yrjuKSw1VkJnlt58Dp5D90m1do-v6RS--5y43t7w4a-9rPc2vLdA3Xerp4M4qDJG394SmmAAwJonNJm9FdiCnc3LFRqxO8fBEYvi6biR5xWpeNE-ejWcgBhHNg0Dnud0NUjm7wknMtwwgK2GE1bZlOPYw8Pmifuh6g8Yq0W2?purpose=fullsize)

A. Enter Partner Code

* Exactly 6 characters: `A–Z` and `0–9`.

* Six input boxes, one character per box.

* Auto-advance, backspace handling and paste support.

* Case-insensitive validation.

* Send Request button enabled only for a valid 6-character code.

![Moxie Robot App | Empower Your Child’s Social & Emotional Skills](https://images.openai.com/static-rsc-4/hKxb-ufCAGD-sBoKEih4AyC-TZ2_AQ1OleXsKfJatPni2Dqhr0BoLu6_KnGR1kLxUTLER5bXMzbaWFtWsuoAv9gARZxBlD_kSFnMkB729HOBcBxP0ILcm9rCrXXXJVXHF5uas4xNhznPPVW5VlQnBTgyd1FqmuP3cp2S0UG4Av2Sng9q9LhcwVmJlecbLSY4?purpose=fullsize)

B. My Pairing Code + QR

* Generate a unique pairing code automatically.

* Automatically display its QR code.

* Copy code, share code and download QR as PNG.

* Show expiry and regenerate option.

* QR and manual code must resolve to the same pairing invitation.

![Couple - Be closer together by David Filla](https://images.openai.com/static-rsc-4/yw5LyCk4NmG0fgzFoX0GXU4htUEoNPPBKFgcO7INuJZctb_gcoOPOd9u4J7feHELBakuo9BPyLi3lXiLl9HwNCPnpFXmzkusaDl621IsWzuj5EeRmC2rZLsnqeNrcps_woGnTZ1PtJ28_0bA_zntgzJU364VDb93u6jPeDJiiDh_UMLIGCv6u-4qP0ruQceS?purpose=fullsize)

C. Scan Partner QR

* Open the camera scanner.

* Detect and validate the pairing QR automatically.

* Display the partner's verified profile before sending the request.

* Handle invalid, expired and already-used codes.

* Ask for confirmation before sending a pairing request.

### Recommended code format

Example: `A7K2M9`

* Length: exactly 6 characters.

* Allowed characters: uppercase letters `A–Z` and digits `0–9`.

* Exclude confusing characters such as `O/0` and `I/1` if desired.

* Generate codes securely on the server.

* Enforce uniqueness, expiry, rate limits and single-use redemption.

* Do not pair users automatically just because someone scanned a QR. The recipient must still accept the pairing request.

Security note: six characters are convenient but have limited entropy. Rate limiting and server-side validation are essential. The QR should contain a short-lived, opaque invitation token, not a user's private ID or credentials.

### 2\. Master prompt — Pairing system + Server File Manager

Copy this entire prompt into your coding AI and attach/open your existing Duora Android and Node.js server projects.

# Duora Upgrade: Six-Character Pairing, QR Invitations and Admin File Manager

Act as a senior Kotlin Android developer, Node.js backend engineer, database architect and application security engineer.

Upgrade my existing Duora project without replacing working features. First inspect the Android app, Node.js server, database schema, authentication middleware, avatar catalogue, deployment configuration and existing pairing implementation.

Implement the following requirements in phases. Reuse existing APIs and database structures where practical. Do not invent working endpoints or claim features are complete without testing.

# PHASE 1 — Fix Partner Code Input

## 1. Required code format

Replace the existing oversized partner code with exactly six alphanumeric characters.

Example: A7K2M9

Rules:
- Exactly six characters.
- Uppercase A–Z and digits 0–9.
- Normalize lowercase input to uppercase.
- Optionally exclude visually confusing characters such as O, 0, I and 1.
- Generate codes on the trusted backend, not randomly in the Android UI.
- Enforce uniqueness among active invitations.
- Reject expired, revoked, consumed or invalid codes.
- Do not allow a user to pair with themselves.

## 2. Android input UI

Replace the existing oversized input fields with six compact, evenly spaced character boxes.

Implement:
- Automatic focus movement to the next box.
- Backspace movement to the previous box.
- Paste handling for a complete six-character code.
- Keyboard input restricted to allowed characters.
- Automatic uppercase conversion.
- Clear focus and keyboard dismissal when appropriate.
- Loading indicator while validating.
- Inline errors for invalid or expired codes.
- Disabled Send Request button until the input is valid.

Do not create six independent backend requests. Collect the six characters into one code and submit one request.

Ensure the layout works on narrow Android screens, with keyboard insets handled correctly.

# PHASE 2 — Automatic Pairing QR Generation

## 1. Generate invitation

Create a backend endpoint or reuse an existing equivalent to create a pairing invitation.

The backend must:
- Authenticate the requesting user.
- Check whether the user is already paired.
- Generate a secure random six-character code.
- Ensure active-code uniqueness.
- Associate the invitation with the authenticated account.
- Assign an expiry timestamp.
- Store only the necessary invitation metadata.
- Return the code, expiry and a QR payload or opaque invitation token.

Do not expose private account IDs, access tokens or database credentials in the QR payload.

A QR payload should contain an opaque, short-lived invitation reference or signed invitation token. Do not embed arbitrary URLs supplied by users.

The six-character code and QR invitation must resolve to the same invitation workflow.

## 2. QR display screen

Create a premium Duora screen containing:
- Title: “Connect with Your Love 💕”.
- Six-character pairing code.
- Automatically generated QR image.
- Expiry countdown or human-readable expiry.
- Copy Code action.
- Share Code action.
- Download QR action.
- Regenerate Code action.
- Loading, expired and error states.

Generate the QR as soon as the backend returns a valid invitation. Do not require the user to press a separate Generate QR button.

If the invitation expires, stop presenting it as valid and provide a Regenerate option.

Do not generate an unlimited number of active invitations for the same account.

## 3. Download QR as PNG

Allow the user to save the generated QR code as a PNG image.

Requirements:
- High enough resolution for reliable scanning.
- White margin/quiet zone around the QR code.
- Strong contrast.
- Optional Duora heart mark in the center only if it does not impair scanning.
- No transparent or busy background that makes the code difficult to read.
- Use Android's appropriate media/file-saving APIs.
- Request only necessary permissions.
- Handle cancellation, insufficient storage and save failures.
- Provide confirmation after the image is successfully saved.

The downloaded image may contain the invitation QR and optionally the short code. It must not expose account credentials.

## 4. Share invitation

Support Android Sharesheet for sharing the code or saved QR image.

Do not automatically send an invitation to a contact or grant pairing access merely because the user shared the image.

# PHASE 3 — Scan Partner QR

Use CameraX and a maintained QR/barcode scanning library compatible with the existing Android project.

Implement:
- Camera permission request when the scanner is opened.
- Live camera preview.
- Scanning frame and guidance text.
- Flashlight toggle where supported.
- Back and cancel actions.
- Automatic detection of a valid QR.
- Duplicate-scan prevention.
- Loading while validating the invitation.
- Invalid, expired and revoked invitation states.

After scanning:
1. Extract the opaque invitation reference.
2. Submit it to the authenticated backend.
3. Validate its signature or token and expiry.
4. Verify that the invitation is active and the users are eligible to pair.
5. Display the verified partner profile.
6. Ask the user to confirm sending a pairing request.
7. Create a pending request only after confirmation.

Do not automatically accept a pairing request or establish a relationship after scanning.

Release the camera when the screen closes or scanning completes.

# PHASE 4 — Pairing Request and Acceptance

Implement a complete two-user workflow:

1. User A creates an invitation.
2. User B enters the six-character code or scans the QR.
3. The backend validates the invitation.
4. User B reviews the verified identity of User A.
5. User B sends a pairing request.
6. User A receives an in-app or push notification.
7. User A accepts or declines.
8. On acceptance, the backend atomically establishes the mutual relationship.
9. Both users' partner profiles refresh.
10. Their partner names and avatars update across Home, Chat, Calls and Settings.

Enforce:
- No self-pairing.
- No duplicate pending requests.
- No pairing to multiple partners if the product permits only one partner.
- No expired or revoked invitation use.
- No unauthorized acceptance.
- No race condition when two requests are accepted simultaneously.

The backend is the source of truth. Sending a request is not equivalent to successful pairing.

# PHASE 5 — Node.js Avatar Catalogue and File Manager

Build a separate, authenticated web-based administration panel for managing Duora avatar assets and permitted server files.

This is an administrator tool, not a public user feature.

## 1. Admin web UI

Create a responsive web app with:
- Secure admin login.
- Dashboard overview.
- Avatar Library.
- File Browser.
- Upload Files.
- Edit Text File.
- Rename.
- Delete with confirmation.
- Search and filter.
- File details and upload status.
- Pagination for large directories.
- Error and empty states.
- Audit/activity history.

Use the existing web stack if one exists. Otherwise, use a lightweight React frontend or a server-rendered admin page compatible with the current Node.js project.

Keep the UI separate from the public avatar catalogue API.

## 2. File browser

Allow authorized administrators to browse only approved, configured storage roots.

Display:
- Relative file path.
- Filename.
- File type.
- File size.
- Last-modified timestamp.
- Avatar category where applicable.

Support:
- Opening directories.
- Searching filenames.
- Viewing image thumbnails.
- Previewing supported text files.
- Renaming permitted files.
- Uploading new assets.
- Editing supported text files.
- Deleting permitted files after confirmation.

Do not expose arbitrary operating-system directories.

Never provide a general-purpose terminal, shell command execution or unrestricted filesystem access through the browser.

## 3. File editing restrictions

Allow text editing only for explicitly approved file types and locations, such as selected JSON catalogue manifests or supported configuration files.

Use a strict allowlist for editable files.

Do not expose:
- .env files.
- Secrets and credentials.
- Private keys or certificates.
- Database dumps.
- Authentication tokens.
- Arbitrary executable files.
- package manager lockfiles or package scripts through the ordinary editor.
- System configuration outside the approved storage root.

Do not allow users to edit arbitrary JavaScript or server source code through the public-facing avatar manager.

If server source-code administration is genuinely required, keep it as a separate restricted deployment workflow with stronger authorization, backups and rollback.

## 4. Secure file operations

For every file operation:
- Require an authenticated administrator.
- Check the administrator's role and permissions server-side.
- Resolve and validate the requested path against an approved root.
- Reject path traversal, symbolic-link escapes and encoded traversal attempts.
- Reject unexpected extensions and MIME types.
- Validate file sizes and image dimensions.
- Generate safe server-side filenames where appropriate.
- Never trust client-provided filenames as filesystem paths.
- Prevent overwriting protected files.
- Write updates atomically where practical.
- Maintain audit records for uploads, edits, renames and deletions.
- Require confirmation for destructive actions.
- Apply rate limits to sensitive endpoints.

Use strict file permissions and ensure the application process cannot modify unrelated system files.

## 5. Avatar upload workflow

Allow an administrator to upload new avatar images into:
- Boys.
- Girls.

Optional metadata:
- Avatar display name.
- Stable avatar ID.
- Category.
- Style tags.
- Active/inactive status.

On upload:
1. Authenticate the administrator.
2. Validate file format, size and image dimensions.
3. Decode and re-encode supported images where practical.
4. Generate an optimized thumbnail.
5. Assign or validate a stable avatar ID.
6. Store the image in persistent storage.
7. Update the catalogue metadata.
8. Make the avatar available through the existing catalogue API.
9. Invalidate or update catalogue caches.
10. Return a success response only after the asset and metadata are committed.

Do not overwrite an existing avatar silently. If replacing an asset, create a backup or retain a previous version and update cache versions.

## 6. Dynamic avatar catalogue

The Android app must fetch avatar metadata from the existing Node.js API.

Use stable avatar IDs, for example:
- boy_001
- boy_002
- girl_001
- girl_002

The user's profile stores the selected avatar ID, not image bytes or a duplicated image URL.

The API resolves the ID to the current image reference.

After an administrator uploads a new avatar, it should become available without rebuilding the Android APK or restarting the server, provided that the catalogue is dynamically loaded and storage is persistent.

Implement appropriate catalogue cache invalidation or a version/ETag strategy.

Existing selected avatar IDs must remain valid after adding or reordering avatars.

# PHASE 6 — Persistence and Deployment

Inspect the current server hosting and storage configuration.

The uploaded assets must survive:
- Server restarts.
- Process crashes.
- Application redeployment.
- Container replacement where applicable.

Do not store permanent assets only inside a temporary directory or a deployment filesystem that is discarded on redeploy.

If the deployment platform uses ephemeral storage, configure a persistent volume or object storage such as the project's existing Cloudflare R2 integration.

Serve static images with correct content types, caching headers and stable asset references.

Do not rebuild the server for ordinary avatar uploads or metadata changes. Restart/redeploy only when actual server code or deployment configuration changes require it.

# PHASE 7 — Suggested API contracts

Adapt the endpoint names to the existing project. Do not duplicate existing equivalent endpoints.

Pairing:
- POST /api/pairing/invitations
- GET /api/pairing/invitations/current
- POST /api/pairing/resolve-code
- POST /api/pairing/resolve-qr
- POST /api/pairing/requests
- POST /api/pairing/requests/:requestId/accept
- POST /api/pairing/requests/:requestId/decline
- DELETE /api/pairing/invitations/current

Admin:
- GET /api/admin/files
- GET /api/admin/files/preview
- POST /api/admin/files/upload
- PATCH /api/admin/files/rename
- PATCH /api/admin/files/edit
- DELETE /api/admin/files
- GET /api/admin/avatars
- POST /api/admin/avatars
- PATCH /api/admin/avatars/:avatarId
- DELETE /api/admin/avatars/:avatarId

These are suggested contracts, not existing deployed endpoints. Implement and document the actual request/response schemas, authentication rules and status codes.

# PHASE 8 — Testing

Pairing tests:
1. Valid six-character code works.
2. Five- and seven-character codes are rejected.
3. Lowercase input normalizes correctly.
4. Paste, auto-advance and backspace work.
5. QR generation succeeds after invitation creation.
6. Downloaded QR can be scanned by another device.
7. Expired and revoked codes fail.
8. Duplicate requests are rejected.
9. Pairing succeeds only after acceptance.
10. Concurrent acceptance cannot create conflicting relationships.

Admin tests:
1. Unauthorized users cannot access the dashboard or API.
2. Approved administrators can upload boy and girl avatars.
3. New avatars appear in the Android catalogue without an APK rebuild.
4. Assets persist after server restart and redeployment.
5. Rename and delete operate only within approved storage roots.
6. Path traversal and unauthorized file edits are rejected.
7. Protected files and secrets cannot be viewed or modified.
8. Failed uploads do not create broken catalogue entries.
9. Audit logs record administrative changes.
10. The app can fetch and display newly uploaded avatars.

Provide complete source code, database migrations, environment configuration, security policies, setup instructions and test results. Do not fabricate test results or expose secrets in logs.

# Final deliverables

- Updated Kotlin pairing UI.
- QR generation, scanning, download and share functionality.
- Updated Node.js pairing endpoints.
- Database changes where necessary.
- Authenticated admin web application.
- Secure file and avatar management API.
- Dynamic avatar catalogue integration.
- Deployment and backup instructions.
- A list of changed files and verified tests.

### 3\. Step-by-step instructions

### Phase A — Fix pairing code

Step 1: Update Kotlin input

Existing 6-field UI ko retain karo, lekin har field mein sirf ek allowed character accept ho. Full code paste karne par chhe fields automatically populate hon.

Step 2: Update Node.js pairing logic

Server par secure code generate karo, expiry store karo aur duplicate active codes reject karo. Existing database aur authentication middleware ko reuse karo.

Step 3: QR generation

Invitation API se valid token milte hi QR generate karo. Android par ZXing jaisi compatible library se bitmap bana sakte ho; server-side generation bhi possible hai. Ek hi approach choose karo aur duplicate QR logic mat banao.

Step 4: QR scan, download aur share

Scanner camera se token read kare, backend validate kare, aur user confirmation ke baad pairing request create ho. QR PNG ko Android Sharesheet aur system save flow se export karo.

Useful references:

* [ZXing — QR generation and scanning](https://github.com/zxing/zxing?utm_source=chatgpt.com)

* [Android media picker and storage guidance](https://developer.android.com/training/data-storage/shared/photopicker?utm_source=chatgpt.com)

### Phase B — Server File Manager Web App

Step 5: Create the admin dashboard

Node.js project ke andar ek separate admin frontend banao. Dashboard mein Avatar Library, Files, Uploads aur Activity Log sections rakho.

Step 6: Secure the admin API

Har admin endpoint par server-side authentication aur role authorization check karo. Public avatar API ko admin file-management permissions mat do. Path traversal aur arbitrary file access block karo.

Step 7: Implement avatar upload

Admin image upload kare → backend validate kare → image aur thumbnail persistent storage mein save hon → catalogue metadata update ho → Android app refreshed catalogue fetch kare.

Step 8: Test without rebuild

Ek naya avatar upload karo. Android app mein catalogue refresh karo aur verify karo ki image select ki ja sakti hai. APK rebuild ya server restart ki zaroorat nahi honi chahiye for normal avatar uploads.

### Recommended storage structure

```text
duora-server/
├── src/
│   ├── routes/
│   │   ├── pairing.routes.js
│   │   ├── avatar.routes.js
│   │   └── admin.routes.js
│   ├── controllers/
│   ├── services/
│   ├── middleware/
│   │   └── requireAdmin.js
│   └── app.js
├── admin-web/
│   ├── src/
│   └── package.json
└── storage/
    └── avatars/
        ├── boys/
        ├── girls/
        └── thumbnails/
```

Ye illustrative structure hai. Agar tumhara existing server TypeScript, Express, Fastify ya kisi aur structure mein hai, to usi conventions ko follow karna.

Production note: `storage/` ko durable volume ya object storage se connect karo. Agar hosting par filesystem ephemeral hai, to deployment ke baad images delete ho sakti hain. Aise environment mein Cloudflare R2 ya equivalent persistent object storage better rahega.

### 4\. Final testing checklist

Verification checklist

0/10

Six-character code input, paste aur validation working

QR automatic generate hota hai aur doosre device se scan hota hai

QR PNG save aur share hota hai

Expired code aur duplicate requests reject hote hain

Pairing recipient ke accept karne ke baad hi complete hoti hai

Admin login aur role authorization enforced hai

New avatar upload ke baad Android catalogue refresh hoti hai

Uploaded avatars server restart/redeploy ke baad bhi available hain

Rename/delete sirf approved files par kaam karte hain

Protected files, secrets aur arbitrary paths inaccessible hain

Meri recommendation: pehle pairing API + six-character code + QR generation/scanning complete karo. Uske baad admin dashboard aur avatar uploads implement karo. Is order mein tum pehle two-device pairing verify kar sakte ho, phir server file management ko safely add kar sakte ho.
