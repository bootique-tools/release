<script setup lang="ts">
withDefaults(
    defineProps<{
        href: string
        /**
         * Render just the "opens elsewhere" icon. Used where the text itself is already a control -
         * inside ModuleTable the module name is a <label> driving the row checkbox, so the link to
         * GitHub has to be separate from it.
         */
        iconOnly?: boolean
        label?: string
    }>(),
    { iconOnly: false, label: 'Open on GitHub' },
)
</script>

<template>
    <a
        v-if="iconOnly"
        :href="href"
        target="_blank"
        rel="noopener"
        class="ms-1 link-secondary"
        :aria-label="label"
        :title="label"
    >
        <i class="bi bi-box-arrow-up-right small" aria-hidden="true"></i>
    </a>
    <a v-else :href="href" target="_blank" rel="noopener"><slot /></a>
</template>
