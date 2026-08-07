<script setup lang="ts">
defineProps<{ title: string; subtitle?: string }>()
</script>

<template>
    <header class="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-3">
        <div>
            <h1 class="h4 mb-0">{{ title }}</h1>
            <p v-if="subtitle" class="text-body-secondary small mb-0">{{ subtitle }}</p>
        </div>
        <div class="d-flex align-items-center gap-2">
            <slot name="actions" />
        </div>
    </header>

    <slot />
</template>
