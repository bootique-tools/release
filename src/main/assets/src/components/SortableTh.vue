<script setup lang="ts">
import { computed } from 'vue'
import type { SortDirection } from '@/api/agrest'

const props = defineProps<{
    field: string
    activeField: string
    direction: SortDirection
}>()

const emit = defineEmits<{ sort: [field: string] }>()

const active = computed(() => props.activeField === props.field)
const ariaSort = computed(() =>
    active.value ? (props.direction === 'ASC' ? 'ascending' : 'descending') : 'none',
)
</script>

<template>
    <th scope="col" :aria-sort="ariaSort">
        <button type="button" class="btn btn-link btn-sm th-sort p-0" @click="emit('sort', field)">
            <slot />
            <i
                v-if="active"
                class="bi ms-1"
                :class="direction === 'ASC' ? 'bi-caret-up-fill' : 'bi-caret-down-fill'"
                aria-hidden="true"
            ></i>
        </button>
    </th>
</template>
