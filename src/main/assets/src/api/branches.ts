import type { DataResponse } from './agrest'
import { getJson, getText } from './http'
import type { Project } from './types'

/** As with milestones, this endpoint forces its own include and ignores client Agrest parameters. */
export function listBranchProjects(): Promise<DataResponse<Project>> {
    return getJson<DataResponse<Project>>('/branches/show-all')
}

export function currentBranch(name: string): Promise<string> {
    return getText('/branches/get-branch', { name })
}

export type BranchAction = 'createBranch' | 'checkoutBranch'

/** Starts an asynchronous batch job; progress arrives over the job-status socket. */
export function startBranchJob(
    action: BranchAction,
    modules: string[],
    branchTitle: string,
): Promise<string> {
    return getText(`/branches/${action}`, {
        branchTitle,
        selectedModules: JSON.stringify(modules),
    })
}
