<script setup lang="ts">
withDefaults(
    defineProps<{
        label?: string
        /** Centred in its own block, for a whole region that has nothing to show yet. */
        block?: boolean
    }>(),
    { label: 'Loading…' },
)
</script>

<!--
  A region is fetching. Distinct from LoadingBar (a page-level listing), JobProgress (a server batch
  job with a real percentage) and BusyButton (one action in flight).
-->

<template>
    <div v-if="block" class="app-placeholder" role="status">
        <span class="spinner-border" aria-hidden="true"></span>
        <span class="visually-hidden">{{ label }}</span>
    </div>
    <div v-else class="d-flex align-items-center gap-2 text-body-secondary" role="status">
        <span class="spinner-border spinner-border-sm" aria-hidden="true"></span>
        {{ label }}
    </div>
</template>
