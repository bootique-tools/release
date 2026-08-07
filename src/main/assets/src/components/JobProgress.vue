<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ progress: number; running: boolean }>()

const percent = computed(() => Math.round(props.progress))
</script>

<template>
    <div v-if="running || progress > 0" class="mb-3">
        <div
            class="progress"
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
            >
                {{ percent }}%
            </div>
        </div>
    </div>
</template>
