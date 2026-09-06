import type { AuthResponse, Env } from "../../_lib/db.ts";
import { userByEmail } from "../../_lib/db.ts";
import { error, json } from "../../_lib/http.ts";
import { signJwt } from "../../_lib/jwt.ts";
import { verifyPassword } from "../../_lib/passwords.ts";

interface LoginRequest {
  email?: string;
  password?: string;
}

export const onRequestPost: PagesFunction<Env> = async ({ request, env }) => {
  const body = (await request.json().catch(() => null)) as LoginRequest | null;
  if (!body?.email || !body?.password) return error("Invalid request body");

  const user = await userByEmail(env.DB, body.email);
  if (!user || !(await verifyPassword(body.password, user.password_hash))) {
    return error("Invalid credentials", 401);
  }
  const token = await signJwt({ sub: user.id, role: user.role }, env.JWT_SECRET);
  return json<AuthResponse>({ token, userId: user.id, role: user.role, displayName: user.display_name });
};
