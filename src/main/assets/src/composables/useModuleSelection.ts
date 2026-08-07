import { computed, type Ref, ref } from 'vue'

/**
 * Checkbox selection over a list of modules, tracked by repository name rather than by row object,
 * so it survives the row list being replaced on reload.
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
    }
}
