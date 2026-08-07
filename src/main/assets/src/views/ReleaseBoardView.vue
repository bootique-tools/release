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
import * as api from '@/api/releaseProcess'
import { fetchReleaseSummary } from '@/api/releaseSelect'
import { errorMessage } from '@/api/http'
import {
    STAGE_LABELS,
    type ReleaseStage,
    type ReleaseStageStatus,
    type RepositoryDescriptor,
} from '@/api/types'
import {
    canDropRelease,
    canResumeFrom,
    canRollbackRelease,
    canRollbackStage,
    fullRollbackFailed,
    isRolledBack,
    isSettled,
    logKind,
    releaseRunning,
    syncEnabled,
} from '@/domain/releaseBoard'

const router = useRouter()
const board = useReleaseBoard()

const summary = ref<{ releaseVersion: string | null; devVersion: string | null } | null>(null)
const confirm = ref<'drop' | 'rollback' | null>(null)
const confirmBusy = ref(false)

const list = computed(() => board.repositories.value)
const running = computed(() => releaseRunning(list.value))

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

function statusOf(descriptor: RepositoryDescriptor, stage: ReleaseStage): ReleaseStageStatus {
    return descriptor.stageStatusMap[stage]
}

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
            board.stop()
            await router.push('/release/select-projects')
        } else if (action === 'rollback') {
            await board.act(api.rollbackRelease)
        }
        confirm.value = null
    } catch (e) {
        board.error.value = errorMessage(e)
    } finally {
        confirmBusy.value = false
    }
}

onMounted(async () => {
    summary.value = await fetchReleaseSummary().catch(() => null)
    await board.start()
})
</script>

