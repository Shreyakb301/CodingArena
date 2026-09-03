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

## Deploy: Cloudflare Pages (via GitHub Actions)

The Java/Gradle build runs on GitHub's runners; Cloudflare only hosts the
static output. Config: `.github/workflows/deploy-web.yml` +
`composeApp/src/wasmJsMain/resources/_headers` (Wasm content-type + caching).

**One-time setup:**

1. Cloudflare dashboard -> **Workers & Pages** -> **Create** -> **Pages** ->
   **Direct Upload** -> name it `codingarena` -> skip the upload.
2. In the project's **Settings -> Builds & deployments**, set the production
   branch to `master`.
3. Create an API token: **My Profile -> API Tokens -> Create Token ->**
   "Cloudflare Pages" template (or a custom token with *Account -> Cloudflare
   Pages -> Edit*). Copy it.
4. Note your **Account ID** (right-hand side of the Workers & Pages page).
5. GitHub repo -> **Settings -> Secrets and variables -> Actions -> New
   repository secret**, add:
   - `CLOUDFLARE_API_TOKEN`
   - `CLOUDFLARE_ACCOUNT_ID`

**Deploy:** push to `master`, or run the *deploy web* workflow manually. The
URL is `https://codingarena.pages.dev` (plus a custom domain if you add one).

### Deploy by hand (no CI)

```bash
./gradlew :composeApp:wasmJsBrowserDistribution
npx wrangler pages deploy composeApp/build/dist/wasmJs/productionExecutable \
  --project-name=codingarena
```

(`npx wrangler login` first, or set `CLOUDFLARE_API_TOKEN`.)

### Other hosts

Any static host serves the `productionExecutable/` directory as-is - GitHub
Pages, Netlify, Vercel, S3. The `_headers` file is Cloudflare-specific
(Netlify reads it too); elsewhere the app still runs, it just misses the
long-cache hints.

---

## Install on iPhone

1. Open the deployed URL in **Safari** (must be Safari, not Chrome).
2. **Share → Add to Home Screen.**
3. Launch from the new icon - full screen, no browser bars.

Requires **iOS 18.2+** (Kotlin/Wasm needs the WasmGC feature Safari shipped in
18.2). Older iOS loads a blank page.

---

## Persistence caveat

The sql.js database currently runs **in memory**: progress survives navigation
within a session but is lost on a hard reload. Durable options, in order of
effort:

1. **Account + server sync** (already built) - the real answer for
   cross-device progress.
2. Swap the sql.js worker for an **OPFS-backed** SQLite worker so the local DB
   file persists. Isolated to `DatabaseDriverFactory.wasmJs.kt` + the worker
   script.

---

## What changed for this target

- SQLDelight generates in **async mode** (`generateAsync = true`) - the browser
  has no synchronous SQLite. JVM/iOS/Android adapt the schema back with
  `.synchronous()`; repositories use `awaitAsList()` etc.
- SQLDelight bumped `2.0.2 -> 2.1.0` (first release with a Wasm
  `web-worker-driver`).
- `:core` and `:composeApp` gained a `wasmJs` target; `:core` has a
  `wasmJsMain` `DatabaseDriverFactory`; `:composeApp` has `wasmJsMain/main.kt`.
