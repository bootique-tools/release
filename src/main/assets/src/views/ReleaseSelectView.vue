<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import PageShell from '@/components/PageShell.vue'
import LoadingBar from '@/components/LoadingBar.vue'
import ErrorAlert from '@/components/ErrorAlert.vue'
import EmptyState from '@/components/EmptyState.vue'
import AppModal from '@/components/AppModal.vue'
import { useCacheWarmup } from '@/composables/useCacheWarmup'
import { nextDevVersionOf, releaseVersionOf } from '@/domain/version'
import {
    createReleaseDescriptor,
    listProjectsForVersion,
    listReleaseProjects,
    toggleProjectSelection,
} from '@/api/releaseSelect'
import { errorMessage } from '@/api/http'
import type { Project } from '@/api/types'

const router = useRouter()
const { waitForCache } = useCacheWarmup()

// server-ordered: this array is in Maven dependency order and must never be re-sorted here
const modules = ref<Project[]>([])
const versions = ref<string[]>([])
const currentVersion = ref('')
const releaseVersion = ref('')
const nextDevVersion = ref('')
const selected = ref<string[]>([])

const loading = ref(false)
const submitting = ref(false)
const error = ref('')
const confirmOpen = ref(false)

const canStart = computed(
    () => selected.value.length > 0 && releaseVersion.value !== '' && nextDevVersion.value !== '',
)

watch(releaseVersion, (value) => {
    nextDevVersion.value = nextDevVersionOf(value)
})

async function loadVersions() {
    loading.value = true
    error.value = ''
    try {
        await waitForCache()
        const response = await listReleaseProjects()
        // `disable` is not persisted, so this listing comes back selectable; nothing is releasable
        // until a version is chosen, so mark every row disabled as the original page did
        modules.value = (response.data ?? []).map((m) => ({ ...m, disable: true }))
        // distinct in first-seen order, matching the order the modules are released in
        versions.value = [...new Set(modules.value.map((m) => m.version))]
    } catch (e) {
        error.value = errorMessage(e)
    } finally {
        loading.value = false
    }
}

async function selectVersion() {
    if (!currentVersion.value) {
        return
    }
    loading.value = true
    error.value = ''
    releaseVersion.value = releaseVersionOf(currentVersion.value)
    try {
        const response = await listProjectsForVersion(currentVersion.value)
        modules.value = response.data ?? []
        // the server marks releasable modules with disable=false; those start out checked
        selected.value = modules.value.filter((m) => !m.disable).map((m) => m.repository.name)
        if (!modules.value.length) {
            error.value = 'Please clone all repositories to your local repositories first.'
        }
    } catch (e) {
        error.value = errorMessage(e)
    } finally {
        loading.value = false
    }
}

/**
 * Toggling a module round-trips to the server, which resolves the transitive dependency closure and
 * returns the resulting set in release order.
 *
 * The server expects the selection as it will be AFTER the toggle, plus whether the clicked module
 * is now selected - it only pulls in dependencies when that flag is true.
 */
async function toggle(module: Project) {
    if (!currentVersion.value) {
        return
    }
    const name = module.repository.name
    const willSelect = !selected.value.includes(name)
    const next = willSelect
        ? [...selected.value, name]
        : selected.value.filter((n) => n !== name)

    error.value = ''
    // optimistic, so the checkbox tracks the click even when the server returns an identical set
    selected.value = next
    try {
        const response = await toggleProjectSelection(currentVersion.value, next, name, willSelect)
        selected.value = (response.data ?? []).map((m) => m.repository.name)
    } catch (e) {
        error.value = errorMessage(e)
    }
}

async function startRelease() {
    submitting.value = true
    error.value = ''
    try {
        await createReleaseDescriptor({
            fromVersion: currentVersion.value,
            releaseVersion: releaseVersion.value,
            devVersion: nextDevVersion.value,
            projects: selected.value,
        })
        confirmOpen.value = false
        await router.push('/release/board')
    } catch (e) {
        error.value = errorMessage(e)
    } finally {
        submitting.value = false
    }
}

onMounted(loadVersions)
</script>

<template>
    <PageShell title="Start a release" subtitle="Pick a version, then the modules to release">
        <template #actions>
            <button type="button" class="btn btn-sm btn-success" :disabled="!canStart"
                    @click="confirmOpen = true">
                <i class="bi bi-play-fill me-1" aria-hidden="true"></i>Start release
            </button>
        </template>

        <ErrorAlert :message="error" @dismiss="error = ''"/>

        <div class="row g-3 mb-4">
            <div class="col-md-4">
                <label class="form-label" for="current-version">Current version</label>
                <select id="current-version" v-model="currentVersion" class="form-select"
                        @change="selectVersion">
                    <option value="" disabled>Choose a version&hellip;</option>
                    <option v-for="version in versions" :key="version" :value="version">
                        {{ version }}
                    </option>
                </select>
            </div>
            <div class="col-md-4">
                <label class="form-label" for="release-version">Release version</label>
                <input id="release-version" v-model="releaseVersion" type="text" class="form-control"/>
            </div>
            <div class="col-md-4">
                <label class="form-label" for="dev-version">Next dev version</label>
                <input id="dev-version" v-model="nextDevVersion" type="text" class="form-control"/>
            </div>
        </div>

        <LoadingBar :loading="loading"/>

        <div v-if="modules.length" class="table-responsive">
            <table class="table table-sm table-hover align-middle">
                <thead>
                <tr>
                    <th scope="col" style="width: 1%">Release</th>
                    <th scope="col">Module</th>
                    <th scope="col">Version</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="module in modules" :key="module.id"
                    :class="{ 'opacity-50': module.disable }">
                    <td>
                        <input :id="`rel-${module.repository.name}`" class="form-check-input"
                               type="checkbox" :disabled="module.disable"
                               :checked="selected.includes(module.repository.name)"
                               @change="toggle(module)"/>
                    </td>
                    <td>
                        <label :for="`rel-${module.repository.name}`" class="mb-0">
                            {{ module.repository.name }}
                        </label>
                    </td>
                    <td class="text-nowrap">{{ module.version }}</td>
                </tr>
                </tbody>
            </table>
        </div>

        <EmptyState v-else-if="!loading && currentVersion"
                    message="No modules available for this version." icon="bi-box"/>
        <EmptyState v-else-if="!loading" message="Choose a version to see its modules."
                    icon="bi-arrow-up"/>

        <AppModal :open="confirmOpen" title="Start this release?" size="sm"
                  @close="confirmOpen = false">
            <dl class="row mb-3">
                <dt class="col-5">From version</dt>
                <dd class="col-7">{{ currentVersion }}</dd>
                <dt class="col-5">Release version</dt>
                <dd class="col-7">{{ releaseVersion }}</dd>
                <dt class="col-5">Next dev version</dt>
                <dd class="col-7">{{ nextDevVersion }}</dd>
            </dl>
            <p class="mb-1">Modules to release, in order:</p>
            <ol class="small">
                <li v-for="name in selected" :key="name">{{ name }}</li>
            </ol>
            <p class="fw-semibold mb-0">
                Be sure you have permission to push commits to these repositories.
            </p>

            <template #footer>
                <button type="button" class="btn btn-secondary" @click="confirmOpen = false">
                    Cancel
                </button>
                <button type="button" class="btn btn-success" :disabled="submitting"
                        @click="startRelease">
                    <span v-if="submitting" class="spinner-border spinner-border-sm me-2"
                          aria-hidden="true"></span>
                    Confirm
                </button>
            </template>
        </AppModal>
    </PageShell>
</template>
