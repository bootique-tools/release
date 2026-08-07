<script setup lang="ts">
import { ref } from 'vue'
import PageShell from '@/components/PageShell.vue'
import LoadingBar from '@/components/LoadingBar.vue'
import AppAlert from '@/components/AppAlert.vue'
import EmptyState from '@/components/EmptyState.vue'
import SortableTh from '@/components/SortableTh.vue'
import TableFrame from '@/components/TableFrame.vue'
import BusyButton from '@/components/BusyButton.vue'
import ChipLink from '@/components/ChipLink.vue'
import ExternalLink from '@/components/ExternalLink.vue'
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

const { items, total, loading, error, sort, direction, reload, toggleSort } = useAgrestList<Repository>({
    fetch: listRepositories,
    syncQuery: true,
})
const { refresh: refreshSession } = useSession()

const actionError = ref('')
const pathNotSet = ref(false)
const busy = ref<string | null>(null)
const pickingPath = ref(false)

/** One banner, two sources: the listing and the row actions have separate lifecycles. */
function dismissErrors() {
    actionError.value = ''
    error.value = ''
}

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
            <BusyButton
                variant="primary"
                icon="bi-folder2-open"
                :busy="pickingPath"
                :disabled="busy !== null"
                @click="chooseBasePath"
            >
                Set local path
            </BusyButton>
            <BusyButton
                icon="bi-arrow-repeat"
                :busy="busy === 'update-all'"
                :disabled="busy !== null"
                @click="run('update-all', updateAll)"
            >
                Update all
            </BusyButton>
            <BusyButton
                icon="bi-files"
                :busy="busy === 'clone-all'"
                :disabled="busy !== null"
                @click="run('clone-all', cloneAll)"
            >
                Clone all
            </BusyButton>
        </template>

        <AppAlert
            v-if="pickingPath"
            tone="info"
            busy
            :dismissible="false"
            message="A folder picker has opened on the machine running the server."
        />

        <!-- list and action errors have separate lifecycles, but only ever one banner -->
        <AppAlert
            :message="actionError || error"
            :action-label="pathNotSet ? 'Set path' : undefined"
            @action="chooseBasePath"
            @dismiss="dismissErrors"
        />
        <LoadingBar :loading="loading" />

        <TableFrame v-if="items.length">
            <thead>
                <tr>
                    <SortableTh field="name" :active-field="sort" :direction="direction" @sort="toggleSort">
                        Repository
                    </SortableTh>
                    <SortableTh
                        field="pullRequests"
                        :active-field="sort"
                        :direction="direction"
                        @sort="toggleSort"
                    >
                        PRs
                    </SortableTh>
                    <SortableTh field="issues" :active-field="sort" :direction="direction" @sort="toggleSort">
                        Issues
                    </SortableTh>
                    <SortableTh
                        field="pushedAt"
                        :active-field="sort"
                        :direction="direction"
                        @sort="toggleSort"
                    >
                        Last push
                    </SortableTh>
                    <th scope="col">Milestones</th>
                    <th scope="col" class="text-end">Local copy</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="repo in items" :key="repo.id">
                    <td>
                        <ExternalLink :href="repo.url">{{ repo.name }}</ExternalLink>
                        <ChipLink v-if="repo.parent" :href="repo.parent.url" class="ms-1"> parent </ChipLink>
                    </td>
                    <td>
                        <RouterLink
                            :to="{ path: '/pr', query: { filter: 'repository.name', field: repo.name } }"
                        >
                            {{ repo.pullRequests?.length ?? 0 }}
                        </RouterLink>
                    </td>
                    <td>
                        <RouterLink
                            :to="{
                                path: '/issue',
                                query: { filter: 'repository.name', field: repo.name },
                            }"
                        >
                            {{ repo.issues?.length ?? 0 }}
                        </RouterLink>
                    </td>
                    <td class="text-nowrap">{{ repo.pushedAtStr }}</td>
                    <td>
                        <ChipLink to="/milestone">{{ repo.milestones?.length ?? 0 }}</ChipLink>
                    </td>
                    <td class="text-end text-nowrap">
                        <!-- MISSING means there is no local clone yet, so only cloning is offered -->
                        <div v-if="repo.localStatus === 'MISSING'" class="btn-group btn-group-sm">
                            <BusyButton
                                variant="primary"
                                icon="bi-files"
                                :busy="busy === repo.name"
                                :disabled="busy !== null"
                                @click="run(repo.name, () => cloneRepository(repo.name))"
                            >
                                Clone
                            </BusyButton>
                        </div>
                        <div v-else class="btn-group btn-group-sm">
                            <BusyButton
                                icon="bi-folder2-open"
                                :label="`Open ${repo.name} in a file manager`"
                                :disabled="busy !== null"
                                @click="run(repo.name, () => openRepository(repo.name, 'files'))"
                            />
                            <BusyButton
                                icon="bi-terminal"
                                :label="`Open ${repo.name} in a terminal`"
                                :disabled="busy !== null"
                                @click="run(repo.name, () => openRepository(repo.name, 'terminal'))"
                            />
                            <BusyButton
                                icon="bi-arrow-repeat"
                                :label="`Pull ${repo.name}`"
                                :busy="busy === repo.name"
                                :disabled="busy !== null"
                                @click="run(repo.name, () => updateRepository(repo.name))"
                            />
                        </div>
                    </td>
                </tr>
            </tbody>
        </TableFrame>

        <EmptyState
            v-else-if="!loading"
            message="No repositories yet. The GitHub import runs on a schedule after startup."
            icon="bi-git"
        />
    </PageShell>
</template>
