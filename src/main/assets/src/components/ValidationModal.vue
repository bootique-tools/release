<script setup lang="ts">
import { ref, watch } from 'vue'
import AppModal from './AppModal.vue'
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
        <div v-if="loading" class="d-flex align-items-center gap-2 text-body-secondary">
            <span class="spinner-border spinner-border-sm" aria-hidden="true"></span>
            Checking every module&hellip;
        </div>
        <p v-else-if="error" class="text-danger mb-0">{{ error }}</p>
        <p v-else class="text-report mb-0">{{ report }}</p>
    </AppModal>
</template>
