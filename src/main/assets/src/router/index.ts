import { createRouter, createWebHistory } from 'vue-router'
import { routes } from './routes'

export type { IssueKind } from './routes'

export const router = createRouter({
    history: createWebHistory('/ui/'),
    routes,
})

router.afterEach((to) => {
    document.title = to.meta.title ? `${to.meta.title} · Release Manager` : 'Release Manager'
})
