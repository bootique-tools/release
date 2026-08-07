/**
 * Wire types mirroring the Java model. Enums are const tuples so they are both a union type and
 * a runtime-iterable list.
 */

export const RELEASE_STAGES = [
    'RELEASE_PULL',
    'RELEASE_VALIDATION',
    'RELEASE_PREPARE',
    'RELEASE_PERFORM',
    'RELEASE_SYNC',
] as const
// NO_RELEASE exists in the Java enum but is filtered out before it reaches the wire
export type ReleaseStage = (typeof RELEASE_STAGES)[number]

export const STAGE_LABELS: Record<ReleaseStage, string> = {
    RELEASE_PULL: 'Pull',
    RELEASE_VALIDATION: 'Validation',
    RELEASE_PREPARE: 'Prepare',
    RELEASE_PERFORM: 'Perform',
    RELEASE_SYNC: 'Central sync',
}

export const STAGE_STATUSES = [
    'Not_Start',
    'In_Progress',
    'Success',
    'Fail',
    'Reload',
    'Skip',
    'Rollback',
    'Fail_Rollback',
] as const
// mixed case is deliberate - these are Java enum constant names, matched literally
export type ReleaseStageStatus = (typeof STAGE_STATUSES)[number]

export type GitStatus = 'OK' | 'NEED_UPDATE' | 'MISSING'
export type BatchJobStatus = 'IN_PROGRESS' | 'FAILURE' | 'SUCCESS'

// GitHubEntity maps getGithubId() to the JSON name "id", so there is only ever an `id` on the wire.

export interface Label {
    id: string
    url: string
    name: string
    color: string
}

export interface Author {
    id: string
    url: string
    login: string
    name: string | null
    type: string
}

export interface Milestone {
    id: string
    url: string
    number: number
    state: string
    title: string
    openIssues?: OpenIssue[]
    repository?: Repository
}

export interface Repository {
    id: string
    url: string
    name: string
    description: string | null
    localStatus: GitStatus
    pushedAtStr: string | null
    sshUrl: string | null
    upstream: boolean
    issues?: OpenIssue[]
    milestones?: Milestone[]
    pullRequests?: PullRequest[]
    parent?: Repository | null
}

interface IssueLike {
    id: string
    url: string
    number: number
    title: string
    createdAtStr: string
    repoName: string
    commentsCount: number
    author?: Author
    labels?: Label[]
    repository?: Repository
}

export interface OpenIssue extends IssueLike {
    milestone?: Milestone | null
}

export type PullRequest = IssueLike

export interface Project {
    id: string
    branchName: string | null
    disable: boolean
    groupId: string
    pathStr: string
    version: string
    repository: Repository
    dependencies?: Project[]
}

export interface RepositoryDescriptor {
    repositoryName: string
    stageStatusMap: Record<ReleaseStage, ReleaseStageStatus>
    centralDeploymentId: string | null
}

export interface JobStatusResponse {
    /** False once the job is gone from the server's in-memory map — a restart, or a newer job. */
    jobRunning: boolean
    projectName: string | null
    status: BatchJobStatus | null
    progress: number
    version: string | null
    branchName: string | null
    result: string | null
}

export interface SessionCounts {
    repositories: number
    issues: number
    pullRequests: number
}

export interface SessionInfo {
    /** null until the first GitHub import completes */
    organization: { login: string; name: string; url: string } | null
    user: { login: string; name: string | null; url: string } | null
    counts: SessionCounts
}

export interface ToolCheck {
    javaVersion: string
    jdk: string
    mavenVersion: string
}

export interface ReleaseSummary {
    hasDescriptor: boolean
    fromVersion: string | null
    releaseVersion: string | null
    devVersion: string | null
}
