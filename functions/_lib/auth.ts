import { Env } from "./db";
import { verifyJwt } from "./jwt";

export interface SessionUser {
  userId: string;
  role: string;
}

/** Reads and verifies the Bearer token. Returns null when missing or invalid. */
export async function sessionUser(request: Request, env: Env): Promise<SessionUser | null> {
  const header = request.headers.get("authorization");
  if (!header?.startsWith("Bearer ")) return null;
  const claims = await verifyJwt(header.slice(7).trim(), env.JWT_SECRET);
  if (!claims || typeof claims.sub !== "string") return null;
  return { userId: claims.sub, role: typeof claims.role === "string" ? claims.role : "STUDENT" };
}
