<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
    <div class="text-center text-body-secondary py-5">
        <i class="bi bi-signpost-split fs-1 d-block mb-3" aria-hidden="true"></i>
        <h1 class="h4">Page not found</h1>
        <p><code>{{ route.fullPath }}</code> does not match anything.</p>
        <RouterLink class="btn btn-primary" to="/">Back to repositories</RouterLink>
    </div>
</template>
