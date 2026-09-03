// sql.js ships SQLite as sql-wasm.js (Emscripten loader) + sql-wasm.wasm.
// Our classic Web Worker (src/wasmJsMain/resources/sqljs.worker.js) pulls them
// in with importScripts, so both files must sit next to the bundle. Webpack
// does not know about them otherwise.
const CopyWebpackPlugin = require("copy-webpack-plugin");

config.plugins.push(
    new CopyWebpackPlugin({
        patterns: [
            { from: require.resolve("sql.js/dist/sql-wasm.wasm"), to: "sql-wasm.wasm" },
            { from: require.resolve("sql.js/dist/sql-wasm.js"), to: "sql-wasm.js" },
        ],
    }),
);
