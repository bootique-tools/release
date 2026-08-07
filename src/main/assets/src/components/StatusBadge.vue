<script setup lang="ts">
import { computed } from 'vue'
import type { BatchJobStatus, ReleaseStageStatus } from '@/api/types'

const props = defineProps<{ status: ReleaseStageStatus | BatchJobStatus | null }>()

/**
 * A rolled-back stage presents as not started, because that is operationally what it is:
 * it can simply be run again.
 */
const DISPLAY: Partial<Record<string, string>> = {
    Rollback: 'Not_Start',
    Fail_Rollback: 'Fail',
}

const VARIANT: Record<string, string> = {
    Success: 'success',
    SUCCESS: 'success',
    Skip: 'secondary',
    Fail: 'danger',
    FAILURE: 'danger',
    In_Progress: 'primary',
    IN_PROGRESS: 'primary',
    Reload: 'info',
    // an empty slot rather than a state, and distinguishable from Skip - the two used to render
    // as near-identical greys, one of them nearly invisible in light mode
    Not_Start: 'empty',
}

const effective = computed(() => (props.status ? (DISPLAY[props.status] ?? props.status) : null))
const label = computed(() => effective.value?.replace(/_/g, ' ') ?? '')
const variant = computed(() => (effective.value ? (VARIANT[effective.value] ?? 'secondary') : 'secondary'))
const spinning = computed(() => effective.value === 'In_Progress' || effective.value === 'IN_PROGRESS')
</script>

<template>
    <span v-if="effective" class="badge d-inline-flex align-items-center gap-1" :class="`chip-${variant}`">
        <span v-if="spinning" class="spinner-border spinner-inline" aria-hidden="true"></span>
        {{ label }}
    </span>
</template>
