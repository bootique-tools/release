import { describe, expect, it } from 'vitest'
import { useAsyncResource } from './useAsyncResource'
import { ApiError } from '@/api/http'

/** A promise plus the handles to settle it later, so a test can control response ordering. */
function deferred<T>() {
    let resolve!: (value: T) => void
    let reject!: (reason: unknown) => void
    const promise = new Promise<T>((res, rej) => {
        resolve = res
        reject = rej
    })
    return { promise, resolve, reject }
}

// immediate: false throughout - the onMounted hook is framework glue, not a contract, and there is
// no component instance here to run it.
const options = { immediate: false as const }

describe('useAsyncResource', () => {
    it('exposes the initial value until the first load resolves', async () => {
        const resource = useAsyncResource({ ...options, initial: [1], load: async () => [2] })

        expect(resource.data.value).toEqual([1])
        await resource.reload()
        expect(resource.data.value).toEqual([2])
    })

    it('clears loading on success and on failure', async () => {
        const ok = useAsyncResource({ ...options, initial: 0, load: async () => 1 })
        await ok.reload()
        expect(ok.loading.value).toBe(false)

        const bad = useAsyncResource({
            ...options,
            initial: 0,
            load: async () => {
                throw new Error('nope')
            },
        })
        await bad.reload()
        expect(bad.loading.value).toBe(false)
    })

    it('leaves the last good data in place when a load fails', async () => {
        let fail = false
        const resource = useAsyncResource({
            ...options,
            initial: [] as string[],
            load: async () => {
                if (fail) {
                    throw new Error('boom')
                }
                return ['a']
            },
        })

        await resource.reload()
        fail = true
        await resource.reload()

        // blanking it would trip the empty state, so the page would say "nothing here" AND show
        // an error at the same time
        expect(resource.data.value).toEqual(['a'])
        expect(resource.error.value).toBe('boom')
    })

    it('reports a server error through errorMessage', async () => {
        const resource = useAsyncResource({
            ...options,
            initial: null,
            load: async () => {
                throw new ApiError(500, 'the repository is locked')
            },
        })

        await resource.reload()
        expect(resource.error.value).toBe('the repository is locked')
    })

    it('clears a previous error when a later load succeeds', async () => {
        let fail = true
        const resource = useAsyncResource({
            ...options,
            initial: 0,
            load: async () => {
                if (fail) {
                    throw new Error('boom')
                }
                return 1
            },
        })

        await resource.reload()
        expect(resource.error.value).toBe('boom')

        fail = false
        await resource.reload()
        expect(resource.error.value).toBe('')
    })

    it('passes arguments through to the loader', async () => {
        const seen: string[] = []
        const resource = useAsyncResource<string, [string]>({
            ...options,
            initial: '',
            load: async (version: string) => {
                seen.push(version)
                return version
            },
        })

        await resource.reload('4.0')
        expect(seen).toEqual(['4.0'])
        expect(resource.data.value).toBe('4.0')
    })

    it('ignores a slow first response that lands after a newer one', async () => {
        const first = deferred<string>()
        const second = deferred<string>()
        const pending = [first, second]

        const resource = useAsyncResource({
            ...options,
            initial: '',
            load: () => pending.shift()!.promise,
        })

        const a = resource.reload()
        const b = resource.reload()

        second.resolve('second')
        await b
        first.resolve('first')
        await a

        expect(resource.data.value).toBe('second')
        expect(resource.loading.value).toBe(false)
    })

    it('ignores a stale failure, so a superseded request cannot show an error', async () => {
        const first = deferred<string>()
        const second = deferred<string>()
        const pending = [first, second]

        const resource = useAsyncResource({
            ...options,
            initial: '',
            load: () => pending.shift()!.promise,
        })

        const a = resource.reload()
        const b = resource.reload()

        second.resolve('second')
        await b
        first.reject(new Error('stale failure'))
        await a

        expect(resource.data.value).toBe('second')
        expect(resource.error.value).toBe('')
    })
})
