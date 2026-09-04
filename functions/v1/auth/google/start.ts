import { Env } from "../../../_lib/db";
import { error } from "../../../_lib/http";

export const onRequestGet: PagesFunction<Env> = async ({ request, env }) => {
  if (!env.GOOGLE_CLIENT_ID) return error("Google sign-in is not configured", 501);

  const origin = new URL(request.url).origin;
  const state = crypto.randomUUID();

  const authUrl = new URL("https://accounts.google.com/o/oauth2/v2/auth");
  authUrl.searchParams.set("client_id", env.GOOGLE_CLIENT_ID);
  authUrl.searchParams.set("redirect_uri", `${origin}/v1/auth/google/callback`);
  authUrl.searchParams.set("response_type", "code");
  authUrl.searchParams.set("scope", "openid email profile");
  authUrl.searchParams.set("state", state);
  authUrl.searchParams.set("prompt", "select_account");

  return new Response(null, {
    status: 302,
    headers: {
      location: authUrl.toString(),
      "set-cookie": `arena_oauth_state=${state}; Path=/v1/auth/google; Max-Age=600; HttpOnly; Secure; SameSite=Lax`,
    },
  });
};
