<script setup lang="ts">
import AppModal from './AppModal.vue'
import { STAGE_LABELS, type ReleaseStage } from '@/api/types'

defineProps<{
    open: boolean
    repository: string
    stage: ReleaseStage | null
    kind: 'release' | 'rollback'
    text: string
    loading: boolean
    error: string
}>()

const emit = defineEmits<{ close: [] }>()
</script>

<template>
    <AppModal :open="open" size="xl"
              :title="`${kind === 'rollback' ? 'Rollback' : 'Release'} log`"
              @close="emit('close')">
        <p class="text-body-secondary small mb-2">
            {{ repository }} <span aria-hidden="true">·</span>
            {{ stage ? STAGE_LABELS[stage] : '' }}
        </p>

        <div v-if="loading" class="d-flex align-items-center gap-2 text-body-secondary">
            <span class="spinner-border spinner-border-sm" aria-hidden="true"></span>
            Loading&hellip;
        </div>
        <p v-else-if="error" class="text-danger mb-0">{{ error }}</p>
        <pre v-else-if="text.trim()" class="log-output mb-0"><code>{{ text }}</code></pre>
        <p v-else class="text-body-secondary mb-0">This stage produced no log output.</p>
    </AppModal>
</template>
