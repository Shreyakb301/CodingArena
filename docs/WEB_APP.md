# CodingArena as a web app (no App Store)

The browser target runs the **same** Compose UI as iOS, compiled to
Kotlin/Wasm. It installs to an iPhone home screen from Safari and never
expires - no Xcode, no Apple ID, no Mac needed to host it.

```
codingarena.web   settings.gradle.kts flag, default true
```

---

## Build

```bash
cd CodingArena
./gradlew :composeApp:wasmJsBrowserDistribution
```

Output (static files, deploy as-is):

```
composeApp/build/dist/wasmJs/productionExecutable/
├── index.html
├── codingarena.js              # loader
├── *.wasm                      # app + skiko
├── sqljs.worker.js             # database Web Worker (classic)
├── sql-wasm.js / sql-wasm.wasm # SQLite (sql.js)
├── manifest.webmanifest
├── sw.js                       # offline cache
├── _headers                    # Cloudflare: wasm mime + caching
└── icons/
```

Run it locally:

```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun --continuous
# serves on http://localhost:8080
```

Test on a phone on the same Wi-Fi: find the Mac's LAN IP
(`ipconfig getifaddr en0`) and open `http://<ip>:8080` in mobile Safari. Note
that iOS **only enables SQLite/OPFS and service workers over HTTPS or
`localhost`** - a plain `http://<lan-ip>` page still runs but has no
persistence and no offline cache. Use a real HTTPS deploy (below) for a true
device test.

---

## Deploy: everything on Cloudflare

The static app **and** the API run on one Cloudflare Pages project.
`wrangler.toml` holds the project name, the build-output dir, and the D1 / KV
bindings, so a deploy is just `wrangler pages deploy`.

| Piece | Where |
|---|---|
| Wasm app (static) | `composeApp/build/dist/wasmJs/productionExecutable/` |
| API (`/v1/*`) | `functions/` - TypeScript Pages Functions |
| Accounts | D1 database `codingarena` (`functions/schema.sql`) |
| Progress snapshots | KV namespace `SNAPSHOTS` |

**By hand:**

```bash
./gradlew :composeApp:wasmJsBrowserDistribution
npx wrangler pages deploy          # reads wrangler.toml
```

**CI:** `.github/workflows/deploy-web.yml` builds and deploys on push to
`master`. Needs repo secrets `CLOUDFLARE_API_TOKEN` (Pages + D1 edit) and
`CLOUDFLARE_ACCOUNT_ID`.

**Pages project secrets** (once, `wrangler pages secret put NAME`):

| Secret | Purpose |
|---|---|
| `JWT_SECRET` | signs session tokens |
| `APP_URL` | `https://codingarena.pages.dev` - OAuth redirects back here |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | enable "Continue with Google"; without them the routes return 501 |

Google Cloud: OAuth consent screen (External, `email`+`profile` scopes) + a
Web OAuth client whose redirect URI is
`https://codingarena.pages.dev/v1/auth/google/callback`.

---

## Install on iPhone

1. Open the deployed URL in **Safari** (must be Safari, not Chrome).
2. **Share → Add to Home Screen.**
3. Launch from the new icon - full screen, no browser bars.

Requires **iOS 18.2+** (Kotlin/Wasm needs the WasmGC feature Safari shipped in
18.2). Older iOS loads a blank page.

---

## Persistence & sync

**Local:** the sql.js worker snapshots the SQLite file to **IndexedDB** after
every write and reloads it on startup (`sqljs.worker.js`). Progress survives a
reload, a browser restart, and going offline. It can be evicted under heavy
storage pressure (`navigator.storage.persist()` reduces that risk).

**Cross-device:** when signed in, `sync.js` ships the whole database file to
`/v1/progress/snapshot` (a KV value per user). On load it pulls the server
copy if it is newer and reloads; otherwise it seeds/updates it, then pushes
every 45 s and on tab-hide.

Caveats:

- Whole-file sync, so two devices that both change things offline do **not**
  merge - the newer push wins. Sign in on your main device first.
- Switching to a device that is behind costs one reload while it pulls.
- The session token stays in localStorage, never in the synced database.

---

## What changed for this target

- SQLDelight generates in **async mode** (`generateAsync = true`) - the browser
  has no synchronous SQLite. JVM/iOS/Android adapt the schema back with
  `.synchronous()`; repositories use `awaitAsList()` etc.
- SQLDelight bumped `2.0.2 -> 2.1.0` (first release with a Wasm
  `web-worker-driver`).
- `:core` and `:composeApp` gained a `wasmJs` target; `:core` has a
  `wasmJsMain` `DatabaseDriverFactory`; `:composeApp` has `wasmJsMain/main.kt`.
- `kotlin.daemon.jvmargs` raised to 6g - the Wasm production compiler OOMs
  below that.

The Ktor `:server` still owns classrooms and code execution (Judge0) and is
unchanged; it is not deployed. Web auth is the Cloudflare functions.
