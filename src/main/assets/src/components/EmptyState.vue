<script setup lang="ts">
defineProps<{ message: string; icon?: string }>()
</script>

<template>
    <div class="app-placeholder">
        <i class="bi app-placeholder__icon" :class="icon ?? 'bi-inbox'" aria-hidden="true"></i>
        <p class="mb-0">{{ message }}</p>
        <slot />
    </div>
</template>
