<script setup lang="ts">
withDefaults(
    defineProps<{
        /**
         * `data` is every listing. `matrix` is the release board: a repository x stage grid whose
         * first column stays pinned while the stage columns scroll, and which drops row hover
         * because the cells carry their own status colouring.
         */
        variant?: 'data' | 'matrix'
    }>(),
    { variant: 'data' },
)
</script>

<!--
  A table, framed as a card and scrollable inside it.

  The single place the canonical table classes are written: six tables used to open with their own
  hand-copied wrapper and had already drifted into three different class sets. Every table in the
  app is framed, so this owns the card rather than each view wrapping it in an AppSection.

  The scroll container is inside the card, so the card's overflow clipping is an ancestor of the
  scrollport rather than between it and the sticky first column - which is what lets `matrix` work.
-->

<template>
    <section class="card app-section app-section--flush">
        <div class="table-responsive">
            <table
                class="table table-sm app-table"
                :class="variant === 'matrix' ? 'app-table--sticky-first' : 'table-hover'"
            >
                <slot />
            </table>
        </div>
    </section>
</template>
