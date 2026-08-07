<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ progress: number; running: boolean }>()

const percent = computed(() => Math.round(props.progress))
</script>

<template>
    <!-- the bar is the same height as LoadingBar so the two are interchangeable in one slot, which
         leaves no room for the label inside it -->
    <div v-if="running || progress > 0" class="d-flex align-items-center gap-2">
        <div
            class="progress flex-grow-1"
            role="progressbar"
            :aria-valuenow="percent"
            aria-valuemin="0"
            aria-valuemax="100"
            aria-label="Job progress"
        >
            <div
                class="progress-bar"
                :class="{ 'progress-bar-animated progress-bar-striped': running }"
                :style="{ width: `${percent}%` }"
            ></div>
        </div>
        <span class="small text-body-secondary text-nowrap">{{ percent }}%</span>
    </div>
</template>
