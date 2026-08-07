<script setup lang="ts">
import { ref } from 'vue'
import AppModal from './AppModal.vue'

defineProps<{ result: string; title: string }>()

const open = ref(false)
</script>

<template>
    <template v-if="result">
        <button type="button" class="btn btn-sm btn-outline-secondary"
                :aria-label="`Show result for ${title}`" title="Show result" @click="open = true">
            <i class="bi bi-eye" aria-hidden="true"></i>
        </button>
        <AppModal :open="open" :title="title" size="lg" @close="open = false">
            <pre class="log-output mb-0"><code>{{ result }}</code></pre>
        </AppModal>
    </template>
</template>
