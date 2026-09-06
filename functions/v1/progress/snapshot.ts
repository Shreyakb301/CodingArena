import { sessionUser } from "../../_lib/auth.ts";
import type { Env } from "../../_lib/db.ts";
import { error } from "../../_lib/http.ts";

// One exported SQLite file per user, for cross-device sync. The client decides
// which side is newer from the X-Snapshot-Updated-At value.
const KEY = (userId: string) => `snapshot:${userId}`;
const MAX_BYTES = 12 * 1024 * 1024;

interface SnapshotMeta {
  updatedAt: number;
}

export const onRequestGet: PagesFunction<Env> = async ({ request, env }) => {
  const user = await sessionUser(request, env);
  if (!user) return error("Sign in first", 401);

  const { value, metadata } = await env.SNAPSHOTS.getWithMetadata<SnapshotMeta>(
    KEY(user.userId),
    "arrayBuffer",
  );
  if (!value) return new Response(null, { status: 404 });

  return new Response(value, {
    headers: {
      "content-type": "application/octet-stream",
      "x-snapshot-updated-at": String(metadata?.updatedAt ?? 0),
      "cache-control": "no-store",
    },
  });
};

export const onRequestPut: PagesFunction<Env> = async ({ request, env }) => {
  const user = await sessionUser(request, env);
  if (!user) return error("Sign in first", 401);

  const body = await request.arrayBuffer();
  if (body.byteLength === 0) return error("Empty snapshot");
  if (body.byteLength > MAX_BYTES) return error("Snapshot too large", 413);

  const updatedAt = Number(request.headers.get("x-snapshot-updated-at")) || Date.now();
  await env.SNAPSHOTS.put(KEY(user.userId), body, { metadata: { updatedAt } satisfies SnapshotMeta });

  return new Response(null, { status: 204 });
};
