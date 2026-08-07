<script setup lang="ts">
import AppModal from './AppModal.vue'
import AppAlert from './AppAlert.vue'
import InlineSpinner from './InlineSpinner.vue'
import LogOutput from './LogOutput.vue'
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
    <AppModal
        :open="open"
        size="xl"
        :title="`${kind === 'rollback' ? 'Rollback' : 'Release'} log`"
        @close="emit('close')"
    >
        <p class="text-body-secondary small mb-2">
            {{ repository }} <span aria-hidden="true">·</span>
            {{ stage ? STAGE_LABELS[stage] : '' }}
        </p>

        <InlineSpinner v-if="loading" />
        <AppAlert v-else-if="error" :message="error" :dismissible="false" />
        <LogOutput v-else :text="text" empty-message="This stage produced no log output." />
    </AppModal>
</template>
