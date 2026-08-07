<script setup lang="ts">
defineProps<{ loading: boolean }>()
</script>

<template>
    <div v-if="loading" class="indeterminate-bar my-3" role="status" aria-label="Loading">
        <div class="indeterminate-bar__block"></div>
    </div>
</template>
