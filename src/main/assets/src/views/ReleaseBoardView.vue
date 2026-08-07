<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import PageShell from '@/components/PageShell.vue'
import LoadingBar from '@/components/LoadingBar.vue'
import ErrorAlert from '@/components/ErrorAlert.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'
import LogModal from '@/components/LogModal.vue'
import { useReleaseBoard } from '@/composables/useReleaseBoard'
import { useReleaseSummary } from '@/composables/useReleaseSummary'
import * as api from '@/api/releaseProcess'
import { errorMessage } from '@/api/http'
import { STAGE_LABELS, type ReleaseStage, type RepositoryDescriptor } from '@/api/types'
import {
    canDropRelease,
    canRollbackRelease,
    logKind,
    releaseRunning,
    stageActions,
    statusOf,
    syncEnabled,
    type StageAction,
} from '@/domain/releaseBoard'

const router = useRouter()
const { stages, repositories, loading, error, start, refresh, act, stop } = useReleaseBoard()

// the route guard has just fetched this, so the board never re-requests it
const { summary } = useReleaseSummary()
const confirm = ref<'drop' | 'rollback' | null>(null)
const confirmBusy = ref(false)

const running = computed(() => releaseRunning(repositories.value))

interface ActionSpec {
    icon: string
    /** Both the tooltip and, expanded with the repository and stage, the accessible name. */
    title: string
    variant: string
    run: (descriptor: RepositoryDescriptor, stage: ReleaseStage) => void
}

/** How each cell action presents, and what it calls. Visibility is decided in the domain layer. */
const ACTIONS: Record<StageAction, ActionSpec> = {
    log: {
        icon: 'bi-info-circle',
        title: 'Show log',
        variant: 'btn-outline-secondary',
        run: (d, stage) => openLog(d.repositoryName, stage, logKind(statusOf(d, stage), stage) ?? 'release'),
    },
    rollback: {
        icon: 'bi-chevron-double-left',
        title: 'Roll this stage back',
        variant: 'btn-outline-info',
        run: (d, stage) => act(() => api.rollbackRepository(d.repositoryName, stage)),
    },
    restart: {
        icon: 'bi-arrow-clockwise',
        title: 'Restart this stage',
        variant: 'btn-outline-dark',
        run: (d, stage) => act(() => api.restartStage(d.repositoryName, stage)),
    },
    skip: {
        icon: 'bi-skip-forward-fill',
        title: 'Skip this stage',
        variant: 'btn-outline-success',
        run: (d, stage) => act(() => api.skipStage(d.repositoryName, stage)),
    },
    resume: {
        icon: 'bi-chevron-double-right',
        title: 'Resume from this stage',
        variant: 'btn-outline-success',
        run: (d, stage) => act(() => api.restartStage(d.repositoryName, stage)),
    },
    retryRollback: {
        icon: 'bi-arrow-clockwise',
        title: 'Retry the rollback',
        variant: 'btn-outline-dark',
        run: (d, stage) => act(() => api.rollbackRepository(d.repositoryName, stage)),
    },
    skipRollback: {
        icon: 'bi-skip-forward-fill',
        title: 'Skip the rollback',
        variant: 'btn-outline-success',
        run: (d, stage) => act(() => api.skipRollback(d.repositoryName, stage)),
    },
}

// one modal for the whole board, rather than one rendered inside every cell
const log = ref({
    open: false,
    repository: '',
    stage: null as ReleaseStage | null,
    kind: 'release' as 'release' | 'rollback',
    text: '',
    loading: false,
    error: '',
})

// opening a second log while the first is in flight must not let the first response land in it
let logRequest = 0

async function openLog(repository: string, stage: ReleaseStage, kind: 'release' | 'rollback') {
    const token = ++logRequest
    log.value = { open: true, repository, stage, kind, text: '', loading: true, error: '' }
    try {
        const text =
            kind === 'rollback'
                ? await api.fetchRollbackLog(repository, stage)
                : await api.fetchReleaseLog(repository, stage)
        if (token === logRequest) {
            log.value = { ...log.value, text, loading: false }
        }
    } catch (e) {
        if (token === logRequest) {
            log.value = { ...log.value, error: errorMessage(e), loading: false }
        }
    }
}

