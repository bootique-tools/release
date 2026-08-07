<script setup lang="ts">
withDefaults(
    defineProps<{
        title?: string
        subtitle?: string
        /**
         * The section is nothing but a table: drop the body padding and let the card clip the
         * scrollable table to its own radius.
         */
        flush?: boolean
    }>(),
    { flush: false },
)
</script>

<!--
  One region of a page, framed as a card.

  Status affordances - AppAlert, LoadingBar, JobProgress - deliberately stay OUTSIDE sections, in
  the page flow, because they belong to the page rather than to any one region. An EmptyState goes
  inside, replacing the body, so the frame does not flicker in and out as data arrives.
-->

<template>
    <section class="card app-section" :class="{ 'app-section--flush': flush }">
        <div v-if="title" class="card-header">
            <div class="d-flex flex-wrap align-items-center justify-content-between gap-2">
                <div>
                    <h2 class="h6 mb-0">{{ title }}</h2>
                    <p v-if="subtitle" class="text-body-secondary small mb-0">{{ subtitle }}</p>
                </div>
                <slot name="actions" />
            </div>
        </div>

        <slot v-if="flush" />
        <div v-else class="card-body">
            <slot />
        </div>
    </section>
</template>
