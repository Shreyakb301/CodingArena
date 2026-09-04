import { AuthResponse, createUser, Env, userByEmail } from "../../_lib/db";
import { error, json } from "../../_lib/http";
import { signJwt } from "../../_lib/jwt";
import { hashPassword } from "../../_lib/passwords";

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

  const user = await createUser(env.DB, {
    displayName,
    email,
    passwordHash: await hashPassword(body.password!),
    role,
  });
  const token = await signJwt({ sub: user.id, role: user.role }, env.JWT_SECRET);
  return json<AuthResponse>(
    { token, userId: user.id, role: user.role, displayName: user.display_name },
    201,
  );
};
