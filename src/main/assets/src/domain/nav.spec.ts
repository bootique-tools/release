import { describe, expect, it } from 'vitest'
import { isNavActive, navLinks, type NavRouteLike } from './nav'

const COUNTS = { repositories: 12, issues: 34, pullRequests: 5 }

describe('navLinks', () => {
    it('includes only routes that ask to be in the nav', () => {
        const routes: NavRouteLike[] = [
            { path: '/', meta: { title: 'Repositories', nav: { order: 1 } } },
            { path: '/release/board', meta: { title: 'Release' } },
        ]
        expect(navLinks(routes).map((l) => l.to)).toEqual(['/'])
    })

    // getRoutes() is sorted by matcher score, not by declaration order, so `order` is load-bearing
    it('sorts by the declared order rather than the input order', () => {
        const routes: NavRouteLike[] = [
            { path: '/b', meta: { title: 'B', nav: { order: 2 } } },
            { path: '/a', meta: { title: 'A', nav: { order: 1 } } },
        ]
        expect(navLinks(routes).map((l) => l.label)).toEqual(['A', 'B'])
    })

    it('falls back to the route title, so the two cannot drift', () => {
        const routes: NavRouteLike[] = [
            { path: '/', meta: { title: 'Repositories', nav: { order: 1, label: 'Repos' } } },
            { path: '/branches', meta: { title: 'Branches', nav: { order: 2 } } },
        ]
        expect(navLinks(routes).map((l) => l.label)).toEqual(['Repos', 'Branches'])
    })

    it('resolves a badge against the session counts', () => {
        const routes: NavRouteLike[] = [
            { path: '/issues', meta: { title: 'Issues', nav: { order: 1, badge: 'issues' } } },
            { path: '/branches', meta: { title: 'Branches', nav: { order: 2 } } },
        ]
        const links = navLinks(routes, COUNTS)
        expect(links[0]!.badge).toBe(34)
        expect(links[1]!.badge).toBeUndefined()
    })

    it('omits the badge entirely until the session has loaded', () => {
        const routes: NavRouteLike[] = [
            { path: '/issues', meta: { title: 'Issues', nav: { order: 1, badge: 'issues' } } },
        ]
        expect(navLinks(routes)[0]!.badge).toBeUndefined()
    })

    it('shows a zero count rather than hiding the badge', () => {
        const routes: NavRouteLike[] = [
            { path: '/pull-requests', meta: { title: 'PRs', nav: { order: 1, badge: 'pullRequests' } } },
        ]
        expect(navLinks(routes, { ...COUNTS, pullRequests: 0 })[0]!.badge).toBe(0)
    })
})

describe('isNavActive', () => {
    it('matches the route itself and anything below it', () => {
        expect(isNavActive('/release', '/release')).toBe(true)
        expect(isNavActive('/release', '/release/board')).toBe(true)
    })

    // the whole reason this is segment-aware rather than a prefix test
    it('does not let /release-notes activate /release', () => {
        expect(isNavActive('/release', '/release-notes')).toBe(false)
    })

    it('treats the root as exact, so it does not activate on every page', () => {
        expect(isNavActive('/', '/')).toBe(true)
        expect(isNavActive('/', '/branches')).toBe(false)
        expect(isNavActive('/', '/issues')).toBe(false)
    })
})
