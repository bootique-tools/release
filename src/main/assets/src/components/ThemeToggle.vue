<script setup lang="ts">
import { computed } from 'vue'
import { useTheme } from '@/composables/useTheme'

const { theme, cycle } = useTheme()

const icon = computed(
    () =>
        ({ light: 'bi-sun', dark: 'bi-moon-stars', auto: 'bi-circle-half' })[theme.value],
)
</script>

<template>
    <button type="button" class="btn btn-sm btn-outline-secondary border-0"
            :title="`Theme: ${theme}`" :aria-label="`Theme: ${theme}. Click to change.`"
            @click="cycle">
        <i class="bi" :class="icon" aria-hidden="true"></i>
    </button>
</template>
