import { computed, type Ref, ref } from 'vue'

/**
 * Checkbox selection over a list of modules, tracked by repository name.
 * Selection survives the row list being replaced, and drops names that are no longer present.
 */
export function useModuleSelection(names: Ref<string[]>) {
    const selected = ref<string[]>([])

    const allSelected = computed({
        get: () => names.value.length > 0 && names.value.every((n) => selected.value.includes(n)),
        set: (value: boolean) => {
            selected.value = value ? [...names.value] : []
        },
    })

    return {
        selected,
        allSelected,
        hasSelection: computed(() => selected.value.length > 0),

        isSelected: (name: string) => selected.value.includes(name),

        toggle(name: string): void {
            const index = selected.value.indexOf(name)
            if (index === -1) {
                selected.value.push(name)
            } else {
                selected.value.splice(index, 1)
            }
        },

        clear(): void {
            selected.value = []
        },
    }
}
