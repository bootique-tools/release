<script setup lang="ts">
import AppModal from './AppModal.vue'

withDefaults(
    defineProps<{
        open: boolean
        title: string
        message: string
        confirmLabel?: string
        variant?: 'primary' | 'danger' | 'warning'
        busy?: boolean
    }>(),
    { confirmLabel: 'Confirm', variant: 'primary', busy: false },
)

const emit = defineEmits<{ confirm: []; cancel: [] }>()
</script>

<template>
    <AppModal :open="open" :title="title" size="sm" @close="emit('cancel')">
        <p class="mb-0">{{ message }}</p>
        <template #footer>
            <button type="button" class="btn btn-secondary" @click="emit('cancel')">Cancel</button>
            <button
                type="button"
                class="btn"
                :class="`btn-${variant}`"
                :disabled="busy"
                @click="emit('confirm')"
            >
                <span v-if="busy" class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>
                {{ confirmLabel }}
            </button>
        </template>
    </AppModal>
</template>
