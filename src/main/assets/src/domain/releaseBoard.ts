import { RELEASE_STAGES, type ReleaseStage, type ReleaseStageStatus, type RepositoryDescriptor } from '@/api/types'

/**
 * Predicates driving the release board, as pure functions over the descriptor list.
 *
 * The original used bitwise `^` between mutually exclusive comparisons (a status cannot be both
 * `Skip` and `Success`), where XOR is equivalent to OR, and `&=`/`|=` as boolean accumulators.
 * They are spelled out here as ordinary boolean logic.
 */

export const isSettled = (status: ReleaseStageStatus): boolean =>
    status === 'Success' || status === 'Skip' || status === 'Fail'

export const isFinishedOk = (status: ReleaseStageStatus): boolean =>
    status === 'Success' || status === 'Skip'

export const isRolledBack = (status: ReleaseStageStatus): boolean =>
    status === 'Rollback' || status === 'Fail_Rollback'

const statusOf = (descriptor: RepositoryDescriptor, stage: ReleaseStage): ReleaseStageStatus =>
    descriptor.stageStatusMap[stage]

/** A stage is running somewhere, so every mutating action must be disabled. */
export function releaseRunning(list: RepositoryDescriptor[]): boolean {
    return list.some((d) => RELEASE_STAGES.some((s) => statusOf(d, s) === 'In_Progress'))
}

/** Any repository has entered the Central sync stage, which is the point of no return. */
export function syncStarted(list: RepositoryDescriptor[]): boolean {
    return list.some((d) => statusOf(d, 'RELEASE_SYNC') !== 'Not_Start')
}

export function syncEnabled(list: RepositoryDescriptor[]): boolean {
    return (
        list.length > 0 &&
        list.every((d) => isFinishedOk(statusOf(d, 'RELEASE_PERFORM'))) &&
        !syncStarted(list)
    )
}

export function releaseFinished(list: RepositoryDescriptor[]): boolean {
    return list.length > 0 && list.every((d) => isFinishedOk(statusOf(d, 'RELEASE_SYNC')))
}

/** Dropping is allowed before sync begins, and again once the whole release has finished. */
export function canDropRelease(list: RepositoryDescriptor[]): boolean {
    return list.length > 0 && (releaseFinished(list) || !syncStarted(list))
}

export function canRollbackRelease(list: RepositoryDescriptor[]): boolean {
    return (
        list.length > 0 &&
        list.every(
            (d) =>
                (statusOf(d, 'RELEASE_PREPARE') !== 'Not_Start' ||
                    statusOf(d, 'RELEASE_PERFORM') !== 'Not_Start') &&
                statusOf(d, 'RELEASE_SYNC') === 'Not_Start',
        )
    )
}

/**
 * Perform is always rollback-able; prepare only while perform has not been done for that
 * repository - and neither once any repository has entered sync.
 */
export function canRollbackStage(
    list: RepositoryDescriptor[],
    descriptor: RepositoryDescriptor,
    stage: ReleaseStage,
): boolean {
    if (syncStarted(list)) {
        return false
    }
    if (stage === 'RELEASE_PERFORM') {
        return true
    }
    if (stage !== 'RELEASE_PREPARE') {
        return false
    }
    const perform = statusOf(descriptor, 'RELEASE_PERFORM')
    return perform === 'Not_Start' || perform === 'Rollback'
}

/** Rollback runs backwards, so the earliest rolled-back stage is the furthest back it reached. */
export function firstRolledBackStage(descriptor: RepositoryDescriptor): ReleaseStage | null {
    return RELEASE_STAGES.find((s) => isRolledBack(statusOf(descriptor, s))) ?? null
}

/** Exactly one cell per repository offers to resume forward: the one rollback reached last. */
export function canResumeFrom(descriptor: RepositoryDescriptor, stage: ReleaseStage): boolean {
    return firstRolledBackStage(descriptor) === stage
}

/** The rollback failed all the way back at the first stage. */
export function fullRollbackFailed(descriptor: RepositoryDescriptor): boolean {
    return statusOf(descriptor, 'RELEASE_PULL') === 'Fail_Rollback'
}

/** Which log a cell's info button should open, or null when it has none. */
export function logKind(
    status: ReleaseStageStatus,
    stage: ReleaseStage,
): 'release' | 'rollback' | null {
    if (isSettled(status)) {
        return 'release'
    }
    if (isRolledBack(status) && (stage === 'RELEASE_PREPARE' || stage === 'RELEASE_PERFORM')) {
        return 'rollback'
    }
    return null
}
