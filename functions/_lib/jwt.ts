// Minimal HS256 JWT, matching the shape the app expects (a bearer token it
// stores and sends back). Uses Web Crypto - available in Workers.

const enc = new TextEncoder();
const dec = new TextDecoder();

function b64urlEncode(bytes: Uint8Array): string {
  let s = "";
  for (const b of bytes) s += String.fromCharCode(b);
  return btoa(s).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

function b64urlDecode(s: string): Uint8Array {
  const pad = s.length % 4 ? "=".repeat(4 - (s.length % 4)) : "";
  const b = atob(s.replace(/-/g, "+").replace(/_/g, "/") + pad);
  return Uint8Array.from(b, (c) => c.charCodeAt(0));
}

async function hmacKey(secret: string): Promise<CryptoKey> {
  return crypto.subtle.importKey(
    "raw",
    enc.encode(secret),
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["sign", "verify"],
  );
}

const ISSUER = "codingarena";
const TTL_SECONDS = 60 * 60 * 24 * 7;

export async function signJwt(claims: Record<string, unknown>, secret: string): Promise<string> {
  const now = Math.floor(Date.now() / 1000);
  const header = b64urlEncode(enc.encode(JSON.stringify({ alg: "HS256", typ: "JWT" })));
  const payload = b64urlEncode(
    enc.encode(JSON.stringify({ ...claims, iss: ISSUER, iat: now, exp: now + TTL_SECONDS })),
  );
  const data = `${header}.${payload}`;
  const sig = await crypto.subtle.sign("HMAC", await hmacKey(secret), enc.encode(data));
  return `${data}.${b64urlEncode(new Uint8Array(sig))}`;
}

export async function verifyJwt(token: string, secret: string): Promise<Record<string, unknown> | null> {
  const parts = token.split(".");
  if (parts.length !== 3) return null;
  const [header, payload, sig] = parts;
  const ok = await crypto.subtle.verify(
    "HMAC",
    await hmacKey(secret),
    b64urlDecode(sig),
    enc.encode(`${header}.${payload}`),
  );
  if (!ok) return null;
  const claims = JSON.parse(dec.decode(b64urlDecode(payload)));
  if (typeof claims.exp === "number" && claims.exp < Math.floor(Date.now() / 1000)) return null;
  return claims;
}

/** Decode without verifying - only for a token just fetched from Google over TLS. */
export function decodeJwt(token: string): Record<string, unknown> {
  return JSON.parse(dec.decode(b64urlDecode(token.split(".")[1])));
}
