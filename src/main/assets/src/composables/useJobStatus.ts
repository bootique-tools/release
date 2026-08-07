import { onScopeDispose, reactive, ref } from 'vue'
import { fetchJobStatus, JOB_STATUS_SOCKET } from '@/api/job'
import { ManagedSocket } from '@/api/sockets'
import type { BatchJobStatus } from '@/api/types'
import { useBackgroundJob, type JobOwner } from './useBackgroundJob'

export interface RepoJobResult {
    status: BatchJobStatus
    result: string
    version: string | null
    branchName: string | null
}

/**
 * Progress of the current batch job. Results are keyed by repository name rather than by row object,
 * so they survive the row list being replaced on reload.
 */
export function useJobStatus(owner: JobOwner) {
    const progress = ref(0)
    const running = ref(false)
    const byRepo = reactive<Record<string, RepoJobResult>>({})

    const background = useBackgroundJob()
    let socket: ManagedSocket | null = null

    function disconnect() {
        socket?.close()
        socket = null
    }

    function finish() {
        running.value = false
        background.finish()
        disconnect()
    }

    async function refresh(): Promise<void> {
        let status
        try {
            status = await fetchJobStatus()
        } catch {
            // the server cannot tell us about the job, so stop claiming one is running
            finish()
            return
        }

        // the job map is in-memory and swept of completed jobs, so a resumed job can be gone after a
        // restart or once a newer job was submitted. Without this the page would stay disabled forever.
        if (!status.jobRunning) {
            finish()
            return
        }

        progress.value = status.progress
        if (status.projectName) {
            byRepo[status.projectName] = {
                status: status.status ?? 'IN_PROGRESS',
                result: status.result ?? '',
                version: status.version,
                branchName: status.branchName,
            }
        }

        if (status.progress >= 100) {
            finish()
        }
    }

    /**
     * Begins tracking. The server binds its listener to whichever job is current when the socket
     * opens, so this must be called after the endpoint that submits the job has resolved.
     */
    async function start(): Promise<void> {
        disconnect()
        running.value = true
        progress.value = 0
        background.start(owner)

        socket = new ManagedSocket(JOB_STATUS_SOCKET, {
            onMessage: () => void refresh(),
            onReconnect: () => void refresh(),
        })
        await socket.open()
        await refresh()
    }

    /** Picks tracking back up after a reload, if the job still running was started by this page. */
    async function resume(): Promise<void> {
        if (background.isMine(owner)) {
            await start()
        }
    }

    function reset(): void {
        for (const key of Object.keys(byRepo)) {
            delete byRepo[key]
        }
        progress.value = 0
    }

    onScopeDispose(disconnect)

    return { progress, running, byRepo, start, resume, refresh, reset }
}
