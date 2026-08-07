import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { fetchReleaseSummary } from '@/api/releaseSelect'

export type IssueKind = 'issue' | 'pr'

declare module 'vue-router' {
    interface RouteMeta {
        title?: string
        kind?: IssueKind
    }
}

/**
 * Which release page to show depends on whether a descriptor is persisted - the same decision the
 * server used to make when it picked a template. On a failed probe the selection page is the safe
 * landing: it cannot act on a release that may not exist.
 */
async function hasRelease(): Promise<boolean> {
    const summary = await fetchReleaseSummary().catch(() => null)
    return summary?.hasDescriptor === true
}

const routes: RouteRecordRaw[] = [
    {
        path: '/',
        name: 'repos',
        component: () => import('@/views/RepositoriesView.vue'),
        meta: { title: 'Repositories' },
    },
    {
        path: '/issue',
        name: 'issues',
        component: () => import('@/views/IssueListView.vue'),
        meta: { title: 'Issues', kind: 'issue' },
    },
    {
        path: '/pr',
        name: 'pullRequests',
        component: () => import('@/views/IssueListView.vue'),
        meta: { title: 'Pull requests', kind: 'pr' },
    },
    {
        path: '/milestone',
        name: 'milestones',
        component: () => import('@/views/MilestonesView.vue'),
        meta: { title: 'Milestones' },
    },
    {
        path: '/branches',
        name: 'branches',
        component: () => import('@/views/BranchesView.vue'),
        meta: { title: 'Branches' },
    },
    {
        path: '/maven',
        name: 'maven',
        component: () => import('@/views/MavenView.vue'),
        meta: { title: 'Maven projects' },
    },
    {
        // the selection page's own guard forwards to the board when a release is already in flight
        path: '/release',
        redirect: '/release/select-projects',
    },
    {
        path: '/release/select-projects',
        name: 'releaseSelect',
        component: () => import('@/views/ReleaseSelectView.vue'),
        meta: { title: 'Start a release' },
        beforeEnter: async () => ((await hasRelease()) ? '/release/board' : true),
    },
    {
        path: '/release/board',
        name: 'releaseBoard',
        component: () => import('@/views/ReleaseBoardView.vue'),
        meta: { title: 'Release' },
        beforeEnter: async () => ((await hasRelease()) ? true : '/release/select-projects'),
    },
    {
        path: '/release-notes',
        name: 'releaseNotes',
        component: () => import('@/views/ReleaseNotesView.vue'),
        meta: { title: 'Release notes' },
    },
    {
        path: '/tool-check',
        name: 'toolCheck',
        component: () => import('@/views/ToolCheckView.vue'),
        meta: { title: 'Tool check' },
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'notFound',
        component: () => import('@/views/NotFoundView.vue'),
        meta: { title: 'Not found' },
    },
]

export const router = createRouter({
    history: createWebHistory('/ui/'),
    routes,
})

router.afterEach((to) => {
    document.title = to.meta.title ? `${to.meta.title} · Release Manager` : 'Release Manager'
})
