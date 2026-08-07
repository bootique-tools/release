import type { DataResponse } from './agrest'
import { getJson, getText } from './http'
import type { Project } from './types'

/**
 * Milestone and branch listings ignore client-supplied Agrest parameters - the controller forces its
 * own include and takes no UriInfo. Do not add an include here, it would silently do nothing.
 */
export function listMilestoneProjects(): Promise<DataResponse<Project>> {
    return getJson<DataResponse<Project>>('/milestone/show-all')
}

/** Distinct titles of open milestones across the given repositories. */
export function listMilestoneTitles(modules: string[]): Promise<string[]> {
    return getJson<string[]>('/milestone/getMilestones', {
        selectedModules: JSON.stringify(modules),
    })
}

export type MilestoneAction = 'create' | 'close' | 'rename'

/** Starts an asynchronous batch job; progress arrives over the job-status socket. */
export function startMilestoneJob(
    action: MilestoneAction,
    modules: string[],
    titles: { milestoneTitle?: string; milestoneNewTitle?: string },
): Promise<string> {
    return getText(`/milestone/${action}`, {
        selectedModules: JSON.stringify(modules),
        milestoneTitle: titles.milestoneTitle,
        milestoneNewTitle: titles.milestoneNewTitle,
    })
}
