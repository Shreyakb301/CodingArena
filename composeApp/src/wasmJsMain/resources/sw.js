// Offline support without getting stuck on a stale build.
//
// - Content-hashed files (*.wasm, webpack chunks) never change under their
//   name, so they are cache-first and kept forever.
// - Everything referenced by a fixed name (the HTML shell, codingarena.js, the
//   sql.js worker) is network-first: a fresh deploy is picked up on the next
//   load, and the cached copy is only a fallback when offline.
const CACHE = "codingarena-v2";
const IMMUTABLE = /\.(wasm)$|^[a-f0-9]{16,}\./;

self.addEventListener("install", () => self.skipWaiting());

self.addEventListener("activate", (event) => {
    event.waitUntil(
        caches.keys()
            .then((keys) => Promise.all(keys.filter((k) => k !== CACHE).map((k) => caches.delete(k))))
            .then(() => self.clients.claim()),
    );
});

self.addEventListener("fetch", (event) => {
    const req = event.request;
    if (req.method !== "GET") return;
    const url = new URL(req.url);
    if (url.origin !== self.location.origin) return;

    const name = url.pathname.split("/").pop() || "";
    const immutable = IMMUTABLE.test(name);

    event.respondWith(
        immutable ? cacheFirst(req) : networkFirst(req),
    );
});

async function cacheFirst(req) {
    const hit = await caches.match(req);
    if (hit) return hit;
    const res = await fetch(req);
    if (res.ok) (await caches.open(CACHE)).put(req, res.clone());
    return res;
}

async function networkFirst(req) {
    try {
        const res = await fetch(req);
        if (res.ok && res.type === "basic") {
            (await caches.open(CACHE)).put(req, res.clone());
        }
        return res;
    } catch (err) {
        const hit = await caches.match(req);
        if (hit) return hit;
        throw err;
    }
}
