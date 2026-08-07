<script setup lang="ts">
import PageShell from '@/components/PageShell.vue'
import LoadingBar from '@/components/LoadingBar.vue'
import AppAlert from '@/components/AppAlert.vue'
import AppSection from '@/components/AppSection.vue'
import BusyButton from '@/components/BusyButton.vue'
import LogOutput from '@/components/LogOutput.vue'
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
            <BusyButton icon="bi-arrow-repeat" :busy="loading" @click="reload()">Re-check</BusyButton>
        </template>

        <AppAlert :message="error" @dismiss="error = ''" />
        <LoadingBar :loading="loading" />

        <div v-if="tools" class="row row-cols-1 row-cols-lg-3 g-3">
            <div
                v-for="tool in [
                    { name: 'Java', value: tools.javaVersion },
                    { name: 'Maven', value: tools.mavenVersion },
                    { name: 'JDK', value: tools.jdk },
                ]"
                :key="tool.name"
                class="col"
            >
                <AppSection :title="tool.name" class="h-100">
                    <LogOutput :text="tool.value" prose empty-message="Not detected." />
                </AppSection>
            </div>
        </div>
    </PageShell>
</template>
