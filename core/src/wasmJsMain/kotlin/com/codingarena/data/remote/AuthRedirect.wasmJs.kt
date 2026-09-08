package com.codingarena.data.remote

actual fun startWebRedirect(url: String): Boolean {
    navigate(url)
    return true
}

@JsFun("(url) => { window.location.assign(url); }")
private external fun navigate(url: String)
