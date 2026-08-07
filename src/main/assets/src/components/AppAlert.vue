<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
    defineProps<{
        message: string
        tone?: 'danger' | 'warning' | 'info'
        /** Show a spinner in place of the tone icon, for a banner about work in flight. */
        busy?: boolean
        /** Renders a call-to-action button alongside the message. */
        actionLabel?: string
        dismissible?: boolean
    }>(),
    { tone: 'danger', busy: false, dismissible: true },
)

const emit = defineEmits<{ action: []; dismiss: [] }>()

const ICONS: Record<string, string> = {
    danger: 'bi-exclamation-triangle-fill',
    warning: 'bi-exclamation-circle-fill',
    info: 'bi-info-circle-fill',
}

const icon = computed(() => ICONS[props.tone])
</script>

<!--
  The only banner in the app, inside modals as much as on pages - there is deliberately no second
  error surface. Server messages are multi-line, hence .log-output on the text.
-->

<template>
    <div v-if="message" class="alert d-flex align-items-center gap-3" :class="`alert-${tone}`" role="alert">
        <span v-if="busy" class="spinner-border spinner-border-sm flex-shrink-0" aria-hidden="true"></span>
        <i v-else class="bi flex-shrink-0" :class="icon" aria-hidden="true"></i>

        <span class="flex-grow-1 log-output">{{ message }}</span>

        <button
            v-if="actionLabel"
            type="button"
            class="btn btn-sm flex-shrink-0"
            :class="`btn-${tone}`"
            @click="emit('action')"
        >
            {{ actionLabel }}
        </button>
        <button
            v-if="dismissible"
            type="button"
            class="btn-close flex-shrink-0"
            aria-label="Dismiss"
            @click="emit('dismiss')"
        ></button>
    </div>
</template>
