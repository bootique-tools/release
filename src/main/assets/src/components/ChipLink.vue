<script setup lang="ts">
import type { RouteLocationRaw } from 'vue-router'

withDefaults(
    defineProps<{
        /** An in-app destination. Mutually exclusive with `href`. */
        to?: RouteLocationRaw
        /** An external destination, opened in a new tab. */
        href?: string
        tone?: 'secondary' | 'primary' | 'info'
    }>(),
    { tone: 'secondary' },
)
</script>

<!--
  A badge that is also a link: issue counts, the "parent" marker, an issue number. Worth a component
  rather than a class alone because the two destinations need different elements.
-->

<template>
    <RouterLink v-if="to" :to="to" class="badge text-decoration-none" :class="`chip-${tone}`">
        <slot />
    </RouterLink>
    <a
        v-else-if="href"
        :href="href"
        target="_blank"
        rel="noopener"
        class="badge text-decoration-none"
        :class="`chip-${tone}`"
    >
        <slot />
    </a>
</template>
