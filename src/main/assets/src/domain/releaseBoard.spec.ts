import { describe, expect, it } from 'vitest'
import {
    canDropRelease,
    canResumeFrom,
    canRollbackRelease,
    canRollbackStage,
    firstRolledBackStage,
    fullRollbackFailed,
    isFinishedOk,
    isRolledBack,
    isSettled,
    logKind,
    releaseFinished,
    releaseRunning,
    syncEnabled,
    syncStarted,
} from './releaseBoard'
import type { ReleaseStageStatus, RepositoryDescriptor } from '@/api/types'

/** Builds a descriptor from pull/validation/prepare/perform/sync statuses, in pipeline order. */
function repo(
    name: string,
    [pull, validation, prepare, perform, sync]: ReleaseStageStatus[],
): RepositoryDescriptor {
    return {
        repositoryName: name,
        stageStatusMap: {
            RELEASE_PULL: pull!,
            RELEASE_VALIDATION: validation!,
            RELEASE_PREPARE: prepare!,
            RELEASE_PERFORM: perform!,
            RELEASE_SYNC: sync!,
        },
        centralDeploymentId: null,
    }
}

const NOT_STARTED = repo('a', ['Not_Start', 'Not_Start', 'Not_Start', 'Not_Start', 'Not_Start'])
const PERFORMED = repo('a', ['Success', 'Success', 'Success', 'Success', 'Not_Start'])
const DONE = repo('a', ['Success', 'Success', 'Success', 'Success', 'Success'])

describe('status classification', () => {
    it('treats Success, Skip and Fail as settled', () => {
        expect(isSettled('Success')).toBe(true)
        expect(isSettled('Skip')).toBe(true)
        expect(isSettled('Fail')).toBe(true)
        expect(isSettled('In_Progress')).toBe(false)
        expect(isSettled('Rollback')).toBe(false)
    })

    it('treats only Success and Skip as finished successfully', () => {
        expect(isFinishedOk('Success')).toBe(true)
        expect(isFinishedOk('Skip')).toBe(true)
        expect(isFinishedOk('Fail')).toBe(false)
    })

    it('treats Rollback and Fail_Rollback as rolled back', () => {
        expect(isRolledBack('Rollback')).toBe(true)
        expect(isRolledBack('Fail_Rollback')).toBe(true)
        expect(isRolledBack('Not_Start')).toBe(false)
    })
})

describe('releaseRunning', () => {
    it('is false for an empty board', () => {
        expect(releaseRunning([])).toBe(false)
    })

    it('is true when any repository has a stage in progress', () => {
        const running = repo('b', ['Success', 'In_Progress', 'Not_Start', 'Not_Start', 'Not_Start'])
        expect(releaseRunning([PERFORMED, running])).toBe(true)
    })

    it('is false when nothing is in progress', () => {
        expect(releaseRunning([PERFORMED, NOT_STARTED])).toBe(false)
    })
})

describe('sync gating', () => {
    it('is not started while every sync cell is Not_Start', () => {
        expect(syncStarted([PERFORMED, NOT_STARTED])).toBe(false)
    })

    it('is started as soon as one sync cell moves', () => {
        const syncing = repo('b', ['Success', 'Success', 'Success', 'Success', 'In_Progress'])
        expect(syncStarted([PERFORMED, syncing])).toBe(true)
    })

    it('enables sync only once every repository has performed', () => {
        expect(syncEnabled([PERFORMED, PERFORMED])).toBe(true)
        expect(syncEnabled([PERFORMED, NOT_STARTED])).toBe(false)
    })

    it('counts a skipped perform as performed', () => {
        const skipped = repo('b', ['Success', 'Success', 'Success', 'Skip', 'Not_Start'])
        expect(syncEnabled([PERFORMED, skipped])).toBe(true)
    })

    it('does not re-enable sync once it has begun', () => {
        expect(syncEnabled([DONE])).toBe(false)
    })

    // Array.every is true for an empty list, which would flash the controls on before the first fetch
    it('is disabled on an empty board', () => {
        expect(syncEnabled([])).toBe(false)
        expect(releaseFinished([])).toBe(false)
        expect(canDropRelease([])).toBe(false)
        expect(canRollbackRelease([])).toBe(false)
    })
})

describe('releaseFinished', () => {
    it('is true only when every repository has synced', () => {
        expect(releaseFinished([DONE, DONE])).toBe(true)
        expect(releaseFinished([DONE, PERFORMED])).toBe(false)
    })
})