<template>
    <PageShell
        title="Release"
        :subtitle="
            summary?.releaseVersion ? `${summary.releaseVersion} (next dev ${summary.devVersion})` : undefined
        "
    >
        <template #actions>
            <button type="button" class="btn btn-sm btn-outline-secondary" @click="board.refresh()">
                <i class="bi bi-arrow-repeat me-1" aria-hidden="true"></i>Refresh
            </button>
            <button
                v-if="canRollbackRelease(list)"
                type="button"
                class="btn btn-sm btn-outline-warning"
                :disabled="running"
                @click="confirm = 'rollback'"
            >
                <i class="bi bi-arrow-counterclockwise me-1" aria-hidden="true"></i>Roll back
            </button>
            <button
                v-if="canDropRelease(list)"
                type="button"
                class="btn btn-sm btn-outline-danger"
                @click="confirm = 'drop'"
            >
                <i class="bi bi-trash me-1" aria-hidden="true"></i>Drop release
            </button>
        </template>

        <ErrorAlert :message="board.error.value" @dismiss="board.error.value = ''" />
        <LoadingBar :loading="board.loading.value" />

        <div class="table-responsive">
            <table class="table table-sm align-middle stage-board">
                <thead>
                    <tr>
                        <th scope="col" class="stage-board__repo">Repository</th>
                        <th v-for="stage in board.stages.value" :key="stage" scope="col">
                            {{ STAGE_LABELS[stage] }}
                            <button
                                v-if="stage === 'RELEASE_SYNC' && syncEnabled(list)"
                                type="button"
                                class="btn btn-sm btn-outline-success ms-1"
                                :disabled="running"
                                aria-label="Start the Central sync stage"
                                title="Start sync"
                                @click="board.act(api.startSyncStage)"
                            >
                                <i class="bi bi-play-fill" aria-hidden="true"></i>
                            </button>
                        </th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="descriptor in list" :key="descriptor.repositoryName">
                        <th scope="row" class="stage-board__repo fw-normal">
                            {{ descriptor.repositoryName }}
                        </th>
                        <td
                            v-for="stage in board.stages.value"
                            :key="stage"
                            :class="{ 'table-active': statusOf(descriptor, stage) === 'In_Progress' }"
                        >
                            <div class="d-flex align-items-center gap-2">
                                <StatusBadge :status="statusOf(descriptor, stage)" />

                                <div class="btn-group btn-group-sm">
                                    <button
                                        v-if="logKind(statusOf(descriptor, stage), stage)"
                                        type="button"
                                        class="btn btn-outline-secondary"
                                        :aria-label="`Show log for ${descriptor.repositoryName} ${STAGE_LABELS[stage]}`"
                                        title="Show log"
                                        @click="
                                            openLog(
                                                descriptor.repositoryName,
                                                stage,
                                                logKind(statusOf(descriptor, stage), stage)!,
                                            )
                                        "
                                    >
                                        <i class="bi bi-info-circle" aria-hidden="true"></i>
                                    </button>

                                    <template v-if="isSettled(statusOf(descriptor, stage))">
                                        <button
                                            v-if="canRollbackStage(list, descriptor, stage)"
                                            type="button"
                                            class="btn btn-outline-info"
                                            :disabled="running"
                                            title="Roll this stage back"
                                            :aria-label="`Roll back ${descriptor.repositoryName} ${STAGE_LABELS[stage]}`"
                                            @click="
                                                board.act(() =>
                                                    api.rollbackRepository(descriptor.repositoryName, stage),
                                                )
                                            "
                                        >
                                            <i class="bi bi-chevron-double-left" aria-hidden="true"></i>
                                        </button>

                                        <template v-if="statusOf(descriptor, stage) === 'Fail'">
                                            <button
                                                type="button"
                                                class="btn btn-outline-dark"
                                                :disabled="running"
                                                title="Restart this stage"
                                                :aria-label="`Restart ${descriptor.repositoryName} ${STAGE_LABELS[stage]}`"
                                                @click="
                                                    board.act(() =>
                                                        api.restartStage(descriptor.repositoryName, stage),
                                                    )
                                                "
                                            >
                                                <i class="bi bi-arrow-clockwise" aria-hidden="true"></i>
                                            </button>
                                            <button
                                                type="button"
                                                class="btn btn-outline-success"
                                                :disabled="running"
                                                title="Skip this stage"
                                                :aria-label="`Skip ${descriptor.repositoryName} ${STAGE_LABELS[stage]}`"
                                                @click="
                                                    board.act(() =>
                                                        api.skipStage(descriptor.repositoryName, stage),
                                                    )
                                                "
                                            >
                                                <i class="bi bi-skip-forward-fill" aria-hidden="true"></i>
                                            </button>
                                        </template>
                                    </template>

                                    <template v-else-if="isRolledBack(statusOf(descriptor, stage))">
                                        <!-- exactly one cell per repository is the point to resume forward from -->
                                        <button
                                            v-if="
                                                statusOf(descriptor, stage) === 'Rollback' &&
                                                canResumeFrom(descriptor, stage)
                                            "
                                            type="button"
                                            class="btn btn-outline-success"
                                            :disabled="running"
                                            title="Resume from this stage"
                                            :aria-label="`Resume ${descriptor.repositoryName} from ${STAGE_LABELS[stage]}`"
                                            @click="
                                                board.act(() =>
                                                    api.restartStage(descriptor.repositoryName, stage),
                                                )
                                            "
                                        >
                                            <i class="bi bi-chevron-double-right" aria-hidden="true"></i>
                                        </button>

                                        <template v-if="statusOf(descriptor, stage) === 'Fail_Rollback'">
                                            <button
                                                v-if="
                                                    fullRollbackFailed(descriptor) && stage === 'RELEASE_PULL'
                                                "
                                                type="button"
                                                class="btn btn-outline-success"
                                                :disabled="running"
                                                title="Resume from this stage"
                                                :aria-label="`Resume ${descriptor.repositoryName} from ${STAGE_LABELS[stage]}`"
                                                @click="
                                                    board.act(() =>
                                                        api.restartStage(descriptor.repositoryName, stage),
                                                    )
                                                "
                                            >
                                                <i class="bi bi-chevron-double-right" aria-hidden="true"></i>
                                            </button>
                                            <template v-else-if="!fullRollbackFailed(descriptor)">
                                                <button
                                                    type="button"
                                                    class="btn btn-outline-dark"
                                                    :disabled="running"
                                                    title="Retry the rollback"
                                                    :aria-label="`Retry rollback of ${descriptor.repositoryName} ${STAGE_LABELS[stage]}`"
                                                    @click="
                                                        board.act(() =>
                                                            api.rollbackRepository(
                                                                descriptor.repositoryName,
                                                                stage,
                                                            ),
                                                        )
                                                    "
                                                >
                                                    <i class="bi bi-arrow-clockwise" aria-hidden="true"></i>
                                                </button>
                                                <button
                                                    type="button"
                                                    class="btn btn-outline-success"
                                                    :disabled="running"
                                                    title="Skip the rollback"
                                                    :aria-label="`Skip rollback of ${descriptor.repositoryName} ${STAGE_LABELS[stage]}`"
                                                    @click="
                                                        board.act(() =>
                                                            api.skipRollback(
                                                                descriptor.repositoryName,
                                                                stage,
                                                            ),
                                                        )
                                                    "
                                                >
                                                    <i class="bi bi-skip-forward-fill" aria-hidden="true"></i>
                                                </button>
                                            </template>
                                        </template>
                                    </template>
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
