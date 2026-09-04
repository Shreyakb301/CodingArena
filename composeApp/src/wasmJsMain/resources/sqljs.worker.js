// Classic Web Worker backing SQLDelight's WebWorkerDriver (2.1.0 wasmJs).
//
// Protocol (verified against the driver source):
//   in : { id, action: "exec"|"begin_transaction"|"end_transaction"
//              |"rollback_transaction", sql, params }
//   out: { id, results: { values: [[...]] } }   or   { id, error: {...} }
//
// The sql.js database lives in memory, but it is snapshotted to IndexedDB
// after every write (debounced) and reloaded on startup, so progress survives
// a reload, a browser restart, and going offline. The DB is small, so
// exporting the whole file each time is cheap.
//
// sql-wasm.js / sql-wasm.wasm are copied next to the bundle by
// webpack.config.d/sqljs.js.

importScripts("./sql-wasm.js");

const IDB_NAME = "codingarena";
const IDB_STORE = "sqlite";
const IDB_KEY = "db";

function openIdb() {
    return new Promise((resolve, reject) => {
        const req = indexedDB.open(IDB_NAME, 1);
        req.onupgradeneeded = () => req.result.createObjectStore(IDB_STORE);
        req.onsuccess = () => resolve(req.result);
        req.onerror = () => reject(req.error);
    });
}

async function loadSnapshot() {
    try {
        const idb = await openIdb();
        return await new Promise((resolve, reject) => {
            const req = idb.transaction(IDB_STORE, "readonly").objectStore(IDB_STORE).get(IDB_KEY);
            req.onsuccess = () => resolve(req.result || null);
            req.onerror = () => reject(req.error);
        });
    } catch (e) {
        return null; // private window, blocked storage - fall back to in-memory
    }
}

async function saveSnapshot(bytes) {
    try {
        const idb = await openIdb();
        await new Promise((resolve, reject) => {
            const tx = idb.transaction(IDB_STORE, "readwrite");
            tx.objectStore(IDB_STORE).put(bytes, IDB_KEY);
            tx.oncomplete = () => resolve();
            tx.onerror = () => reject(tx.error);
        });
    } catch (e) {
        // Storage full or evicted - the session keeps working from memory.
    }
}

let db = null;
let saveTimer = null;

function scheduleSave() {
    clearTimeout(saveTimer);
    saveTimer = setTimeout(() => {
        if (db) saveSnapshot(db.export());
    }, 400);
}

const WRITE = /^\s*(INSERT|UPDATE|DELETE|REPLACE|CREATE|DROP|ALTER|PRAGMA\s+user_version\s*=)/i;

const ready = (async () => {
    const SQL = await initSqlJs({ locateFile: () => "./sql-wasm.wasm" });
    const snapshot = await loadSnapshot();
    db = snapshot ? new SQL.Database(snapshot) : new SQL.Database();
})();

function handle(data) {
    switch (data && data.action) {
        case "exec": {
            if (!data.sql) throw new Error("exec: Missing query string");
            const results = db.exec(data.sql, data.params)[0] ?? { values: [] };
            if (WRITE.test(data.sql)) scheduleSave();
            return { id: data.id, results };
        }
        case "begin_transaction":
            db.exec("BEGIN TRANSACTION;");
            return { id: data.id, results: { values: [] } };
        case "end_transaction":
            db.exec("COMMIT;");
            scheduleSave();
            return { id: data.id, results: { values: [] } };
        case "rollback_transaction":
            db.exec("ROLLBACK;");
            return { id: data.id, results: { values: [] } };
        default:
            throw new Error(`Unsupported action: ${data && data.action}`);
    }
}

self.onmessage = (event) => {
    const data = event.data;
    ready
        .then(() => {
            try {
                postMessage(handle(data));
            } catch (err) {
                postMessage({ id: data.id, error: { message: String((err && err.message) || err), name: "Error" } });
            }
        })
        .catch((err) => postMessage({ id: data.id, error: { message: String(err), name: "Error" } }));
};
