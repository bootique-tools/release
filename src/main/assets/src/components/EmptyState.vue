<script setup lang="ts">
defineProps<{ message: string; icon?: string }>()
</script>

<template>
    <div class="text-center text-body-secondary py-5">
        <i class="bi fs-2 d-block mb-2" :class="icon ?? 'bi-inbox'" aria-hidden="true"></i>
        <p class="mb-0">{{ message }}</p>
        <slot />
    </div>
</template>
