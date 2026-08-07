import { onScopeDispose, ref, shallowRef } from 'vue'
import { ManagedSocket } from '@/api/sockets'
import * as api from '@/api/releaseProcess'
import { RELEASE_SOCKET } from '@/api/releaseProcess'
import { errorMessage } from '@/api/http'
import type { ReleaseStage, RepositoryDescriptor } from '@/api/types'
import { releaseRunning } from '@/domain/releaseBoard'

/** Safety net in case a stage transition is missed while the socket is down. */
const POLL_WHILE_RUNNING_MS = 10_000

export function useReleaseBoard() {
    const stages = shallowRef<ReleaseStage[]>([])
    const repositories = shallowRef<RepositoryDescriptor[]>([])
    const loading = ref(false)
    const error = ref('')

    let socket: ManagedSocket | null = null
    let poller: ReturnType<typeof setInterval> | null = null
    let refreshing = false
    let refreshAgain = false
    let disposed = false

    /**
     * Coalesces bursts of stage transitions: a refresh arriving while one is in flight is collapsed
     * into a single follow-up, so ten transitions cost two fetches rather than ten.
     */
    async function refresh(): Promise<void> {
        if (refreshing) {
            refreshAgain = true
            return
        }
        refreshing = true
        try {
            do {
                refreshAgain = false
                repositories.value = await api.fetchRepositoryDescriptors()
            } while (refreshAgain)
            error.value = ''
        } catch (e) {
            error.value = errorMessage(e)
            // a signal that arrived during the failed fetch would otherwise be lost, leaving the
            // board frozen until the next transition
            if (refreshAgain && !disposed) {
                refreshAgain = false
                setTimeout(() => void refresh(), 1_000)
            }
        } finally {
            refreshing = false
        }
    }

    async function start(): Promise<void> {
        loading.value = true
        try {
            stages.value = await api.fetchStages()
            await refresh()

            // open the socket before kicking the pipeline, so no transition falls in the gap
            const opening = new ManagedSocket(RELEASE_SOCKET, {
                onMessage: () => void refresh(),
                onReconnect: () => void refresh(),
            })
            // start() spans several round-trips, so the view may already be gone by now
            if (disposed) {
                opening.close()
                return
            }
            socket = opening
            await socket.open()
            if (disposed) {
                return
            }

            // guarded server-side; resumes an interrupted release and is a no-op otherwise.
            // deliberately not repeated on reconnect - reconnecting must not re-trigger a release.
            await api.startExecute()
            await refresh()
            if (disposed) {
                return
            }

            poller = setInterval(() => {
                if (releaseRunning(repositories.value)) {
                    void refresh()
                }
            }, POLL_WHILE_RUNNING_MS)
        } catch (e) {
            error.value = errorMessage(e)
        } finally {
            loading.value = false
        }
    }

    /** Runs a board action and re-syncs, since the resulting transitions may arrive out of band. */
    async function act(action: () => Promise<void>): Promise<void> {
        error.value = ''
        try {
            await action()
            await refresh()
        } catch (e) {
            error.value = errorMessage(e)
        }
    }

    function stop(): void {
        disposed = true
        socket?.close()
        socket = null
        if (poller !== null) {
            clearInterval(poller)
            poller = null
        }
    }

    onScopeDispose(stop)

    return { stages, repositories, loading, error, start, refresh, act, stop }
}
