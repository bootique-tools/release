<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import PageShell from '@/components/PageShell.vue'
import LoadingBar from '@/components/LoadingBar.vue'
import AppAlert from '@/components/AppAlert.vue'
import EmptyState from '@/components/EmptyState.vue'
import ModuleTable from '@/components/ModuleTable.vue'
import JobProgress from '@/components/JobProgress.vue'
import BusyButton from '@/components/BusyButton.vue'
import { useModuleSelection } from '@/composables/useModuleSelection'
import { useJobStatus } from '@/composables/useJobStatus'
import { useAsyncResource } from '@/composables/useAsyncResource'
import { listBranchProjects, startBranchJob, type BranchAction } from '@/api/branches'
import { errorMessage } from '@/api/http'
import type { Project } from '@/api/types'

const branchTitle = ref('')

const {
    data: modules,
    loading,
    error,
} = useAsyncResource({
    initial: [] as Project[],
    load: async () => (await listBranchProjects()).data ?? [],
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
} = useJobStatus('branches')

const canStart = computed(() => hasSelection.value && branchTitle.value.trim() !== '' && !jobRunning.value)

async function run(action: BranchAction) {
    error.value = ''
    resetJob()
    try {
        await startBranchJob(action, selected.value, branchTitle.value.trim())
        await startJob()
    } catch (e) {
        error.value = errorMessage(e)
    }
}

onMounted(resumeJob)
</script>

<template>
    <PageShell title="Branches" subtitle="Create or check out a branch across modules">
        <template #actions>
            <label class="visually-hidden" for="branch-title">Branch name</label>
            <input
                id="branch-title"
                v-model="branchTitle"
                type="text"
                class="form-control form-control-sm app-toolbar-input"
                placeholder="Branch name"
                :disabled="jobRunning"
            />
            <BusyButton variant="primary" :disabled="!canStart" @click="run('createBranch')">
                Create
            </BusyButton>
            <BusyButton :disabled="!canStart" @click="run('checkoutBranch')">Check out</BusyButton>
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
                <th scope="col">Current branch</th>
            </template>
            <template #row="{ module }">
                <td class="text-nowrap">
                    <!-- the job reports the new branch as each module completes, ahead of a reload -->
                    <code v-if="jobResults[module.repository.name]?.branchName">
                        {{ jobResults[module.repository.name]!.branchName }}
                    </code>
                    <code v-else-if="module.branchName">{{ module.branchName }}</code>
                    <span v-else class="text-body-secondary">—</span>
                </td>
            </template>
        </ModuleTable>

        <EmptyState v-else-if="!loading" message="No modules found." icon="bi-signpost" />
    </PageShell>
</template>
