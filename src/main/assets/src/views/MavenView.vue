<script setup lang="ts">
import { computed, onMounted } from 'vue'
import PageShell from '@/components/PageShell.vue'
import LoadingBar from '@/components/LoadingBar.vue'
import ErrorAlert from '@/components/ErrorAlert.vue'
import EmptyState from '@/components/EmptyState.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import ResultButton from '@/components/ResultButton.vue'
import JobProgress from '@/components/JobProgress.vue'
import { useJobStatus } from '@/composables/useJobStatus'
import { useAsyncResource } from '@/composables/useAsyncResource'
import { listReleaseProjects } from '@/api/releaseSelect'
import { verifyAll } from '@/api/maven'
import { errorMessage } from '@/api/http'
import type { Project } from '@/api/types'

const {
    data: modules,
    loading,
    error,
} = useAsyncResource({
    initial: [] as Project[],
    load: async () => (await listReleaseProjects()).data ?? [],
    warmCache: true,
})

const {
    running: jobRunning,
    progress: jobProgress,
    byRepo: jobResults,
    start: startJob,
    resume: resumeJob,
    reset: resetJob,
} = useJobStatus('maven')

const anyResults = computed(() => Object.keys(jobResults).length > 0)

async function verify() {
    error.value = ''
    resetJob()
    try {
        await verifyAll()
        await startJob()
    } catch (e) {
        error.value = errorMessage(e)
    }
}

onMounted(resumeJob)
</script>

<template>
    <PageShell
        title="Maven projects"
        subtitle="Runs mvn clean install across every module, in dependency order"
    >
        <template #actions>
            <button
                type="button"
                class="btn btn-sm btn-outline-primary"
                :disabled="jobRunning"
                @click="verify"
            >
                <span
                    v-if="jobRunning"
                    class="spinner-border spinner-border-sm me-1"
                    aria-hidden="true"
                ></span>
                <i v-else class="bi bi-play-fill me-1" aria-hidden="true"></i>
                Verify all
            </button>
        </template>

        <ErrorAlert :message="error" @dismiss="error = ''" />
        <JobProgress :progress="jobProgress" :running="jobRunning" />
        <LoadingBar :loading="loading" />

        <div v-if="modules.length" class="table-responsive">
            <table class="table table-sm table-hover align-middle">
                <thead>
                    <tr>
                        <th scope="col">Module</th>
                        <th scope="col">Version</th>
                        <th scope="col">Depends on</th>
                        <th v-if="anyResults" scope="col">Status</th>
                        <th v-if="anyResults" scope="col" class="text-end">Result</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="module in modules" :key="module.id">
                        <td>
                            <a :href="module.repository.url" target="_blank" rel="noopener">
                                {{ module.repository.name }}
                            </a>
                        </td>
                        <td class="text-nowrap">{{ module.version }}</td>
                        <td>
                            <span
                                v-for="dependency in module.dependencies"
                                :key="dependency.id"
                                class="badge text-bg-light me-1"
                            >
                                {{ dependency.repository?.name ?? dependency.groupId }}
                            </span>
                            <span v-if="!module.dependencies?.length" class="text-body-secondary">—</span>
                        </td>
                        <td v-if="anyResults">
                            <StatusBadge :status="jobResults[module.repository.name]?.status ?? null" />
                        </td>
                        <td v-if="anyResults" class="text-end">
                            <ResultButton
                                :result="jobResults[module.repository.name]?.result ?? ''"
                                :title="module.repository.name"
                            />
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <EmptyState v-else-if="!loading" message="No Maven modules found." icon="bi-box-seam" />
    </PageShell>
</template>
