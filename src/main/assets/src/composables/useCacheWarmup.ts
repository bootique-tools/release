import { readonly, ref } from 'vue'
import { checkCache } from '@/api/session'

const INITIAL_DELAY_MS = 200
const MAX_DELAY_MS = 2_000
const MAX_FAILURES = 10

const ready = ref(false)
const error = ref('')

let pending: Promise<void> | null = null

function sleep(ms: number, signal?: AbortSignal): Promise<void> {
    return new Promise((resolve) => {
        const timer = setTimeout(resolve, ms)
        signal?.addEventListener('abort', () => {
            clearTimeout(timer)
            resolve()
        })
    })
}

async function poll(): Promise<void> {
    let delay = INITIAL_DELAY_MS
    let failures = 0

    while (!ready.value) {
        try {
            if (await checkCache()) {
                ready.value = true
                error.value = ''
                return
            }
            failures = 0
        } catch (e) {
            if (++failures >= MAX_FAILURES) {
                error.value = `Cannot reach the server: ${e instanceof Error ? e.message : String(e)}`
                return
            }
        }
        await sleep(delay)
        delay = Math.min(delay * 1.5, MAX_DELAY_MS)
    }
}

/**
 * Waits for the scheduled GitHub import to populate the local database. Shared app-wide: once the
 * cache is warm it stays warm, so this polls at most once per page load rather than per component.
 */
export function useCacheWarmup() {
    return {
        ready: readonly(ready),
        error: readonly(error),

        waitForCache(): Promise<void> {
            if (ready.value) {
                return Promise.resolve()
            }
            pending ??= poll().finally(() => {
                pending = null
            })
            return pending
        },

        retry(): Promise<void> {
            error.value = ''
            pending ??= poll().finally(() => {
                pending = null
            })
            return pending
        },
    }
}
