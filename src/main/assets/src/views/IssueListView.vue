<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import PageShell from '@/components/PageShell.vue'
import LoadingBar from '@/components/LoadingBar.vue'
import ErrorAlert from '@/components/ErrorAlert.vue'
import EmptyState from '@/components/EmptyState.vue'
import SortableTh from '@/components/SortableTh.vue'
import { useAgrestList } from '@/composables/useAgrestList'
import { listIssues, listPullRequests } from '@/api/issues'
import type { OpenIssue } from '@/api/types'

const route = useRoute()
// one component serves both /issue and /pr; they differ only by source and the milestone column
const isIssues = computed(() => route.meta.kind !== 'pr')

const { items, total, loading, error, sort, direction, filterField, filterValue,
    reload, toggleSort, applyFilter, clearFilter } = useAgrestList<OpenIssue>({
    fetch: (query) => (route.meta.kind === 'pr' ? listPullRequests(query) : listIssues(query)),
    syncQuery: true,
})

const title = computed(() => (isIssues.value ? 'Open issues' : 'Open pull requests'))
</script>

<template>
    <PageShell :title="title" :subtitle="`${total} total`">
        <template #actions>
            <button v-if="filterField" type="button"
                    class="btn btn-sm btn-outline-secondary" @click="clearFilter()">
                <i class="bi bi-x-lg me-1" aria-hidden="true"></i>
                Clear filter: {{ filterValue }}
            </button>
            <button type="button" class="btn btn-sm btn-outline-secondary"
                    :disabled="loading" @click="reload()">
                <i class="bi bi-arrow-repeat me-1" aria-hidden="true"></i>Refresh
            </button>
        </template>

        <ErrorAlert :message="error" @dismiss="error = ''"/>
        <LoadingBar :loading="loading"/>

        <div v-if="items.length" class="table-responsive">
            <table class="table table-sm table-hover table-striped align-middle">
                <thead>
                <tr>
                    <SortableTh field="title" :active-field="sort"
                                :direction="direction" @sort="toggleSort">
                        Title
                    </SortableTh>
                    <SortableTh field="author.login" :active-field="sort"
                                :direction="direction" @sort="toggleSort">
                        Author
                    </SortableTh>
                    <SortableTh field="createdAt" :active-field="sort"
                                :direction="direction" @sort="toggleSort">
                        Created
                    </SortableTh>
                    <SortableTh v-if="isIssues" field="milestone.title" :active-field="sort"
                                :direction="direction" @sort="toggleSort">
                        Milestone
                    </SortableTh>
                    <SortableTh field="labels" :active-field="sort"
                                :direction="direction" @sort="toggleSort">
                        Labels
                    </SortableTh>
                    <SortableTh field="repository.name" :active-field="sort"
                                :direction="direction" @sort="toggleSort">
                        Repository
                    </SortableTh>
                </tr>
                </thead>
                <tbody>
                <tr v-for="item in items" :key="item.id">
                    <td>
                        <a :href="item.url" target="_blank" rel="noopener">{{ item.title }}</a>
                        <a :href="item.url" target="_blank" rel="noopener"
                           class="badge text-bg-secondary ms-1 text-decoration-none">
                            #{{ item.number }}
                        </a>
                    </td>
                    <td>
                        <button v-if="item.author" type="button" class="btn btn-link btn-sm p-0"
                                @click="applyFilter('author.login', item.author.login)">
                            {{ item.author.login }}
                        </button>
                    </td>
                    <td class="text-nowrap">{{ item.createdAtStr }}</td>
                    <td v-if="isIssues">
                        <button v-if="item.milestone" type="button"
                                class="badge text-bg-secondary border-0"
                                @click="applyFilter('milestone.title', item.milestone.title)">
                            {{ item.milestone.title }}
                        </button>
                    </td>
                    <td>
                        <button v-for="label in item.labels" :key="label.id" type="button"
                                class="badge border-0 me-1"
                                :style="{ backgroundColor: `#${label.color}`, color: '#fff' }"
                                @click="applyFilter('labels.name', label.name)">
                            {{ label.name }}
                        </button>
                    </td>
                    <td class="text-nowrap">
                        <button type="button" class="btn btn-link btn-sm p-0"
                                @click="applyFilter('repoName', item.repoName)">
                            {{ item.repoName }}
                        </button>
                        <a v-if="item.repository?.parent" :href="item.repository.parent.url"
                           target="_blank" rel="noopener"
                           class="badge text-bg-secondary ms-1 text-decoration-none">parent</a>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>

        <EmptyState v-else-if="!loading"
                    :message="isIssues ? 'No open issues.' : 'No open pull requests.'"
                    icon="bi-check2-circle"/>
    </PageShell>
</template>
