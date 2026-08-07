import type { SessionCounts } from '@/api/types'

/** Where a route appears in the main nav. A route without this is reachable but not linked. */
export interface NavMeta {
    /** Defaults to the route's title; set only where the nav wants a shorter word. */
    label?: string
    order: number
    badge?: keyof SessionCounts
}

/** The shape this needs from a vue-router record, so the logic stays free of vue-router. */
export interface NavRouteLike {
    path: string
    meta: { title?: string; nav?: NavMeta }
}

export interface NavLink {
    to: string
    label: string
    badge?: number
}

/**
 * The main nav, derived from the route table so the two cannot drift - they had already, with the
 * nav saying "Repos" where the route title said "Repositories".
 *
 * Ordering is explicit because vue-router's getRoutes() returns matcher-score order, not
 * declaration order.
 */
export function navLinks(routes: NavRouteLike[], counts?: SessionCounts): NavLink[] {
    return routes
        .filter((route) => route.meta.nav !== undefined)
        .sort((a, b) => a.meta.nav!.order - b.meta.nav!.order)
        .map((route) => {
            const nav = route.meta.nav!
            const badge = nav.badge ? counts?.[nav.badge] : undefined
            return {
                to: route.path,
                label: nav.label ?? route.meta.title ?? route.path,
                ...(badge === undefined ? {} : { badge }),
            }
        })
}

/**
 * Segment-aware, so /release-notes does not light up the /release entry. `/` is naturally exact,
 * since no other path starts with `//`.
 */
export function isNavActive(to: string, currentPath: string): boolean {
    return currentPath === to || currentPath.startsWith(`${to}/`)
}
