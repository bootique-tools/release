<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import PageShell from '@/components/PageShell.vue'
import LoadingBar from '@/components/LoadingBar.vue'
import AppAlert from '@/components/AppAlert.vue'
import EmptyState from '@/components/EmptyState.vue'
import ModuleTable from '@/components/ModuleTable.vue'
import JobProgress from '@/components/JobProgress.vue'
import MilestoneActionModal from '@/components/MilestoneActionModal.vue'
import ChipLink from '@/components/ChipLink.vue'
import ExternalLink from '@/components/ExternalLink.vue'
import BusyButton from '@/components/BusyButton.vue'
import { useModuleSelection } from '@/composables/useModuleSelection'
import { useJobStatus } from '@/composables/useJobStatus'
import { useAsyncResource } from '@/composables/useAsyncResource'
import { listMilestoneProjects, startMilestoneJob, type MilestoneAction } from '@/api/milestones'
import { errorMessage } from '@/api/http'
import type { Project } from '@/api/types'

const {
    data: modules,
    loading,
    error,
} = useAsyncResource({
    initial: [] as Project[],
    load: async () => (await listMilestoneProjects()).data ?? [],
    warmCache: true,
})

const names = computed(() => modules.value.map((m) => m.repository.name))
const { selected, allSelected, hasSelection } = useModuleSelection(names)
const {
    running: jobRunning,
    progress: jobProgress,
    byRepo: jobResults,
    start: startJob,
    resume: resumeJob,
    reset: resetJob,
} = useJobStatus('milestone')

const pendingAction = ref<MilestoneAction | null>(null)

async function run(titles: { milestoneTitle?: string; milestoneNewTitle?: string }) {
    const action = pendingAction.value
    pendingAction.value = null
    if (!action) {
        return
    }

    error.value = ''
    resetJob()
    try {
        await startMilestoneJob(action, selected.value, titles)
        // the server binds its listener to the job current at connect time, so connect after this resolves
        await startJob()
    } catch (e) {
        error.value = errorMessage(e)
    }
}

onMounted(resumeJob)
</script>

<template>
    <PageShell title="Milestones" subtitle="Create, close or rename milestones across modules">
        <template #actions>
            <BusyButton
                v-for="action in ['create', 'close', 'rename'] as MilestoneAction[]"
                :key="action"
                variant="primary"
                :disabled="!hasSelection || jobRunning"
                @click="pendingAction = action"
            >
                {{ action[0]!.toUpperCase() + action.slice(1) }}
            </BusyButton>
        </template>

        <AppAlert :message="error" @dismiss="error = ''" />
        <JobProgress :progress="jobProgress" :running="jobRunning" />
        <LoadingBar :loading="loading" />

        <ModuleTable
            v-if="modules.length"
            v-model:selected="selected"
            v-model:all-selected="allSelected"
            :modules="modules"
            :results="jobResults"
            :disabled="jobRunning"
        >
            <template #head>
                <th scope="col">Open milestones</th>
            </template>
            <template #row="{ module }">
                <td>
                    <div v-for="milestone in module.repository.milestones" :key="milestone.id" class="small">
                        <ExternalLink :href="milestone.url">{{ milestone.title }}</ExternalLink>
                        <ChipLink
                            :to="{
                                path: '/issue',
                                query: { filter: 'milestone.title', field: milestone.title },
                            }"
                            class="ms-1"
                        >
                            {{ milestone.openIssues?.length ?? 0 }} issues
                        </ChipLink>
                    </div>
                    <span v-if="!module.repository.milestones?.length" class="text-body-secondary small">
                        none
                    </span>
                </td>
            </template>
        </ModuleTable>

        <EmptyState v-else-if="!loading" message="No modules found." icon="bi-flag" />

        <MilestoneActionModal
            :action="pendingAction"
            :modules="selected"
            @confirm="run"
            @cancel="pendingAction = null"
        />
    </PageShell>
</template>
