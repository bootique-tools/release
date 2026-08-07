<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const props = defineProps<{
    /** Escape hatch. Normally the heading comes from the route, so it lives in exactly one place. */
    title?: string
    subtitle?: string
}>()

const route = useRoute()
const heading = computed(() => props.title ?? route.meta.title ?? '')
</script>

<template>
    <header class="app-page__header d-flex flex-wrap align-items-center justify-content-between gap-2">
        <div>
            <h1 class="mb-0">{{ heading }}</h1>
            <p v-if="subtitle" class="text-body-secondary small mb-0">{{ subtitle }}</p>
        </div>
        <div class="d-flex align-items-center gap-2">
            <slot name="actions" />
        </div>
    </header>

    <!-- owns the vertical rhythm, so alerts, bars and sections carry no margins of their own -->
    <div class="app-flow">
        <slot />
    </div>
</template>
