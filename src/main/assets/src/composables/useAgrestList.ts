import { onMounted, ref, shallowRef, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { AgrestQuery, DataResponse, SortDirection } from '@/api/agrest'
import { errorMessage } from '@/api/http'
import { useCacheWarmup } from './useCacheWarmup'

export interface AgrestListOptions<T> {
    fetch: (query: AgrestQuery) => Promise<DataResponse<T>>
    /** Bind sort/filter/field to the URL query, so table filters are linkable and go-back-able. */
    syncQuery?: boolean
}

export function useAgrestList<T>(options: AgrestListOptions<T>) {
    // shallow: rows are replaced wholesale on every reload, never mutated in place
    const items = shallowRef<T[]>([])
    const total = ref(0)
    const loading = ref(false)
    const error = ref('')

    const sort = ref('')
    const direction = ref<SortDirection>('DESC')
    const filterField = ref('')
    const filterValue = ref('')

    const route = options.syncQuery ? useRoute() : null
    const router = options.syncQuery ? useRouter() : null
    const { waitForCache } = useCacheWarmup()

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

    async function reload(): Promise<void> {
        loading.value = true
        error.value = ''
        try {
            await waitForCache()
            const response = await options.fetch({
                sort: sort.value || undefined,
                direction: direction.value,
                filter: filterField.value
                    ? { field: filterField.value, value: filterValue.value }
                    : undefined,
            })
            items.value = response.data ?? []
            total.value = response.total ?? items.value.length
        } catch (e) {
            error.value = errorMessage(e)
            items.value = []
            total.value = 0
        } finally {
            loading.value = false
        }
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
