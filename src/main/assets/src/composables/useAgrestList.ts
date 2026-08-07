import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { AgrestQuery, DataResponse, SortDirection } from '@/api/agrest'
import { useAsyncResource } from './useAsyncResource'

export interface AgrestListOptions<T> {
    fetch: (query: AgrestQuery) => Promise<DataResponse<T>>
    /** Bind sort/filter/field to the URL query, so table filters are linkable and go-back-able. */
    syncQuery?: boolean
}

export function useAgrestList<T>(options: AgrestListOptions<T>) {
    const sort = ref('')
    const direction = ref<SortDirection>('DESC')
    const filterField = ref('')
    const filterValue = ref('')

    const route = options.syncQuery ? useRoute() : null
    const router = options.syncQuery ? useRouter() : null

    const {
        data,
        loading,
        error,
        reload: fetchPage,
    } = useAsyncResource({
        initial: { items: [] as T[], total: 0 },
        warmCache: true,
        // onMounted below owns the first load; the route watcher owns every later one
        immediate: false,
        load: async () => {
            const response = await options.fetch({
                sort: sort.value || undefined,
                direction: direction.value,
                filter: filterField.value
                    ? { field: filterField.value, value: filterValue.value }
                    : undefined,
            })
            const rows = response.data ?? []
            return { items: rows, total: response.total ?? rows.length }
        },
    })

    const items = computed(() => data.value.items)
    const total = computed(() => data.value.total)

    function readQuery() {
        if (!route) {
            return
        }
        sort.value = String(route.query.sort ?? '')
        direction.value = route.query.dir === 'ASC' ? 'ASC' : 'DESC'
        filterField.value = String(route.query.filter ?? '')
        filterValue.value = String(route.query.field ?? '')
    }

    function writeQuery() {
        if (!router || !route) {
            return
        }
        void router.replace({
            query: {
                ...route.query,
                sort: sort.value || undefined,
                dir: sort.value ? direction.value : undefined,
                filter: filterField.value || undefined,
                field: filterValue.value || undefined,
            },
        })
    }

    /**
     * With the query in the URL, the route watcher is the single reload trigger - calling reload()
     * here as well would fire two identical requests per click.
     */
    function applyChange() {
        if (route) {
            writeQuery()
        } else {
            void reload()
        }
    }

    function reload(): Promise<void> {
        return fetchPage()
    }

    /** First click on a column sorts descending; clicking the same column again flips it. */
    function toggleSort(field: string): void {
        if (sort.value === field) {
            direction.value = direction.value === 'ASC' ? 'DESC' : 'ASC'
        } else {
            sort.value = field
            direction.value = 'DESC'
        }
        applyChange()
    }

    function applyFilter(field: string, value: string): void {
        filterField.value = field
        filterValue.value = value
        applyChange()
    }

    function clearFilter(): void {
        filterField.value = ''
        filterValue.value = ''
        applyChange()
    }

    if (route) {
        readQuery()
        // covers both our own writeQuery and an external navigation to the same route with new params
        watch(
            () => [route.query.filter, route.query.field, route.query.sort, route.query.dir],
            () => {
                readQuery()
                void reload()
            },
        )
    }

    onMounted(reload)

    return {
        items,
        total,
        loading,
        error,
        sort,
        direction,
        filterField,
        filterValue,
        reload,
        toggleSort,
        applyFilter,
        clearFilter,
    }
}
