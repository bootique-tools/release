const BASE = '/api'
const DEFAULT_TIMEOUT_MS = 30_000

export type Params = Record<string, string | number | boolean | null | undefined>

export class ApiError extends Error {
    constructor(
        readonly status: number,
        readonly body: string,
    ) {
        super(body || `Request failed with HTTP ${status}`)
        this.name = 'ApiError'
    }

    /** The local repository base path has not been chosen yet - the caller should offer to set it. */
    get isPathNotSet(): boolean {
        return this.status === 412
    }
}

function withQuery(path: string, params?: Params): string {
    if (!params) {
        return BASE + path
    }
    const search = new URLSearchParams()
    for (const [key, value] of Object.entries(params)) {
        if (value !== null && value !== undefined && value !== '') {
            search.append(key, String(value))
        }
    }
    const query = search.toString()
    return BASE + path + (query ? `?${query}` : '')
}

interface RequestOptions {
    /** Omit the client-side timeout, for calls that legitimately block (a native file dialog). */
    noTimeout?: boolean
}

async function request(url: string, init: RequestInit, options: RequestOptions = {}): Promise<Response> {
    const response = await fetch(url, {
        credentials: 'same-origin',
        signal: options.noTimeout ? null : AbortSignal.timeout(DEFAULT_TIMEOUT_MS),
        ...init,
    })

    if (!response.ok) {
        throw new ApiError(response.status, await response.text().catch(() => ''))
    }
    return response
}

export async function getJson<T>(path: string, params?: Params, options?: RequestOptions): Promise<T> {
    const response = await request(withQuery(path, params), { method: 'GET' }, options)
    return (await response.json()) as T
}

export async function getText(path: string, params?: Params, options?: RequestOptions): Promise<string> {
    const response = await request(withQuery(path, params), { method: 'GET' }, options)
    return await response.text()
}

/** POST with no body; query parameters only. */
export async function post(path: string, params?: Params): Promise<void> {
    await request(withQuery(path, params), { method: 'POST' })
}

export async function postJson<T>(path: string, body: unknown): Promise<T> {
    const response = await request(BASE + path, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
    })
    return (await response.json()) as T
}

export async function postForm(path: string, form: Record<string, string>): Promise<void> {
    await request(BASE + path, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams(form),
    })
}

/** Error text suitable for showing to a user, whatever was thrown. */
export function errorMessage(error: unknown): string {
    if (error instanceof ApiError) {
        return error.message
    }
    if (error instanceof DOMException && error.name === 'TimeoutError') {
        return 'The server did not respond in time.'
    }
    return error instanceof Error ? error.message : String(error)
}
