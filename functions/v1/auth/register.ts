import type { AuthResponse, Env } from "../../_lib/db.ts";
import { createUser, userByEmail } from "../../_lib/db.ts";
import { error, json } from "../../_lib/http.ts";
import { signJwt } from "../../_lib/jwt.ts";
import { hashPassword } from "../../_lib/passwords.ts";

interface RegisterRequest {
  displayName?: string;
  email?: string | null;
  password?: string;
  role?: string;
}

export const onRequestPost: PagesFunction<Env> = async ({ request, env }) => {
  const body = (await request.json().catch(() => null)) as RegisterRequest | null;
  if (!body) return error("Invalid request body");

  const displayName = (body.displayName ?? "").trim();
  if (displayName.length < 2 || displayName.length > 80) return error("Display name must be 2-80 characters");
  if ((body.password ?? "").length < 10) return error("Password must be at least 10 characters");

  const role = body.role === "TEACHER" ? "TEACHER" : "STUDENT";
  const email = body.email?.trim().toLowerCase() || null;
  if (role === "TEACHER" && !email) return error("Teachers need an email");
  if (email && (await userByEmail(env.DB, email))) return error("That email is already registered");

  let user;
  try {
    user = await createUser(env.DB, {
      displayName,
      email,
      passwordHash: await hashPassword(body.password!),
      role,
    });
  } catch (e) {
    // The unique index is the real guard - the check above is only a fast path,
    // and two requests can race between it and the insert.
    if (/unique/i.test(String((e as Error)?.message))) {
      return error("That email is already registered");
    }
    throw e;
  }
  const token = await signJwt({ sub: user.id, role: user.role }, env.JWT_SECRET);
  return json<AuthResponse>(
    { token, userId: user.id, role: user.role, displayName: user.display_name },
    201,
  );
};
