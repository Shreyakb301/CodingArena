package com.codingarena.data.remote

/**
 * Called whenever [KtorClassroomGateway] saves or clears a session token.
 *
 * The token itself is always stored in [com.codingarena.domain.repository
 * .SettingsRepository], which every platform reads. On the web build this
 * additionally mirrors it into `localStorage`, where `sync.js` - a plain
 * script outside the Wasm bundle - can see it to start cross-device sync
 * without waiting for a page reload. Every other platform ignores it.
 */
expect fun onAuthTokenChanged(token: String)
