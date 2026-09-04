# Web app + accounts — session summary

Everything below is **live at https://codingarena.pages.dev** and committed to
`master`. One platform: Cloudflare (static app + API + database).

---

## What now works

| | Status |
|---|---|
| Installable web app (Kotlin/Wasm, same UI as iOS) | ✅ live, add-to-home-screen |
| Local persistence (survives reload/restart/offline) | ✅ verified in a headless browser |
| Email + password accounts | ✅ verified against prod (register / login / bad password / dupe) |
| Snapshot sync transport (`/v1/progress/snapshot`) | ✅ verified byte-exact round-trip; a real device's DB uploads and a fresh device pulls + boots on it |
| Cross-device sync end-to-end | ◑ pieces verified individually; the full A→B content propagation I could not fully automate — **worth a real 2-device check** |
| "Continue with Google" | ⏳ code deployed & returns 501 until you add the Google OAuth client |
| Auto-deploy on `git push` | ⏳ needs 2 GitHub secrets |

---

## Two things left for you

### 1. Google OAuth client (~15 min) — unlocks "Continue with Google"

- **console.cloud.google.com** → new project → **APIs & Services → OAuth
  consent screen**: External, add scopes `userinfo.email` + `userinfo.profile`,
  add your Gmail as a test user.
- **Credentials → Create → OAuth client ID → Web application**. Authorized
  redirect URI: `https://codingarena.pages.dev/v1/auth/google/callback`.
- Set the two secrets (no redeploy needed):
  ```
  npx wrangler pages secret put GOOGLE_CLIENT_ID --project-name codingarena
  npx wrangler pages secret put GOOGLE_CLIENT_SECRET --project-name codingarena
  ```

### 2. GitHub secrets — unlocks push-to-deploy

Repo **Settings → Secrets and variables → Actions**:
- `CLOUDFLARE_API_TOKEN` — token with **Cloudflare Pages: Edit** (and D1: Edit)
- `CLOUDFLARE_ACCOUNT_ID` — `329b19534285cbcb8cd90661e60b750c`

Until then, deploy by hand: `./gradlew :composeApp:wasmJsBrowserDistribution && npx wrangler pages deploy`

---

## How it's built

```
codingarena.pages.dev
├── /                     static Kotlin/Wasm app  (composeApp/build/dist/wasmJs/…)
├── /v1/auth/*            Pages Functions (TypeScript)  ── functions/
│     register, login, google/start, google/callback
├── /v1/progress/snapshot GET/PUT the whole DB file for sync
├── D1  "codingarena"     accounts                       ── functions/schema.sql
└── KV  "SNAPSHOTS"        one DB snapshot per user
```

- **Session token** lives in `localStorage` only (never in the synced DB), and
  is copied into the app's settings on each boot.
- **Sync** (`sync.js`, loaded by `index.html`): pull the server snapshot if
  newer → reload; else seed it; then push every 45 s and on tab-hide. Whole-file,
  so it doesn't merge two devices edited offline — sign in on your main device
  first. Switching to a stale device costs one reload.

---

## Code changes this session (commits on `master`)

Web target:
- `:core` + `:composeApp` gained a `wasmJs` target; SQLDelight → async codegen,
  bumped `2.0.2 → 2.1.0`.
- Browser `DatabaseDriverFactory` (sql.js in a Web Worker, snapshotted to
  IndexedDB), `main.kt` entry point, PWA shell, service worker.
- `kotlin.daemon.jvmargs` → 6g (Wasm compiler OOMs below that).

UI:
- Screen transitions in `NavHost`.
- Onboarding redesigned: centred, card options, segmented progress, full-width
  Continue; all step subtitles removed; "Continue with Google" on the welcome
  step.
- Desktop: UI held to a 460dp phone-width column.

Backend (new, `functions/`):
- Auth + snapshot sync as Cloudflare Pages Functions + D1 + KV.
- HS256 JWT, PBKDF2 passwords, Google OAuth code flow — all Web Crypto, ~470
  lines total.

Removed:
- The parallel Ktor server auth work (CORS + Google OAuth, ~320 lines) — the
  Cloudflare functions replace it. `:server` still owns classrooms + code
  execution (Judge0) and is unchanged/undeployed.
- `functions/v1/_debug.ts` diagnostic endpoint.

Full detail: `git log 5550545..HEAD` and `docs/WEB_APP.md`.

---

## Known limitations / next steps

- **Free-tier eviction:** D1 and KV are on the free plan; heavy inactivity or
  the 100 k-writes/day KV limit could bite a real user base. Fine for you + a
  few testers.
- **Google consent screen** stays in "Testing" mode — only added test users can
  sign in (up to 100) until you submit it for verification. `email`/`profile`
  are not "sensitive" scopes, so verification is light.
- **No merge on conflict:** two devices both editing offline → newer push wins.
- **iOS app:** the Ktor/Google work was reverted; when you wire iOS auth it can
  point at the same Cloudflare endpoints (`ArenaServerConfig`).
- The `:server` module (classrooms, Judge0) is untouched and still needs its own
  host if you want those features on web.
