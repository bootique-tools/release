import type { RouteRecordRaw } from 'vue-router'
import { useReleaseSummary } from '@/composables/useReleaseSummary'
import type { NavMeta } from '@/domain/nav'

export type IssueKind = 'issue' | 'pr'

declare module 'vue-router' {
    interface RouteMeta {
        /** The page heading and the document title. PageShell reads it, so no view repeats it. */
        title?: string
        nav?: NavMeta
        kind?: IssueKind
    }
}

/**
 * Which release page to show depends on whether a descriptor is persisted - the same decision the
 * server used to make when it picked a template. On a failed probe the selection page is the safe
 * landing: it cannot act on a release that may not exist.
 */
async function hasRelease(): Promise<boolean> {
    const summary = await useReleaseSummary().refresh()
    return summary?.hasDescriptor === true
}

export const routes: RouteRecordRaw[] = [
    {
        path: '/',
        name: 'repositories',
        component: () => import('@/views/RepositoriesView.vue'),
        meta: { title: 'Repositories', nav: { label: 'Repos', order: 1, badge: 'repositories' } },
    },
    {
        path: '/issues',
        name: 'issues',
        component: () => import('@/views/IssueListView.vue'),
        meta: { title: 'Open issues', nav: { label: 'Issues', order: 2, badge: 'issues' }, kind: 'issue' },
    },
    {
        path: '/pull-requests',
        name: 'pullRequests',
        component: () => import('@/views/IssueListView.vue'),
        meta: {
            title: 'Open pull requests',
            nav: { label: 'PRs', order: 3, badge: 'pullRequests' },
            kind: 'pr',
        },
    },
    {
        path: '/milestones',
        name: 'milestones',
        component: () => import('@/views/MilestonesView.vue'),
        meta: { title: 'Milestones', nav: { order: 4 } },
    },
    {
        path: '/branches',
        name: 'branches',
        component: () => import('@/views/BranchesView.vue'),
        meta: { title: 'Branches', nav: { order: 5 } },
    },
    {
        path: '/maven',
        name: 'maven',
        component: () => import('@/views/MavenView.vue'),
        meta: { title: 'Maven projects', nav: { label: 'Maven', order: 6 } },
    },
    {
        // the nav points here and the selection page's guard forwards on, rather than the nav
        // guessing which of the two release pages is current
        path: '/release',
        redirect: { name: 'releaseSelect' },
        meta: { title: 'Release', nav: { order: 7 } },
    },
    {
        path: '/release/select-projects',
        name: 'releaseSelect',
        component: () => import('@/views/ReleaseSelectView.vue'),
        meta: { title: 'Start a release' },
        beforeEnter: async () => ((await hasRelease()) ? { name: 'releaseBoard' } : true),
    },
    {
        path: '/release/board',
        name: 'releaseBoard',
        component: () => import('@/views/ReleaseBoardView.vue'),
        meta: { title: 'Release' },
        beforeEnter: async () => ((await hasRelease()) ? true : { name: 'releaseSelect' }),
    },
    {
        path: '/release-notes',
        name: 'releaseNotes',
        component: () => import('@/views/ReleaseNotesView.vue'),
        meta: { title: 'Release notes', nav: { order: 8 } },
    },
    {
        path: '/tool-check',
        name: 'toolCheck',
        component: () => import('@/views/ToolCheckView.vue'),
        meta: { title: 'Tool check', nav: { order: 9 } },
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'notFound',
        component: () => import('@/views/NotFoundView.vue'),
        meta: { title: 'Page not found' },
    },
]
