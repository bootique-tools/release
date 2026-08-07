import { agrestParams, type AgrestQuery, type DataResponse } from './agrest'
import { getJson, getText, post } from './http'
import type { Repository } from './types'

export const REPO_INCLUDE = ['milestones', 'issues', 'pullRequests', 'parent'] as const

/** Upstream (fork parent) repositories are excluded server-side. */
export function listRepositories(query: AgrestQuery = {}): Promise<DataResponse<Repository>> {
    return getJson<DataResponse<Repository>>('/repo/show-all', {
        ...agrestParams({ include: REPO_INCLUDE, ...query }),
    })
}

/**
 * Opens a native folder chooser on the machine running the server and blocks until it is dismissed.
 * Resolves to the chosen path, or an empty string if the user cancelled.
 */
export function selectBasePath(): Promise<string> {
    return getText('/git/select_path', undefined, { noTimeout: true })
}

export function cloneRepository(repo: string): Promise<void> {
    return post('/git/clone', { repo })
}

export function updateRepository(repo: string): Promise<void> {
    return post('/git/update', { repo })
}

export function cloneAll(): Promise<void> {
    return post('/git/clone_all')
}

export function updateAll(): Promise<void> {
    return getText('/git/update_all', undefined, { noTimeout: true }).then(() => undefined)
}

export function openRepository(repo: string, type: 'terminal' | 'files'): Promise<void> {
    return getText('/git/open', { repo, type }).then(() => undefined)
}
