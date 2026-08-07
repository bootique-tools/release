import type { Params } from './http'

export interface DataResponse<T> {
    data: T[]
    total?: number
}

export type SortDirection = 'ASC' | 'DESC'

export interface AgrestQuery {
    /** Related entities to fetch, e.g. ['repository', 'labels']. */
    include?: readonly string[]
    sort?: string
    direction?: SortDirection
    filter?: { field: string; value: string }
    limit?: number
    start?: number
}

/**
 * Builds Agrest client parameters. Values are serialized with JSON.stringify and encoded by
 * URLSearchParams at the call site, so a filter containing a quote or an ampersand stays intact.
 */
export function agrestParams(query: AgrestQuery): Params {
    const params: Params = {}

    if (query.include?.length) {
        params.include = JSON.stringify(query.include)
    }
    if (query.sort) {
        params.sort = query.sort
        params.direction = query.direction ?? 'DESC'
    }
    if (query.filter?.value) {
        params.cayenneExp = JSON.stringify([`${query.filter.field} like $b`, query.filter.value])
    }
    if (query.limit != null) {
        params.limit = query.limit
    }
    if (query.start != null) {
        params.start = query.start
    }

    return params
}
