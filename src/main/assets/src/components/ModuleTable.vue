<script setup lang="ts">
import StatusBadge from './StatusBadge.vue'
import ResultButton from './ResultButton.vue'
import type { Project } from '@/api/types'
import type { RepoJobResult } from '@/composables/useJobStatus'

defineProps<{
    modules: Project[]
    selected: string[]
    allSelected: boolean
    /** Per-repository outcome of the running batch job, keyed by repository name. */
    results?: Record<string, RepoJobResult>
    disabled?: boolean
}>()

const emit = defineEmits<{
    'update:selected': [names: string[]]
    'update:allSelected': [value: boolean]
}>()

function toggle(selected: string[], name: string) {
    const next = selected.includes(name) ? selected.filter((n) => n !== name) : [...selected, name]
    emit('update:selected', next)
}
</script>

<template>
    <div class="table-responsive">
        <table class="table table-sm table-hover align-middle">
            <thead>
                <tr>
                    <th scope="col" style="width: 1%">
                        <input
                            class="form-check-input"
                            type="checkbox"
                            :checked="allSelected"
                            :disabled="disabled"
                            aria-label="Select all modules"
                            @change="emit('update:allSelected', ($event.target as HTMLInputElement).checked)"
                        />
                    </th>
                    <th scope="col">Module</th>
                    <th scope="col">Version</th>
                    <slot name="head" />
                    <th v-if="results" scope="col">Status</th>
                    <th v-if="results" scope="col" class="text-end">Result</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="module in modules" :key="module.id">
                    <td>
                        <input
                            :id="`mod-${module.repository.name}`"
                            class="form-check-input"
                            type="checkbox"
                            :value="module.repository.name"
                            :checked="selected.includes(module.repository.name)"
                            :disabled="disabled"
                            @change="toggle(selected, module.repository.name)"
                        />
                    </td>
                    <td>
                        <label :for="`mod-${module.repository.name}`" class="mb-0">
                            {{ module.repository.name }}
                        </label>
                        <a
                            :href="module.repository.url"
                            target="_blank"
                            rel="noopener"
                            class="ms-1 link-secondary"
                            aria-label="Open on GitHub"
                        >
                            <i class="bi bi-box-arrow-up-right small" aria-hidden="true"></i>
                        </a>
                    </td>
                    <td class="text-nowrap">{{ module.version }}</td>
                    <slot name="row" :module="module" />
                    <td v-if="results">
                        <StatusBadge :status="results[module.repository.name]?.status ?? null" />
                    </td>
                    <td v-if="results" class="text-end">
                        <ResultButton
                            :result="results[module.repository.name]?.result ?? ''"
                            :title="module.repository.name"
                        />
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</template>
