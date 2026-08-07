<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import ThemeToggle from './ThemeToggle.vue'
import { useSession } from '@/composables/useSession'
import { useBackgroundJob } from '@/composables/useBackgroundJob'

const emit = defineEmits<{ validatePom: [] }>()

const { session } = useSession()
const { active: backgroundJob } = useBackgroundJob()
const route = useRoute()
const expanded = ref(false)

const counts = computed(() => session.value?.counts)

const links = computed(() => [
    { to: '/', label: 'Repos', badge: counts.value?.repositories, exact: true },
    { to: '/issue', label: 'Issues', badge: counts.value?.issues },
    { to: '/pr', label: 'PRs', badge: counts.value?.pullRequests },
    { to: '/milestone', label: 'Milestones' },
    { to: '/branches', label: 'Branches' },
    { to: '/maven', label: 'Maven projects' },
    { to: '/release', label: 'Release' },
    { to: '/release-notes', label: 'Release notes' },
    { to: '/tool-check', label: 'Tool check' },
])

function isActive(to: string, exact?: boolean): boolean {
    // segment-aware, otherwise /release-notes would also light up the /release entry
    return exact ? route.path === to : route.path === to || route.path.startsWith(`${to}/`)
}
</script>

<template>
    <nav class="navbar navbar-expand-lg bg-body-tertiary border-bottom sticky-top">
        <div class="container-xxl">
            <a
                v-if="session?.organization"
                class="navbar-brand"
                :href="session.organization.url"
                target="_blank"
                rel="noopener"
            >
                {{ session.organization.name }}
            </a>
            <span v-else class="navbar-text text-warning-emphasis small">
                No data from GitHub API, wrong token? Try to refresh the page or check your token.
            </span>

            <button
                class="navbar-toggler"
                type="button"
                aria-label="Toggle navigation"
                :aria-expanded="expanded"
                @click="expanded = !expanded"
            >
                <span class="navbar-toggler-icon"></span>
            </button>

            <div class="collapse navbar-collapse" :class="{ show: expanded }">
                <ul v-if="session?.organization" class="navbar-nav me-auto" @click="expanded = false">
                    <li v-for="link in links" :key="link.to" class="nav-item">
                        <RouterLink
                            class="nav-link"
                            :to="link.to"
                            :class="{ active: isActive(link.to, link.exact) }"
                        >
                            {{ link.label }}
                            <span v-if="link.badge != null" class="badge chip-secondary">
                                {{ link.badge }}
                            </span>
                        </RouterLink>
                    </li>
                    <li class="nav-item">
                        <button
                            type="button"
                            class="nav-link border-0 bg-transparent"
                            @click="emit('validatePom')"
                        >
                            Validate pom
                        </button>
                    </li>
                </ul>

                <div class="d-flex align-items-center gap-2 ms-auto">
                    <span
                        v-if="backgroundJob"
                        class="navbar-text small text-body-secondary"
                        :title="`A ${backgroundJob} job is running`"
                    >
                        <span class="spinner-border spinner-border-sm me-1" aria-hidden="true"></span>
                        {{ backgroundJob }}
                    </span>
                    <a
                        v-if="session?.user"
                        class="navbar-text link-body-emphasis"
                        :href="session.user.url"
                        target="_blank"
                        rel="noopener"
                        :title="`Current user: ${session.user.login} (${session.user.name ?? ''})`"
                    >
                        {{ session.user.login }}
                    </a>
                    <ThemeToggle />
                </div>
            </div>
        </div>
    </nav>
</template>
