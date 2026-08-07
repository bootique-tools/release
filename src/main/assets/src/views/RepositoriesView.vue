<script setup lang="ts">
import { ref } from 'vue'
import PageShell from '@/components/PageShell.vue'
import LoadingBar from '@/components/LoadingBar.vue'
import ErrorAlert from '@/components/ErrorAlert.vue'
import EmptyState from '@/components/EmptyState.vue'
import SortableTh from '@/components/SortableTh.vue'
import { useAgrestList } from '@/composables/useAgrestList'
import { useSession } from '@/composables/useSession'
import {
    cloneAll,
    cloneRepository,
    listRepositories,
    openRepository,
    selectBasePath,
    updateAll,
    updateRepository,
} from '@/api/repos'
import { ApiError, errorMessage } from '@/api/http'
import type { Repository } from '@/api/types'

const { items, total, loading, error, sort, direction, reload, toggleSort } =
    useAgrestList<Repository>({ fetch: listRepositories, syncQuery: true })
const { refresh: refreshSession } = useSession()

const actionError = ref('')
const pathNotSet = ref(false)
const busy = ref<string | null>(null)
const pickingPath = ref(false)

function handle(error: unknown) {
    if (error instanceof ApiError && error.isPathNotSet) {
        pathNotSet.value = true
    }
    actionError.value = errorMessage(error)
}

/** Runs a repository action, then refreshes the row data and the nav counts. */
async function run(key: string, action: () => Promise<void>) {
    busy.value = key
    actionError.value = ''
    pathNotSet.value = false
    try {
        await action()
        await reload()
        await refreshSession()
    } catch (e) {
        handle(e)
    } finally {
        busy.value = null
    }
}

async function chooseBasePath() {
    pickingPath.value = true
    actionError.value = ''
    try {
        const path = await selectBasePath()
        // an empty body means the dialog was dismissed - leave the page alone
        if (path.trim()) {
            pathNotSet.value = false
            await reload()
        }
    } catch (e) {
        handle(e)
    } finally {
        pickingPath.value = false
    }
}
</script>

<template>
    <PageShell title="Repositories" :subtitle="`${total} tracked`">
        <template #actions>
            <button type="button" class="btn btn-sm btn-outline-primary"
                    :disabled="pickingPath || busy !== null" @click="chooseBasePath">
                <span v-if="pickingPath" class="spinner-border spinner-border-sm me-1"
                      aria-hidden="true"></span>
                <i v-else class="bi bi-folder2-open me-1" aria-hidden="true"></i>
                Set local path
            </button>
            <button type="button" class="btn btn-sm btn-outline-secondary" :disabled="busy !== null"
                    @click="run('update-all', updateAll)">
                <i class="bi bi-arrow-repeat me-1" aria-hidden="true"></i>Update all
            </button>
            <button type="button" class="btn btn-sm btn-outline-secondary" :disabled="busy !== null"
                    @click="run('clone-all', cloneAll)">
                <i class="bi bi-files me-1" aria-hidden="true"></i>Clone all
            </button>
        </template>

        <div v-if="pickingPath" class="alert alert-info d-flex align-items-center gap-2">
            <span class="spinner-border spinner-border-sm" aria-hidden="true"></span>
            A folder picker has opened on the machine running the server.
        </div>

        <ErrorAlert :message="actionError"
                    :action-label="pathNotSet ? 'Set path' : undefined"
                    @action="chooseBasePath" @dismiss="actionError = ''"/>
        <ErrorAlert :message="error" @dismiss="error = ''"/>
        <LoadingBar :loading="loading"/>

        <div v-if="items.length" class="table-responsive">
            <table class="table table-sm table-hover align-middle">
                <thead>
                <tr>
                    <SortableTh field="name" :active-field="sort"
                                :direction="direction" @sort="toggleSort">
                        Repository
                    </SortableTh>
                    <SortableTh field="pullRequests" :active-field="sort"
                                :direction="direction" @sort="toggleSort">
                        PRs
                    </SortableTh>
                    <SortableTh field="issues" :active-field="sort"
                                :direction="direction" @sort="toggleSort">
                        Issues
                    </SortableTh>
                    <SortableTh field="pushedAt" :active-field="sort"
                                :direction="direction" @sort="toggleSort">
                        Last push
                    </SortableTh>
                    <th scope="col">Milestones</th>
                    <th scope="col" class="text-end">Local copy</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="repo in items" :key="repo.id">
                    <td>
                        <a :href="repo.url" target="_blank" rel="noopener">{{ repo.name }}</a>
                        <a v-if="repo.parent" :href="repo.parent.url" target="_blank" rel="noopener"
                           class="badge text-bg-secondary ms-1 text-decoration-none">parent</a>
                    </td>
                    <td>
                        <RouterLink :to="{ path: '/pr', query: { filter: 'repository.name', field: repo.name } }">
                            {{ repo.pullRequests?.length ?? 0 }}
                        </RouterLink>
                    </td>
                    <td>
                        <RouterLink :to="{ path: '/issue', query: { filter: 'repository.name', field: repo.name } }">
                            {{ repo.issues?.length ?? 0 }}
                        </RouterLink>
                    </td>
                    <td class="text-nowrap">{{ repo.pushedAtStr }}</td>
                    <td>
                        <RouterLink to="/milestone" class="badge text-bg-secondary text-decoration-none">
                            {{ repo.milestones?.length ?? 0 }}
                        </RouterLink>
                    </td>
                    <td class="text-end text-nowrap">
                        <!-- MISSING means there is no local clone yet, so only cloning is offered -->
                        <div v-if="repo.localStatus === 'MISSING'" class="btn-group btn-group-sm">
                            <button type="button" class="btn btn-outline-primary"
                                    :disabled="busy !== null"
                                    @click="run(repo.name, () => cloneRepository(repo.name))">
                                <span v-if="busy === repo.name"
                                      class="spinner-border spinner-border-sm me-1" aria-hidden="true"></span>
                                <i v-else class="bi bi-files me-1" aria-hidden="true"></i>Clone
                            </button>
                        </div>
                        <div v-else class="btn-group btn-group-sm">
                            <button type="button" class="btn btn-outline-secondary"
                                    :disabled="busy !== null" :aria-label="`Open ${repo.name} in a file manager`"
                                    title="Open in file manager"
                                    @click="run(repo.name, () => openRepository(repo.name, 'files'))">
                                <i class="bi bi-folder2-open" aria-hidden="true"></i>
                            </button>
                            <button type="button" class="btn btn-outline-secondary"
                                    :disabled="busy !== null" :aria-label="`Open ${repo.name} in a terminal`"
                                    title="Open in terminal"
                                    @click="run(repo.name, () => openRepository(repo.name, 'terminal'))">
                                <i class="bi bi-terminal" aria-hidden="true"></i>
                            </button>
                            <button type="button" class="btn btn-outline-secondary"
                                    :disabled="busy !== null" :aria-label="`Pull ${repo.name}`"
                                    title="Pull latest"
                                    @click="run(repo.name, () => updateRepository(repo.name))">
                                <span v-if="busy === repo.name"
                                      class="spinner-border spinner-border-sm" aria-hidden="true"></span>
                                <i v-else class="bi bi-arrow-repeat" aria-hidden="true"></i>
                            </button>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>

        <EmptyState v-else-if="!loading"
                    message="No repositories yet. The GitHub import runs on a schedule after startup."
                    icon="bi-git"/>
    </PageShell>
</template>
