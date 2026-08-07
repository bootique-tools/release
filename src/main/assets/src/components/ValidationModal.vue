<script setup lang="ts">
import { ref, watch } from 'vue'
import AppModal from './AppModal.vue'
import AppAlert from './AppAlert.vue'
import InlineSpinner from './InlineSpinner.vue'
import LogOutput from './LogOutput.vue'
import { validatePoms } from '@/api/session'
import { errorMessage } from '@/api/http'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits<{ close: [] }>()

const report = ref('')
const error = ref('')
const loading = ref(false)

watch(
    () => props.open,
    async (open) => {
        if (!open) {
            return
        }
        loading.value = true
        report.value = ''
        error.value = ''
        try {
            report.value = await validatePoms()
        } catch (e) {
            error.value = errorMessage(e)
        } finally {
            loading.value = false
        }
    },
)
</script>

<template>
    <AppModal :open="open" title="POM validation" size="xl" @close="emit('close')">
        <InlineSpinner v-if="loading" label="Checking every module…" />
        <AppAlert v-else-if="error" :message="error" :dismissible="false" />
        <LogOutput v-else :text="report" prose empty-message="Nothing to report." />
    </AppModal>
</template>
