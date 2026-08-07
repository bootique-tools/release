import { getJson, getText, post, postForm } from './http'
import type { ReleaseStage, RepositoryDescriptor } from './types'

/** Pushes the literal text "LOAD" on every stage transition; carries no payload. */
export const RELEASE_SOCKET = '/api/release/socket'

export function fetchStages(): Promise<ReleaseStage[]> {
    return getJson<ReleaseStage[]>('/release/stage')
}

export function fetchRepositoryDescriptors(): Promise<RepositoryDescriptor[]> {
    return getJson<RepositoryDescriptor[]>('/release/repository')
}

/** Starts or resumes the pipeline. Guarded server-side, so a redundant call is a no-op. */
export function startExecute(): Promise<void> {
    return post('/release/start-execute')
}

export function startSyncStage(): Promise<void> {
    return post('/release/start-sync-stage')
}

export function dropRelease(): Promise<void> {
    return post('/release/drop')
}

export function skipStage(repository: string, stage: ReleaseStage): Promise<void> {
    return postForm('/release/skip-stage', { repository, stage })
}

export function restartStage(repository: string, stage: ReleaseStage): Promise<void> {
    return postForm('/release/restart-stage', { repository, stage })
}

export function rollbackRepository(repository: string, stage: ReleaseStage): Promise<void> {
    return postForm('/release/rollback-repository', { repository, stage })
}

export function skipRollback(repository: string, stage: ReleaseStage): Promise<void> {
    return postForm('/release/skip-rollback', { repository, stage })
}

export function rollbackRelease(): Promise<void> {
    return post('/release/rollback-release')
}

export function fetchReleaseLog(repository: string, stage: ReleaseStage): Promise<string> {
    return getText('/release/releaseLog', { repository, stage })
}

export function fetchRollbackLog(repository: string, stage: ReleaseStage): Promise<string> {
    return getText('/release/rollbackLog', { repository, stage })
}
