// Cross-device progress sync.
//
// The whole SQLite database is small, so instead of granular endpoints this
// just ships the file: on load, pull the server's snapshot if it is newer than
// ours (then reload so the DB worker picks it up); periodically and on tab-hide,
// push our current database up.
//
// The session token is mirrored into localStorage by the app (main.kt). Data
// lives in the same IndexedDB store the DB worker persists to.
(function () {
  "use strict";

  var TOKEN_KEY = "arena.token";
  var AT_KEY = "arena.snapshotAt";
  var API = location.origin + "/v1/progress/snapshot";
  var DB_NAME = "codingarena";
  var STORE = "sqlite";
  var KEY = "db";
  var PUSH_INTERVAL_MS = 45000;

  function openIdb() {
    return new Promise(function (resolve, reject) {
      var req = indexedDB.open(DB_NAME, 1);
      req.onupgradeneeded = function () { req.result.createObjectStore(STORE); };
      req.onsuccess = function () { resolve(req.result); };
      req.onerror = function () { reject(req.error); };
    });
  }

  function idbGet() {
    return openIdb().then(function (db) {
      return new Promise(function (resolve, reject) {
        var r = db.transaction(STORE, "readonly").objectStore(STORE).get(KEY);
        r.onsuccess = function () { resolve(r.result || null); };
        r.onerror = function () { reject(r.error); };
      });
    });
  }

  function idbPut(bytes) {
    return openIdb().then(function (db) {
      return new Promise(function (resolve, reject) {
        var tx = db.transaction(STORE, "readwrite");
        tx.objectStore(STORE).put(bytes, KEY);
        tx.oncomplete = function () { resolve(); };
        tx.onerror = function () { reject(tx.error); };
      });
    });
  }

  function localAt() { return Number(localStorage.getItem(AT_KEY)) || 0; }
  function setLocalAt(v) { localStorage.setItem(AT_KEY, String(v)); }

  function pull(token) {
    return fetch(API, { headers: { authorization: "Bearer " + token } }).then(function (res) {
      if (res.status === 404) return { seeded: false };
      if (res.status === 401) { localStorage.removeItem(TOKEN_KEY); return { unauthorised: true }; }
      if (!res.ok) return { error: res.status };
      var serverAt = Number(res.headers.get("x-snapshot-updated-at")) || 0;
      if (serverAt <= localAt()) return { upToDate: true };
      return res.arrayBuffer().then(function (buf) {
        return idbPut(buf).then(function () {
          setLocalAt(serverAt);
          location.reload();
          return { reloaded: true };
        });
      });
    });
  }

  function push(token) {
    return idbGet().then(function (bytes) {
      if (!bytes || bytes.byteLength < 512) return;
      var at = Date.now();
      return fetch(API, {
        method: "PUT",
        headers: {
          authorization: "Bearer " + token,
          "content-type": "application/octet-stream",
          "x-snapshot-updated-at": String(at),
        },
        body: bytes,
      }).then(function (res) {
        if (res.ok) setLocalAt(at);
        else if (res.status === 401) localStorage.removeItem(TOKEN_KEY);
      });
    });
  }

  function start() {
    var token = localStorage.getItem(TOKEN_KEY);
    if (!token) return;

    pull(token)
      .then(function (r) {
        // Only seed when the server has nothing; an up-to-date pull leaves
        // further pushes to the interval, so devices don't ping-pong reloads.
        if (r && r.seeded === false) return push(token);
      })
      .catch(function () { /* offline - try again on the interval */ });

    setInterval(function () {
      var t = localStorage.getItem(TOKEN_KEY);
      if (t) push(t).catch(function () {});
    }, PUSH_INTERVAL_MS);

    document.addEventListener("visibilitychange", function () {
      var t = localStorage.getItem(TOKEN_KEY);
      if (document.hidden && t) push(t).catch(function () {});
    });
  }

  // Give the app a moment to write its first state into IndexedDB.
  window.addEventListener("load", function () { setTimeout(start, 4000); });
})();
