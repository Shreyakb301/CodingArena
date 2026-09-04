export const json = <T = unknown>(body: T, status = 200): Response =>
  new Response(JSON.stringify(body), {
    status,
    headers: { "content-type": "application/json" },
  });

export const error = (message: string, status = 400): Response => json({ message }, status);

export function getCookie(request: Request, name: string): string | null {
  const header = request.headers.get("cookie");
  if (!header) return null;
  for (const part of header.split(";")) {
    const [k, ...v] = part.trim().split("=");
    if (k === name) return decodeURIComponent(v.join("="));
  }
  return null;
}

/** Redirect the browser back to the app with a URL fragment. */
export const redirectToApp = (appUrl: string, fragment: string): Response =>
  new Response(null, { status: 302, headers: { location: `${appUrl}/#${fragment}` } });