async function runConfirmed() {
    const action = confirm.value
    confirmBusy.value = true
    try {
        if (action === 'drop') {
            await api.dropRelease()
            stop()
            await router.push('/release/select-projects')
        } else if (action === 'rollback') {
            await act(api.rollbackRelease)
        }
        confirm.value = null
    } catch (e) {
        error.value = errorMessage(e)
    } finally {
        confirmBusy.value = false
    }
}

onMounted(start)
</script>

<template>
    <PageShell
        title="Release"
        :subtitle="
            summary?.releaseVersion ? `${summary.releaseVersion} (next dev ${summary.devVersion})` : undefined
        "
    >
        <template #actions>
            <button type="button" class="btn btn-sm btn-outline-secondary" @click="refresh()">
                <i class="bi bi-arrow-repeat me-1" aria-hidden="true"></i>Refresh
            </button>
            <button
                v-if="canRollbackRelease(repositories)"
                type="button"
                class="btn btn-sm btn-outline-warning"
                :disabled="running"
                @click="confirm = 'rollback'"
            >
                <i class="bi bi-arrow-counterclockwise me-1" aria-hidden="true"></i>Roll back
            </button>
            <button
                v-if="canDropRelease(repositories)"
                type="button"
                class="btn btn-sm btn-outline-danger"
                @click="confirm = 'drop'"
            >
                <i class="bi bi-trash me-1" aria-hidden="true"></i>Drop release
            </button>
        </template>

        <ErrorAlert :message="error" @dismiss="error = ''" />
        <LoadingBar :loading="loading" />

        <div class="table-responsive">
            <table class="table table-sm align-middle stage-board">
                <thead>
                    <tr>
                        <th scope="col" class="stage-board__repo">Repository</th>
                        <th v-for="stage in stages" :key="stage" scope="col">
                            {{ STAGE_LABELS[stage] }}
                            <button
                                v-if="stage === 'RELEASE_SYNC' && syncEnabled(repositories)"
                                type="button"
                                class="btn btn-sm btn-outline-success ms-1"
                                :disabled="running"
                                aria-label="Start the Central sync stage"
                                title="Start sync"
                                @click="act(api.startSyncStage)"
                            >
                                <i class="bi bi-play-fill" aria-hidden="true"></i>
                            </button>
                        </th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="descriptor in repositories" :key="descriptor.repositoryName">
                        <th scope="row" class="stage-board__repo fw-normal">
                            {{ descriptor.repositoryName }}
                        </th>
                        <td
                            v-for="stage in stages"
                            :key="stage"
                            :class="{ 'table-active': statusOf(descriptor, stage) === 'In_Progress' }"
                        >
                            <div class="d-flex align-items-center gap-2">
                                <StatusBadge :status="statusOf(descriptor, stage)" />

                                <div class="btn-group btn-group-sm">
                                    <button
                                        v-for="action in stageActions(repositories, descriptor, stage)"
                                        :key="action"
                                        type="button"
                                        class="btn"
                                        :class="ACTIONS[action].variant"
                                        :disabled="action !== 'log' && running"
                                        :title="ACTIONS[action].title"
                                        :aria-label="`${ACTIONS[action].title} - ${descriptor.repositoryName} ${STAGE_LABELS[stage]}`"
                                        @click="ACTIONS[action].run(descriptor, stage)"
                                    >
                                        <i class="bi" :class="ACTIONS[action].icon" aria-hidden="true"></i>
                                    </button>
                                </div>
                            </div>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <ConfirmModal
            :open="confirm === 'drop'"
            title="Drop this release?"
            message="The release descriptor is discarded and you return to project selection. Work already published is not undone."
            confirm-label="Drop"
            variant="danger"
            :busy="confirmBusy"
            @confirm="runConfirmed"
            @cancel="confirm = null"
        />

        <ConfirmModal
            :open="confirm === 'rollback'"
            title="Roll back the whole release?"
            message="Every repository is rolled back to the state before the release started."
            confirm-label="Roll back"
            variant="warning"
            :busy="confirmBusy"
            @confirm="runConfirmed"
            @cancel="confirm = null"
        />

        <LogModal v-bind="log" @close="log.open = false" />
    </PageShell>
</template>
