<script setup lang="ts">
import { ref } from 'vue'
import AppModal from './AppModal.vue'
import BusyButton from './BusyButton.vue'
import LogOutput from './LogOutput.vue'

defineProps<{ result: string; title: string }>()

const open = ref(false)
</script>

<template>
    <template v-if="result">
        <BusyButton icon="bi-eye" :label="`Show result for ${title}`" @click="open = true" />
        <AppModal :open="open" :title="title" size="lg" @close="open = false">
            <LogOutput :text="result" />
        </AppModal>
    </template>
</template>
