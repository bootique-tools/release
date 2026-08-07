<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import PageShell from '@/components/PageShell.vue'
import LoadingBar from '@/components/LoadingBar.vue'
import ErrorAlert from '@/components/ErrorAlert.vue'
import EmptyState from '@/components/EmptyState.vue'
import ModuleTable from '@/components/ModuleTable.vue'
import JobProgress from '@/components/JobProgress.vue'
import { useModuleSelection } from '@/composables/useModuleSelection'
import { useJobStatus } from '@/composables/useJobStatus'
import { useCacheWarmup } from '@/composables/useCacheWarmup'
import { listBranchProjects, startBranchJob, type BranchAction } from '@/api/branches'
import { errorMessage } from '@/api/http'
import type { Project } from '@/api/types'

const modules = ref<Project[]>([])
const loading = ref(false)
const error = ref('')
const branchTitle = ref('')

const names = computed(() => modules.value.map((m) => m.repository.name))
const selection = useModuleSelection(names)
const job = useJobStatus('branches')
const { waitForCache } = useCacheWarmup()

const canStart = computed(
    () => selection.hasSelection.value && branchTitle.value.trim() !== '' && !job.running.value,
)

async function load() {
    loading.value = true
    error.value = ''
    try {
        await waitForCache()
        modules.value = (await listBranchProjects()).data ?? []
    } catch (e) {
        error.value = errorMessage(e)
    } finally {
        loading.value = false
    }
}

async function run(action: BranchAction) {
    error.value = ''
    job.reset()
    try {
        await startBranchJob(action, selection.selected.value, branchTitle.value.trim())
        await job.start()
    } catch (e) {
        error.value = errorMessage(e)
    }
}

onMounted(async () => {
    await load()
    await job.resume()
})
</script>

<template>
    <PageShell title="Branches" subtitle="Create or check out a branch across modules">
        <template #actions>
            <label class="visually-hidden" for="branch-title">Branch name</label>
            <input id="branch-title" v-model="branchTitle" type="text" class="form-control form-control-sm"
                   style="width: 14rem" placeholder="Branch name" :disabled="job.running.value"/>
            <button type="button" class="btn btn-sm btn-outline-primary" :disabled="!canStart"
                    @click="run('createBranch')">
                Create
            </button>
            <button type="button" class="btn btn-sm btn-outline-secondary" :disabled="!canStart"
                    @click="run('checkoutBranch')">
                Check out
            </button>
        </template>

        <ErrorAlert :message="error" @dismiss="error = ''"/>
        <JobProgress :progress="job.progress.value" :running="job.running.value"/>
        <LoadingBar :loading="loading"/>

        <ModuleTable v-if="modules.length" :modules="modules"
                     :selected="selection.selected.value"
                     :all-selected="selection.allSelected.value"
                     :results="job.byRepo" :disabled="job.running.value"
                     @update:selected="selection.selected.value = $event"
                     @update:all-selected="selection.allSelected.value = $event">
            <template #head>
                <th scope="col">Current branch</th>
            </template>
            <template #row="{ module }">
                <td class="text-nowrap">
                    <!-- the job reports the new branch as each module completes, ahead of a reload -->
                    <code v-if="job.byRepo[module.repository.name]?.branchName">
                        {{ job.byRepo[module.repository.name]!.branchName }}
                    </code>
                    <code v-else-if="module.branchName">{{ module.branchName }}</code>
                    <span v-else class="text-body-secondary">—</span>
                </td>
            </template>
        </ModuleTable>

        <EmptyState v-else-if="!loading" message="No modules found." icon="bi-signpost"/>
    </PageShell>
</template>
