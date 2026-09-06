import type { Env } from "../../../_lib/db.ts";
import { linkOrCreateGoogleUser } from "../../../_lib/db.ts";
import { getCookie, redirectToApp } from "../../../_lib/http.ts";
import { decodeJwt, signJwt } from "../../../_lib/jwt.ts";

// Google OAuth 2.0 authorization-code flow. The id_token is decoded (not
// signature-verified): it is fetched directly from Google over TLS in the code
// exchange, and we check its audience.
export const onRequestGet: PagesFunction<Env> = async ({ request, env }) => {
  const url = new URL(request.url);
  const appUrl = (env.APP_URL || url.origin).replace(/\/$/, "");
  const fail = (reason: string) => redirectToApp(appUrl, `auth_error=${encodeURIComponent(reason)}`);

  if (!env.GOOGLE_CLIENT_ID || !env.GOOGLE_CLIENT_SECRET) return fail("not_configured");
  if (url.searchParams.get("error")) return fail(url.searchParams.get("error")!);

  const code = url.searchParams.get("code");
  if (!code) return fail("missing_code");

  const state = url.searchParams.get("state");
  const expectedState = getCookie(request, "arena_oauth_state");
  if (!expectedState || state !== expectedState) return fail("bad_state");

  let idToken: string | undefined;
  try {
    const res = await fetch("https://oauth2.googleapis.com/token", {
      method: "POST",
      headers: { "content-type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({
        code,
        client_id: env.GOOGLE_CLIENT_ID,
        client_secret: env.GOOGLE_CLIENT_SECRET,
        redirect_uri: `${url.origin}/v1/auth/google/callback`,
        grant_type: "authorization_code",
      }),
    });
    if (!res.ok) return fail("token_exchange_failed");
    idToken = ((await res.json()) as { id_token?: string }).id_token;
  } catch {
    return fail("token_exchange_failed");
  }
  if (!idToken) return fail("no_id_token");

  let claims: Record<string, unknown>;
  try {
    claims = decodeJwt(idToken);
  } catch {
    return fail("bad_id_token");
  }
  if (claims.aud !== env.GOOGLE_CLIENT_ID) return fail("bad_audience");
  if (claims.iss !== "https://accounts.google.com" && claims.iss !== "accounts.google.com") {
    return fail("bad_issuer");
  }

  const sub = claims.sub as string | undefined;
  const email = claims.email as string | undefined;
  if (!sub || !email) return fail("no_email");
  if (claims.email_verified === false) return fail("email_unverified");

  const user = await linkOrCreateGoogleUser(env.DB, sub, email, (claims.name as string) ?? "");
  const token = await signJwt({ sub: user.id, role: user.role }, env.JWT_SECRET);

  const response = redirectToApp(appUrl, `token=${encodeURIComponent(token)}`);
  response.headers.append("set-cookie", "arena_oauth_state=; Path=/v1/auth/google; Max-Age=0");
  return response;
};
