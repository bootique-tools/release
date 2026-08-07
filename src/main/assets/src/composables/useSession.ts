import { readonly, ref } from 'vue'
import { fetchSession } from '@/api/session'
import { errorMessage } from '@/api/http'
import type { SessionInfo } from '@/api/types'

const session = ref<SessionInfo | null>(null)
const loading = ref(false)
const error = ref('')

async function refresh(): Promise<void> {
    loading.value = true
    error.value = ''
    try {
        session.value = await fetchSession()
    } catch (e) {
        error.value = errorMessage(e)
    } finally {
        loading.value = false
    }
}

let started = false

/**
 * Identity and nav badge counts, fetched once and shared app-wide. A null organization means the
 * GitHub import has not produced data yet - the nav shows the same hint the old header did.
 */
export function useSession() {
    if (!started) {
        started = true
        void refresh()
    }
    return {
        session: readonly(session),
        loading: readonly(loading),
        error: readonly(error),
        refresh,
    }
}
