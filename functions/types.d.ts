// Minimal ambient types for the bits of the Workers/D1 runtime we use, so the
// functions type-check without pulling in an npm toolchain. wrangler bundles
// with esbuild (types stripped), so this is editor-only.

interface D1Result<T = unknown> {
  results: T[];
  success: boolean;
}

interface D1PreparedStatement {
  bind(...values: unknown[]): D1PreparedStatement;
  first<T = unknown>(colName?: string): Promise<T | null>;
  run<T = unknown>(): Promise<D1Result<T>>;
  all<T = unknown>(): Promise<D1Result<T>>;
}

interface D1Database {
  prepare(query: string): D1PreparedStatement;
  batch<T = unknown>(statements: D1PreparedStatement[]): Promise<D1Result<T>[]>;
}

interface KVNamespace {
  get(key: string, type: "arrayBuffer"): Promise<ArrayBuffer | null>;
  get(key: string, type: "text"): Promise<string | null>;
  getWithMetadata<M = unknown>(
    key: string,
    type: "arrayBuffer",
  ): Promise<{ value: ArrayBuffer | null; metadata: M | null }>;
  put(
    key: string,
    value: string | ArrayBuffer | ArrayBufferView,
    options?: { metadata?: unknown; expirationTtl?: number },
  ): Promise<void>;
  delete(key: string): Promise<void>;
}

interface EventContext<Env> {
  request: Request;
  env: Env;
  params: Record<string, string | string[]>;
  next: () => Promise<Response>;
}

type PagesFunction<Env = unknown> = (context: EventContext<Env>) => Response | Promise<Response>;
