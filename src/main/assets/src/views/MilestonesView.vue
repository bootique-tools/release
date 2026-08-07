<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import PageShell from '@/components/PageShell.vue'
import LoadingBar from '@/components/LoadingBar.vue'
import ErrorAlert from '@/components/ErrorAlert.vue'
import EmptyState from '@/components/EmptyState.vue'
import ModuleTable from '@/components/ModuleTable.vue'
import JobProgress from '@/components/JobProgress.vue'
import MilestoneActionModal from '@/components/MilestoneActionModal.vue'
import { useModuleSelection } from '@/composables/useModuleSelection'
import { useJobStatus } from '@/composables/useJobStatus'
import { useCacheWarmup } from '@/composables/useCacheWarmup'
import { listMilestoneProjects, startMilestoneJob, type MilestoneAction } from '@/api/milestones'
import { errorMessage } from '@/api/http'
import type { Project } from '@/api/types'

const modules = ref<Project[]>([])
const loading = ref(false)
const error = ref('')

const names = computed(() => modules.value.map((m) => m.repository.name))
const selection = useModuleSelection(names)
const job = useJobStatus('milestone')
const { waitForCache } = useCacheWarmup()

const pendingAction = ref<MilestoneAction | null>(null)

async function load() {
    loading.value = true
    error.value = ''
    try {
        await waitForCache()
        modules.value = (await listMilestoneProjects()).data ?? []
    } catch (e) {
        error.value = errorMessage(e)
    } finally {
        loading.value = false
    }
}

async function run(titles: { milestoneTitle?: string; milestoneNewTitle?: string }) {
    const action = pendingAction.value
    pendingAction.value = null
    if (!action) {
        return
    }

    error.value = ''
    job.reset()
    try {
        await startMilestoneJob(action, selection.selected.value, titles)
        // the server binds its listener to the job current at connect time, so connect after this resolves
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
    <PageShell title="Milestones" subtitle="Create, close or rename milestones across modules">
        <template #actions>
            <button
                v-for="action in ['create', 'close', 'rename'] as MilestoneAction[]"
                :key="action"
                type="button"
                class="btn btn-sm btn-outline-primary"
                :disabled="!selection.hasSelection.value || job.running.value"
                @click="pendingAction = action"
            >
                {{ action[0]!.toUpperCase() + action.slice(1) }}
            </button>
        </template>

        <ErrorAlert :message="error" @dismiss="error = ''" />
        <JobProgress :progress="job.progress.value" :running="job.running.value" />
        <LoadingBar :loading="loading" />

        <ModuleTable
            v-if="modules.length"
            :modules="modules"
            :selected="selection.selected.value"
            :all-selected="selection.allSelected.value"
            :results="job.byRepo"
            :disabled="job.running.value"
            @update:selected="selection.selected.value = $event"
            @update:all-selected="selection.allSelected.value = $event"
        >
            <template #head>
                <th scope="col">Open milestones</th>
            </template>
            <template #row="{ module }">
                <td>
                    <div v-for="milestone in module.repository.milestones" :key="milestone.id" class="small">
                        <a :href="milestone.url" target="_blank" rel="noopener">{{ milestone.title }}</a>
                        <RouterLink
                            :to="{
                                path: '/issue',
                                query: { filter: 'milestone.title', field: milestone.title },
                            }"
                            class="badge text-bg-secondary ms-1 text-decoration-none"
                        >
                            {{ milestone.openIssues?.length ?? 0 }} issues
                        </RouterLink>
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
            :modules="selection.selected.value"
            @confirm="run"
            @cancel="pendingAction = null"
        />
    </PageShell>
</template>
