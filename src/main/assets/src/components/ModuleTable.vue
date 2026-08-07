<script setup lang="ts">
import { computed } from 'vue'
import TableFrame from './TableFrame.vue'
import ExternalLink from './ExternalLink.vue'
import StatusBadge from './StatusBadge.vue'
import ResultButton from './ResultButton.vue'
import type { Project } from '@/api/types'
import type { RepoJobResult } from '@/composables/useJobStatus'

const props = withDefaults(
    defineProps<{
        /**
         * Server-ordered - in Maven dependency order - and never re-sorted here. A module whose
         * `disable` flag is set is shown but cannot be picked.
         */
        modules: Project[]
        /** Omit to drop the checkbox column entirely, for a read-only listing. */
        selected?: string[]
        /** Omit to render `selectHeader` as text instead of a select-all checkbox. */
        allSelected?: boolean
        selectHeader?: string
        /** Per-repository outcome of the running batch job, keyed by repository name. */
        results?: Record<string, RepoJobResult>
        disabled?: boolean
    }>(),
    { selectHeader: 'Select' },
)

const emit = defineEmits<{
    'update:selected': [names: string[]]
    'update:allSelected': [value: boolean]
    /** The row clicked and its new state, for a selection the server has to resolve. */
    toggle: [module: Project, selected: boolean]
}>()

const selectable = computed(() => props.selected !== undefined)
const selectAll = computed(() => props.allSelected !== undefined)

// gated on results being non-empty, not merely present: before a job runs there is nothing to
// report, and two permanently blank columns are not information
const showResults = computed(() => props.results !== undefined && Object.keys(props.results).length > 0)

function toggle(module: Project) {
    const name = module.repository.name
    const isSelected = props.selected?.includes(name) ?? false

    // update:selected first, and only then toggle: emits are synchronous, so a `toggle` listener
    // that reads the bound selection must see the post-click set rather than the pre-click one
    emit(
        'update:selected',
        isSelected ? (props.selected ?? []).filter((n) => n !== name) : [...(props.selected ?? []), name],
    )
    emit('toggle', module, !isSelected)
}
</script>

<template>
    <TableFrame>
        <thead>
            <tr>
                <th v-if="selectable" scope="col" class="app-col-shrink">
                    <input
                        v-if="selectAll"
                        class="form-check-input"
                        type="checkbox"
                        :checked="allSelected"
                        :disabled="disabled"
                        aria-label="Select all modules"
                        @change="emit('update:allSelected', ($event.target as HTMLInputElement).checked)"
                    />
                    <template v-else>{{ selectHeader }}</template>
                </th>
                <th scope="col">Module</th>
                <th scope="col">Version</th>
                <slot name="head" />
                <th v-if="showResults" scope="col">Status</th>
                <th v-if="showResults" scope="col" class="text-end">Result</th>
            </tr>
        </thead>
        <tbody>
            <tr v-for="module in modules" :key="module.id" :class="{ 'opacity-50': module.disable }">
                <td v-if="selectable">
                    <input
                        :id="`mod-${module.repository.name}`"
                        class="form-check-input"
                        type="checkbox"
                        :checked="selected!.includes(module.repository.name)"
                        :disabled="disabled || module.disable"
                        @change="toggle(module)"
                    />
                </td>
                <td>
                    <!-- the name labels the row checkbox, so the link to GitHub has to be separate -->
                    <template v-if="selectable">
                        <label :for="`mod-${module.repository.name}`" class="mb-0">
                            {{ module.repository.name }}
                        </label>
                        <ExternalLink :href="module.repository.url" icon-only />
                    </template>
                    <ExternalLink v-else :href="module.repository.url">
                        {{ module.repository.name }}
                    </ExternalLink>
                </td>
                <td class="text-nowrap">{{ module.version }}</td>
                <slot name="row" :module="module" />
                <td v-if="showResults">
                    <StatusBadge :status="results![module.repository.name]?.status ?? null" />
                </td>
                <td v-if="showResults" class="text-end">
                    <ResultButton
                        :result="results![module.repository.name]?.result ?? ''"
                        :title="module.repository.name"
                    />
                </td>
            </tr>
        </tbody>
    </TableFrame>
</template>
