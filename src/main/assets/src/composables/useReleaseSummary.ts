import { readonly, shallowRef } from 'vue'
import { fetchReleaseSummary } from '@/api/releaseSelect'
import type { ReleaseSummary } from '@/api/types'

const summary = shallowRef<ReleaseSummary | null>(null)

// Clicking "Release" in the nav runs two guards back to back - /release redirects to the selection
// page, whose guard forwards to the board, whose guard runs in turn. Sharing the in-flight promise
// makes that one request instead of two.
let pending: Promise<ReleaseSummary | null> | null = null

/**
 * The persisted release descriptor, shared app-wide.
 *
 * Both release routes guard on it and the board renders it, which used to mean two identical
 * requests per visit to the board. The route guard refreshes; everything downstream reads what the
 * guard just fetched.
 */
export function useReleaseSummary() {
    return {
        summary: readonly(summary),

        /** Re-fetches. Returns null on failure, so guards can fall through to the safe landing. */
        refresh(): Promise<ReleaseSummary | null> {
            pending ??= fetchReleaseSummary()
                .catch(() => null)
                .then((value) => {
                    summary.value = value
                    return value
                })
                .finally(() => {
                    pending = null
                })
            return pending
        },
    }
}
