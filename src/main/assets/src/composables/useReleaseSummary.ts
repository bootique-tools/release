import { readonly, shallowRef } from 'vue'
import { fetchReleaseSummary } from '@/api/releaseSelect'
import type { ReleaseSummary } from '@/api/types'

const summary = shallowRef<ReleaseSummary | null>(null)

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
        async refresh(): Promise<ReleaseSummary | null> {
            summary.value = await fetchReleaseSummary().catch(() => null)
            return summary.value
        },
    }
}
