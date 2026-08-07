<script setup lang="ts">
defineProps<{
    message: string
    /** Renders a call-to-action button alongside the message. */
    actionLabel?: string
}>()

const emit = defineEmits<{ action: []; dismiss: [] }>()
</script>

<template>
    <div v-if="message" class="alert alert-danger d-flex align-items-center gap-3" role="alert">
        <i class="bi bi-exclamation-triangle-fill flex-shrink-0" aria-hidden="true"></i>
        <span class="flex-grow-1 text-report">{{ message }}</span>
        <button v-if="actionLabel" type="button" class="btn btn-sm btn-danger flex-shrink-0"
                @click="emit('action')">
            {{ actionLabel }}
        </button>
        <button type="button" class="btn-close flex-shrink-0" aria-label="Dismiss"
                @click="emit('dismiss')"></button>
    </div>
</template>
