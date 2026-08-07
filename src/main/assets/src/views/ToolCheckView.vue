<script setup lang="ts">
import PageShell from '@/components/PageShell.vue'
import LoadingBar from '@/components/LoadingBar.vue'
import ErrorAlert from '@/components/ErrorAlert.vue'
import { useAsyncResource } from '@/composables/useAsyncResource'
import { fetchToolCheck } from '@/api/session'
import type { ToolCheck } from '@/api/types'

const {
    data: tools,
    loading,
    error,
    reload,
} = useAsyncResource({
    initial: null as ToolCheck | null,
    load: fetchToolCheck,
})
</script>

<template>
    <PageShell
        title="Tool check"
        subtitle="Versions of the external tools the release pipeline shells out to"
    >
        <template #actions>
            <button
                type="button"
                class="btn btn-sm btn-outline-secondary"
                :disabled="loading"
                @click="reload()"
            >
                <i class="bi bi-arrow-repeat me-1" aria-hidden="true"></i>Re-check
            </button>
        </template>

        <ErrorAlert :message="error" @dismiss="error = ''" />
        <LoadingBar :loading="loading" />

        <div v-if="tools" class="row row-cols-1 row-cols-lg-3 g-3">
            <div
                v-for="tool in [
                    { name: 'Java', value: tools.javaVersion, icon: 'bi-cup-hot' },
                    { name: 'Maven', value: tools.mavenVersion, icon: 'bi-box-seam' },
                    { name: 'JDK', value: tools.jdk, icon: 'bi-gear' },
                ]"
                :key="tool.name"
                class="col"
            >
                <div class="card h-100">
                    <div class="card-body">
                        <h2 class="h6 card-title">
                            <i class="bi me-1" :class="tool.icon" aria-hidden="true"></i>{{ tool.name }}
                        </h2>
                        <p class="text-report small mb-0 font-monospace">{{ tool.value }}</p>
                    </div>
                </div>
            </div>
        </div>
    </PageShell>
</template>
