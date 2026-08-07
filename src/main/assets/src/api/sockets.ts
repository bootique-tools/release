/** WebSocket paths are absolute against the servlet context, not under the /api Jersey mapping. */
export function wsUrl(path: string): string {
    const scheme = location.protocol === 'https:' ? 'wss:' : 'ws:'
    return `${scheme}//${location.host}${path}`
}

export interface ManagedSocketOptions {
    onMessage: (data: string) => void
    /** Called after a reconnect, so the caller can re-sync state missed while disconnected. */
    onReconnect?: () => void
}

const MIN_RETRY_MS = 200
const MAX_RETRY_MS = 5_000

/**
 * A WebSocket that reconnects with backoff and can be closed for good. Both server endpoints send
 * content-free pings, so messages are only a signal to re-fetch.
 */
export class ManagedSocket {
    private socket: WebSocket | null = null
    private closed = false
    private retryMs = MIN_RETRY_MS
    private retryTimer: ReturnType<typeof setTimeout> | null = null
    private everOpened = false
    // held on the instance rather than captured by the first connect(), so a retry can settle open()
    private pendingOpen: (() => void) | null = null

    constructor(
        private readonly path: string,
        private readonly options: ManagedSocketOptions,
    ) {}

    /** Resolves on the first successful connection, however many attempts that takes. */
    open(): Promise<void> {
        return new Promise((resolve) => {
            if (this.closed) {
                resolve()
                return
            }
            this.pendingOpen = resolve
            this.connect()
        })
    }

    private connect(): void {
        if (this.closed) {
            return
        }

        const socket = new WebSocket(wsUrl(this.path))
        this.socket = socket

        socket.onopen = () => {
            this.retryMs = MIN_RETRY_MS
            if (this.everOpened) {
                this.options.onReconnect?.()
            }
            this.everOpened = true
            this.settleOpen()
        }

        socket.onmessage = (event) => this.options.onMessage(String(event.data))

        socket.onclose = () => {
            if (this.closed) {
                return
            }
            // jitter so several tabs do not retry in lockstep
            const delay = this.retryMs * (0.5 + Math.random() / 2)
            this.retryMs = Math.min(this.retryMs * 2, MAX_RETRY_MS)
            this.retryTimer = setTimeout(() => this.connect(), delay)
        }
    }

    close(): void {
        this.closed = true
        if (this.retryTimer !== null) {
            clearTimeout(this.retryTimer)
            this.retryTimer = null
        }
        this.socket?.close()
        this.socket = null
        // never leave a caller awaiting open() on a socket that will not reconnect
        this.settleOpen()
    }

    private settleOpen(): void {
        const resolve = this.pendingOpen
        this.pendingOpen = null
        resolve?.()
    }
}
