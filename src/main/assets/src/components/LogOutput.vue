<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
    defineProps<{
        text: string
        /** Shown instead of the block when there is nothing to display. */
        emptyMessage?: string
        /**
         * Plain multi-line prose from the server (a validation report, a tool version) rather than
         * a command log: no monospace, no scroll cap.
         */
        prose?: boolean
    }>(),
    { prose: false },
)

const hasText = computed(() => props.text.trim() !== '')
</script>

<!--
  Verbatim server output. Three call sites each wrote their own <pre>, one of them adding five
  utility classes the others lacked.
-->

<template>
    <pre
        v-if="hasText"
        class="log-output log-output--framed"
        :class="prose ? null : 'log-output--mono log-output--scroll'"
    ><code>{{ text }}</code></pre>
    <p v-else-if="emptyMessage" class="text-body-secondary mb-0">{{ emptyMessage }}</p>
</template>