describe('canDropRelease', () => {
    it('allows dropping before sync begins', () => {
        expect(canDropRelease([PERFORMED])).toBe(true)
    })

    it('allows dropping again once the release has finished', () => {
        expect(canDropRelease([DONE])).toBe(true)
    })

    it('forbids dropping while sync is mid-flight', () => {
        const syncing = repo('b', ['Success', 'Success', 'Success', 'Success', 'In_Progress'])
        expect(canDropRelease([DONE, syncing])).toBe(false)
    })
})

describe('canRollbackRelease', () => {
    it('requires every repository to have started prepare or perform', () => {
        expect(canRollbackRelease([PERFORMED])).toBe(true)
        expect(canRollbackRelease([PERFORMED, NOT_STARTED])).toBe(false)
    })

    it('is forbidden once any repository has entered sync', () => {
        expect(canRollbackRelease([DONE])).toBe(false)
    })
})

describe('canRollbackStage', () => {
    it('always allows rolling back perform', () => {
        expect(canRollbackStage([PERFORMED], PERFORMED, 'RELEASE_PERFORM')).toBe(true)
    })

    // the original mixed ^ and &&, so this button always showed on prepare cells
    it('allows rolling back prepare only while perform is undone', () => {
        const preparedOnly = repo('a', ['Success', 'Success', 'Success', 'Not_Start', 'Not_Start'])
        expect(canRollbackStage([preparedOnly], preparedOnly, 'RELEASE_PREPARE')).toBe(true)
        expect(canRollbackStage([PERFORMED], PERFORMED, 'RELEASE_PREPARE')).toBe(false)
    })

    it('allows rolling back prepare when perform has itself been rolled back', () => {
        const rolled = repo('a', ['Success', 'Success', 'Success', 'Rollback', 'Not_Start'])
        expect(canRollbackStage([rolled], rolled, 'RELEASE_PREPARE')).toBe(true)
    })

    it('forbids every rollback once sync has started', () => {
        const syncing = repo('b', ['Success', 'Success', 'Success', 'Success', 'In_Progress'])
        expect(canRollbackStage([PERFORMED, syncing], PERFORMED, 'RELEASE_PERFORM')).toBe(false)
    })

    it('is never offered for pull, validation or sync', () => {
        expect(canRollbackStage([PERFORMED], PERFORMED, 'RELEASE_PULL')).toBe(false)
        expect(canRollbackStage([PERFORMED], PERFORMED, 'RELEASE_VALIDATION')).toBe(false)
        expect(canRollbackStage([PERFORMED], PERFORMED, 'RELEASE_SYNC')).toBe(false)
    })
})

describe('resuming after a rollback', () => {
    const rolledToPrepare = repo('a', [
        'Success', 'Success', 'Rollback', 'Rollback', 'Not_Start',
    ])

    it('finds the earliest rolled-back stage in pipeline order', () => {
        expect(firstRolledBackStage(rolledToPrepare)).toBe('RELEASE_PREPARE')
        expect(firstRolledBackStage(PERFORMED)).toBeNull()
    })

    it('offers resume on exactly one cell', () => {
        expect(canResumeFrom(rolledToPrepare, 'RELEASE_PREPARE')).toBe(true)
        expect(canResumeFrom(rolledToPrepare, 'RELEASE_PERFORM')).toBe(false)
    })

    it('treats a failed rollback as a rolled-back stage when finding the earliest', () => {
        const failed = repo('a', ['Success', 'Fail_Rollback', 'Rollback', 'Rollback', 'Not_Start'])
        expect(firstRolledBackStage(failed)).toBe('RELEASE_VALIDATION')
    })

    it('detects a rollback that failed all the way back at pull', () => {
        const failedAtPull = repo('a', [
            'Fail_Rollback', 'Rollback', 'Rollback', 'Rollback', 'Not_Start',
        ])
        expect(fullRollbackFailed(failedAtPull)).toBe(true)
        expect(fullRollbackFailed(rolledToPrepare)).toBe(false)
    })
})

describe('logKind', () => {
    it('offers the release log for any settled stage', () => {
        expect(logKind('Success', 'RELEASE_PULL')).toBe('release')
        expect(logKind('Fail', 'RELEASE_SYNC')).toBe('release')
        expect(logKind('Skip', 'RELEASE_PREPARE')).toBe('release')
    })

    it('offers the rollback log only for rolled-back prepare and perform', () => {
        expect(logKind('Rollback', 'RELEASE_PREPARE')).toBe('rollback')
        expect(logKind('Fail_Rollback', 'RELEASE_PERFORM')).toBe('rollback')
        expect(logKind('Rollback', 'RELEASE_PULL')).toBeNull()
    })

    it('offers nothing for stages that have not run', () => {
        expect(logKind('Not_Start', 'RELEASE_PULL')).toBeNull()
        expect(logKind('In_Progress', 'RELEASE_PREPARE')).toBeNull()
    })
})
