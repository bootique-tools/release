import { onMounted, ref, shallowRef, type Ref, type ShallowRef } from 'vue'
import { errorMessage } from '@/api/http'
import { useCacheWarmup } from './useCacheWarmup'

export interface AsyncResourceOptions<T, A extends unknown[]> {
    /** Value before the first successful load. */
    initial: T
    load: (...args: A) => Promise<T>
    /** Wait for the GitHub import to populate the local database first. Default false. */
    warmCache?: boolean
    /**
     * Load once from onMounted, with no arguments. Default true, so a loader that *requires* an
     * argument must pass false and drive the first load itself.
     */
    immediate?: boolean
}

export interface AsyncResource<T, A extends unknown[]> {
    /** Shallow: replaced wholesale on every load, never mutated in place. */
    data: ShallowRef<T>
    loading: Ref<boolean>
    /** Writable, so a view can clear it from `@dismiss`. */
    error: Ref<string>
    /** Never rejects: failures land in `error` and leave `data` alone. */
    reload: (...args: A) => Promise<void>
}

/**
 * One fetch, its in-flight flag and its error message.
 *
 * Deliberately narrow. It is for *data*, not for every in-flight boolean - a submit or a per-row
 * action stays a plain ref, because those carry concerns (per-row keys, follow-up refreshes) that
 * would turn this into a framework.
 *
 * On failure `data` is left as it was: blanking it would trip the `v-else-if="!loading"` empty
 * state, so the page would claim there is nothing to show *and* show an error at the same time.
 */
export function useAsyncResource<T, A extends unknown[] = []>(
    options: AsyncResourceOptions<T, A>,
): AsyncResource<T, A> {
    const data = shallowRef<T>(options.initial)
    const loading = ref(false)
    const error = ref('')

    const { waitForCache } = useCacheWarmup()

    // Responses can land out of order - switching a filter twice quickly used to let the first,
    // slower response overwrite the second. Only the newest request may write.
    let latest = 0

    async function run(args: A | []): Promise<void> {
        const token = ++latest
        loading.value = true
        error.value = ''
        try {
            if (options.warmCache) {
                await waitForCache()
            }
            const value = await options.load(...(args as A))
            if (token === latest) {
                data.value = value
            }
        } catch (e) {
            if (token === latest) {
                error.value = errorMessage(e)
            }
        } finally {
            if (token === latest) {
                loading.value = false
            }
        }
    }

    if (options.immediate !== false) {
        onMounted(() => run([]))
    }

    return { data, loading, error, reload: (...args: A) => run(args) }
}
