<script setup lang="ts">
import { computed, useSlots } from 'vue'

const props = withDefaults(
    defineProps<{
        /** In flight: the icon is replaced by a spinner. */
        busy?: boolean
        /**
         * Disabled for a reason other than this button's own work - a precondition is unmet, or
         * another row's action is running. Kept separate from `busy` so a table can disable every
         * row while spinning only the one that was clicked.
         */
        disabled?: boolean
        /** A bootstrap-icons class, shown while idle. */
        icon?: string
        variant?: 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info' | 'dark'
        outline?: boolean
        size?: 'sm' | 'md'
        /** Accessible name. Required when there is no slot content, i.e. an icon-only button. */
        label?: string
    }>(),
    { variant: 'secondary', outline: true, size: 'sm' },
)

const emit = defineEmits<{ click: [] }>()

const slots = useSlots()
const hasText = computed(() => slots.default !== undefined)

const classes = computed(() => [
    'btn',
    `btn-${props.outline ? 'outline-' : ''}${props.variant}`,
    props.size === 'sm' ? 'btn-sm' : null,
])
</script>

<!--
  One button. The icon/spinner swap and the "margin only when there is text" rule lived at six call
  sites with three different margin utilities between them.
-->

<template>
    <button
        type="button"
        :class="classes"
        :disabled="busy || disabled"
        :aria-label="label"
        :title="label"
        @click="emit('click')"
    >
        <span
            v-if="busy"
            class="spinner-border spinner-border-sm"
            :class="{ 'me-1': hasText }"
            aria-hidden="true"
        ></span>
        <i v-else-if="icon" class="bi" :class="[icon, { 'me-1': hasText }]" aria-hidden="true"></i>
        <slot />
    </button>
</template>
