<script setup lang="ts">
import { onMounted } from 'vue'
import PageShell from '@/components/PageShell.vue'
import LoadingBar from '@/components/LoadingBar.vue'
import AppAlert from '@/components/AppAlert.vue'
import EmptyState from '@/components/EmptyState.vue'
import ModuleTable from '@/components/ModuleTable.vue'
import BusyButton from '@/components/BusyButton.vue'
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
    <PageShell subtitle="Runs mvn clean install across every module, in dependency order">
        <template #actions>
            <BusyButton variant="primary" icon="bi-play-fill" :busy="jobRunning" @click="verify">
                Verify all
            </BusyButton>
        </template>

        <AppAlert :message="error" @dismiss="error = ''" />
        <JobProgress :progress="jobProgress" :running="jobRunning" />
        <LoadingBar :loading="loading" />

        <!-- read-only: no `selected`, so ModuleTable drops the checkbox column -->
        <ModuleTable v-if="modules.length" :modules="modules" :results="jobResults">
            <template #head>
                <th scope="col">Depends on</th>
            </template>
            <template #row="{ module }">
                <td>
                    <span
                        v-for="dependency in module.dependencies"
                        :key="dependency.id"
                        class="badge chip-secondary me-1"
                    >
                        {{ dependency.repository?.name ?? dependency.groupId }}
                    </span>
                    <span v-if="!module.dependencies?.length" class="text-body-secondary">—</span>
                </td>
            </template>
        </ModuleTable>

        <EmptyState v-else-if="!loading" message="No Maven modules found." icon="bi-box-seam" />
    </PageShell>
</template>
