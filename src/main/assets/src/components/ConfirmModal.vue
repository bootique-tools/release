<script setup lang="ts">
import AppModal from './AppModal.vue'
import BusyButton from './BusyButton.vue'

withDefaults(
    defineProps<{
        open: boolean
        title: string
        /** A one-line body. Omit it and use the default slot for anything richer. */
        message?: string
        confirmLabel?: string
        cancelLabel?: string
        variant?: 'primary' | 'success' | 'danger' | 'warning'
        /** In flight: the confirm button spins and both buttons are disabled. */
        busy?: boolean
        /** Preconditions unmet: the confirm button is disabled, without a spinner. */
        confirmDisabled?: boolean
        size?: 'sm' | 'lg' | 'xl'
    }>(),
    {
        confirmLabel: 'Confirm',
        cancelLabel: 'Cancel',
        variant: 'primary',
        busy: false,
        confirmDisabled: false,
        size: 'sm',
    },
)

const emit = defineEmits<{ confirm: []; cancel: [] }>()
</script>

<!--
  Every modal that asks for a decision. The three footers this replaces were structurally identical
  - Cancel plus one variant-coloured action - and differed only in body content and in the predicate
  that enabled the action, which is a slot and two booleans rather than three hand-written footers.
-->

<template>
    <AppModal :open="open" :title="title" :size="size" @close="emit('cancel')">
        <slot>
            <p class="mb-0">{{ message }}</p>
        </slot>

        <template #footer>
            <BusyButton :outline="false" :disabled="busy" @click="emit('cancel')">
                {{ cancelLabel }}
            </BusyButton>
            <BusyButton
                :variant="variant"
                :outline="false"
                :busy="busy"
                :disabled="confirmDisabled"
                @click="emit('confirm')"
            >
                {{ confirmLabel }}
            </BusyButton>
        </template>
    </AppModal>
</template>
