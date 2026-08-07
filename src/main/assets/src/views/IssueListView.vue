<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import PageShell from '@/components/PageShell.vue'
import LoadingBar from '@/components/LoadingBar.vue'
import AppAlert from '@/components/AppAlert.vue'
import EmptyState from '@/components/EmptyState.vue'
import SortableTh from '@/components/SortableTh.vue'
import TableFrame from '@/components/TableFrame.vue'
import BusyButton from '@/components/BusyButton.vue'
import ChipLink from '@/components/ChipLink.vue'
import ExternalLink from '@/components/ExternalLink.vue'
import { labelChipVars } from '@/domain/labelColor'
import { useAgrestList } from '@/composables/useAgrestList'
import { listIssues, listPullRequests } from '@/api/issues'
import type { OpenIssue } from '@/api/types'

const route = useRoute()
// one component serves both /issue and /pr; they differ only by source and the milestone column
const isIssues = computed(() => route.meta.kind !== 'pr')

const {
    items,
    total,
    loading,
    error,
    sort,
    direction,
    filterField,
    filterValue,
    reload,
    toggleSort,
    applyFilter,
    clearFilter,
} = useAgrestList<OpenIssue>({
    fetch: (query) => (route.meta.kind === 'pr' ? listPullRequests(query) : listIssues(query)),
    syncQuery: true,
})

const title = computed(() => (isIssues.value ? 'Open issues' : 'Open pull requests'))
</script>

<template>
    <PageShell :title="title" :subtitle="`${total} total`">
        <template #actions>
            <BusyButton v-if="filterField" icon="bi-x-lg" @click="clearFilter()">
                Clear filter: {{ filterValue }}
            </BusyButton>
            <BusyButton icon="bi-arrow-repeat" :busy="loading" @click="reload()">Refresh</BusyButton>
        </template>

        <AppAlert :message="error" @dismiss="error = ''" />
        <LoadingBar :loading="loading" />

        <TableFrame v-if="items.length">
            <thead>
                <tr>
                    <SortableTh field="title" :active-field="sort" :direction="direction" @sort="toggleSort">
                        Title
                    </SortableTh>
                    <SortableTh
                        field="author.login"
                        :active-field="sort"
                        :direction="direction"
                        @sort="toggleSort"
                    >
                        Author
                    </SortableTh>
                    <SortableTh
                        field="createdAt"
                        :active-field="sort"
                        :direction="direction"
                        @sort="toggleSort"
                    >
                        Created
                    </SortableTh>
                    <SortableTh
                        v-if="isIssues"
                        field="milestone.title"
                        :active-field="sort"
                        :direction="direction"
                        @sort="toggleSort"
                    >
                        Milestone
                    </SortableTh>
                    <SortableTh field="labels" :active-field="sort" :direction="direction" @sort="toggleSort">
                        Labels
                    </SortableTh>
                    <SortableTh
                        field="repository.name"
                        :active-field="sort"
                        :direction="direction"
                        @sort="toggleSort"
                    >
                        Repository
                    </SortableTh>
                </tr>
            </thead>
            <tbody>
                <tr v-for="item in items" :key="item.id">
                    <td>
                        <ExternalLink :href="item.url">{{ item.title }}</ExternalLink>
                        <ChipLink :href="item.url" class="ms-1">#{{ item.number }}</ChipLink>
                    </td>
                    <td>
                        <button
                            v-if="item.author"
                            type="button"
                            class="btn btn-link btn-sm p-0"
                            @click="applyFilter('author.login', item.author.login)"
                        >
                            {{ item.author.login }}
                        </button>
                    </td>
                    <td class="text-nowrap">{{ item.createdAtStr }}</td>
                    <td v-if="isIssues">
                        <button
                            v-if="item.milestone"
                            type="button"
                            class="badge chip-secondary border-0"
                            @click="applyFilter('milestone.title', item.milestone.title)"
                        >
                            {{ item.milestone.title }}
                        </button>
                    </td>
                    <td>
                        <button
                            v-for="label in item.labels"
                            :key="label.id"
                            type="button"
                            class="badge chip-label border-0 me-1"
                            :style="labelChipVars(label.color) ?? undefined"
                            @click="applyFilter('labels.name', label.name)"
                        >
                            {{ label.name }}
                        </button>
                    </td>
                    <td class="text-nowrap">
                        <button
                            type="button"
                            class="btn btn-link btn-sm p-0"
                            @click="applyFilter('repoName', item.repoName)"
                        >
                            {{ item.repoName }}
                        </button>
                        <ChipLink
                            v-if="item.repository?.parent"
                            :href="item.repository.parent.url"
                            class="ms-1"
                        >
                            parent
                        </ChipLink>
                    </td>
                </tr>
            </tbody>
        </TableFrame>

        <EmptyState
            v-else-if="!loading"
            :message="isIssues ? 'No open issues.' : 'No open pull requests.'"
            icon="bi-check2-circle"
        />
    </PageShell>
</template>
