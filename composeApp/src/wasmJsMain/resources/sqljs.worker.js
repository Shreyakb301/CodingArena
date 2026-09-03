// Classic Web Worker backing SQLDelight's WebWorkerDriver.
//
// This is the @cashapp/sqldelight-sqljs-worker protocol, but self-contained:
// it loads sql.js with importScripts instead of a bundler `import`, so it can
// be shipped as a plain static file and needs no worker bundling step.
// sql-wasm.js and sql-wasm.wasm are copied next to the bundle by
// webpack.config.d/sqljs.js.

importScripts("./sql-wasm.js");

let db = null;

const ready = initSqlJs({ locateFile: () => "./sql-wasm.wasm" }).then((SQL) => {
    db = new SQL.Database();
});

function onModuleReady() {
    const data = this.data;
    switch (data && data.action) {
        case "exec":
            if (!data.sql) throw new Error("exec: Missing query string");
            return postMessage({
                id: data.id,
                results: db.exec(data.sql, data.params)[0] ?? { values: [] },
            });
        case "begin_transaction":
            return postMessage({ id: data.id, results: db.exec("BEGIN TRANSACTION;") });
        case "end_transaction":
            return postMessage({ id: data.id, results: db.exec("END TRANSACTION;") });
        case "rollback_transaction":
            return postMessage({ id: data.id, results: db.exec("ROLLBACK TRANSACTION;") });
        default:
            throw new Error(`Unsupported action: ${data && data.action}`);
    }
}

function onError(err) {
    return postMessage({ id: this.data.id, error: err });
}

self.onmessage = (event) =>
    ready.then(onModuleReady.bind(event)).catch(onError.bind(event));
