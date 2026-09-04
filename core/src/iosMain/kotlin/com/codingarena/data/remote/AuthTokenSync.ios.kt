package com.codingarena.data.remote

actual fun onAuthTokenChanged(token: String) {
    // No cross-window session to mirror to on iOS.
}
