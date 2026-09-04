package com.codingarena.data.remote

/**
 * Mirrors into `localStorage` under the same key `main.kt` and `sync.js` use.
 * An empty token (sign-out) removes it. This is what lets an email/password
 * login - not just the Google redirect - start cross-device sync.
 */
actual fun onAuthTokenChanged(token: String) {
    setArenaToken(token)
}

@JsFun("(value) => { if (value) localStorage.setItem('arena.token', value); else localStorage.removeItem('arena.token'); }")
private external fun setArenaToken(value: String)
