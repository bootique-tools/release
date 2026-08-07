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
  The single place the canonical table classes are written. Six tables used to open with their own
  hand-copied wrapper, and had already drifted into three different class sets.
-->

<template>
    <div class="table-responsive">
        <table
            class="table table-sm app-table"
            :class="variant === 'matrix' ? 'app-table--sticky-first' : 'table-hover'"
        >
            <slot />
        </table>
    </div>
</template>
