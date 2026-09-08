package com.codingarena.data.remote

/**
 * Sends the browser to the server-side OAuth start URL as a full-page
 * navigation, so the flow returns to the same tab with its `#token=` fragment.
 *
 * Opening it in a new tab (the default [androidx.compose.ui.platform.UriHandler]
 * behaviour) leaves the original tab stuck on whatever screen it was showing.
 * Returns false on every non-web platform, where the caller falls back to the
 * system browser.
 */
expect fun startWebRedirect(url: String): Boolean
