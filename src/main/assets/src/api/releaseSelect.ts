import type { DataResponse } from './agrest'
import { getJson, postJson } from './http'
import type { Project, ReleaseSummary } from './types'

/**
 * Every listing here arrives pre-sorted into Maven dependency order by the server.
 * Never re-sort client-side - the order is what the release will follow.
 */
export function listReleaseProjects(): Promise<DataResponse<Project>> {
    return getJson<DataResponse<Project>>('/release/show-all')
}

/** Marks projects at `version` (and their transitive dependencies) as selectable. */
export function listProjectsForVersion(version: string): Promise<DataResponse<Project>> {
    return getJson<DataResponse<Project>>('/release/show-projects', { version })
}

/**
 * Recomputes the selection after one checkbox is toggled: the server resolves the transitive
 * dependency closure and returns the resulting set, in order.
 */
export function toggleProjectSelection(
    version: string,
    projects: string[],
    selectedProject: string,
    state: boolean,
): Promise<DataResponse<Project>> {
    return getJson<DataResponse<Project>>('/release/project', {
        version,
        projects: JSON.stringify(projects),
        selectedProject,
        state,
    })
}

export function fetchReleaseSummary(): Promise<ReleaseSummary> {
    return getJson<ReleaseSummary>('/release/summary')
}

export interface CreateReleaseRequest {
    fromVersion: string
    releaseVersion: string
    devVersion: string
    projects: string[]
}

export function createReleaseDescriptor(request: CreateReleaseRequest): Promise<ReleaseSummary> {
    return postJson<ReleaseSummary>('/release/create-descriptor', request)
}
