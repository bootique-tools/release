import { agrestParams, type AgrestQuery, type DataResponse } from './agrest'
import { getJson } from './http'
import type { OpenIssue, PullRequest } from './types'

export const ISSUE_INCLUDE = ['milestone', 'labels', 'repository', 'author', 'repository.parent'] as const

export const PR_INCLUDE = ['labels', 'repository', 'author', 'repository.parent'] as const

export function listIssues(query: AgrestQuery = {}): Promise<DataResponse<OpenIssue>> {
    return getJson<DataResponse<OpenIssue>>('/issue/show-all', {
        ...agrestParams({ include: ISSUE_INCLUDE, ...query }),
    })
}

export function listPullRequests(query: AgrestQuery = {}): Promise<DataResponse<PullRequest>> {
    return getJson<DataResponse<PullRequest>>('/pr/show-all', {
        ...agrestParams({ include: PR_INCLUDE, ...query }),
    })
}
