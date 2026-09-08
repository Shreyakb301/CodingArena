import type { Env } from "../../_lib/db.ts";
import { userById } from "../../_lib/db.ts";
import { sessionUser } from "../../_lib/auth.ts";
import { error, json } from "../../_lib/http.ts";

interface Identity {
  userId: string;
  role: string;
  displayName: string;
}

// The signed-in user's identity for a token the client already holds. The
// Google flow only hands the app a JWT, so the web client calls this on startup
// to learn the display name and role that email/password sign-in gets in its
// response body.
export const onRequestGet: PagesFunction<Env> = async ({ request, env }) => {
  const session = await sessionUser(request, env);
  if (!session) return error("Not signed in", 401);

  const user = await userById(env.DB, session.userId);
  if (!user) return error("Account not found", 404);

  return json<Identity>({ userId: user.id, role: user.role, displayName: user.display_name });
};
