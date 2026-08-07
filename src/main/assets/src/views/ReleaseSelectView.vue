<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import PageShell from '@/components/PageShell.vue'
import LoadingBar from '@/components/LoadingBar.vue'
import AppAlert from '@/components/AppAlert.vue'
import EmptyState from '@/components/EmptyState.vue'
import AppModal from '@/components/AppModal.vue'
import ModuleTable from '@/components/ModuleTable.vue'
import AppSection from '@/components/AppSection.vue'
import BusyButton from '@/components/BusyButton.vue'
import { useAsyncResource } from '@/composables/useAsyncResource'
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

const versions = ref<string[]>([])
const currentVersion = ref('')
const releaseVersion = ref('')
const nextDevVersion = ref('')
const selected = ref<string[]>([])

const submitting = ref(false)
const confirmOpen = ref(false)

/**
 * The module listing, either unfiltered or narrowed to one version. Both arrive in Maven dependency
 * order and must never be re-sorted here - the order is the order the release will follow.
 */
const {
    data: modules,
    loading,
    error,
    reload: loadModules,
} = useAsyncResource<Project[], [version?: string]>({
    initial: [],
    warmCache: true,
    // the first load has a follow-up (deriving the version list), so it is driven below
    immediate: false,
    load: async (version?: string) => {
        if (version) {
            return (await listProjectsForVersion(version)).data ?? []
        }
        // `disable` is not persisted, so this listing comes back selectable; nothing is releasable
        // until a version is chosen, so mark every row disabled as the original page did
        return ((await listReleaseProjects()).data ?? []).map((m) => ({ ...m, disable: true }))
    },
})

const canStart = computed(
    () => selected.value.length > 0 && releaseVersion.value !== '' && nextDevVersion.value !== '',
)

watch(releaseVersion, (value) => {
    nextDevVersion.value = nextDevVersionOf(value)
})

async function selectVersion() {
    if (!currentVersion.value) {
        return
    }
    releaseVersion.value = releaseVersionOf(currentVersion.value)
    await loadModules(currentVersion.value)
    // the server marks releasable modules with disable=false; those start out checked
    selected.value = modules.value.filter((m) => !m.disable).map((m) => m.repository.name)
    if (!modules.value.length) {
        error.value = 'Please clone all repositories to your local repositories first.'
    }
}

/**
 * Toggling a module round-trips to the server, which resolves the transitive dependency closure and
 * returns the resulting set in release order.
 *
 * The server expects the selection as it will be AFTER the toggle, plus whether the clicked module
 * is now selected - it only pulls in dependencies when that flag is true. ModuleTable has already
 * applied the optimistic update by the time this runs, so `selected` is that post-toggle set.
 */
async function toggle(module: Project, willSelect: boolean) {
    if (!currentVersion.value) {
        return
    }
    const name = module.repository.name
    error.value = ''
    try {
        const response = await toggleProjectSelection(currentVersion.value, selected.value, name, willSelect)
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

onMounted(async () => {
    await loadModules()
    // distinct in first-seen order, matching the order the modules are released in. Derived once,
    // so narrowing to a version later does not shrink the choices in the dropdown.
    versions.value = [...new Set(modules.value.map((m) => m.version))]
})
</script>

<template>
    <PageShell title="Start a release" subtitle="Pick a version, then the modules to release">
        <template #actions>
            <BusyButton
                variant="success"
                icon="bi-play-fill"
                :disabled="!canStart"
                @click="confirmOpen = true"
            >
                Start release
            </BusyButton>
        </template>

        <AppAlert :message="error" @dismiss="error = ''" />

        <AppSection title="Versions">
            <div class="row g-3">
                <div class="col-md-4">
                    <label class="form-label" for="current-version">Current version</label>
                    <select
                        id="current-version"
                        v-model="currentVersion"
                        class="form-select"
                        @change="selectVersion"
                    >
                        <option value="" disabled>Choose a version&hellip;</option>
                        <option v-for="version in versions" :key="version" :value="version">
                            {{ version }}
                        </option>
                    </select>
                </div>
                <div class="col-md-4">
                    <label class="form-label" for="release-version">Release version</label>
                    <input id="release-version" v-model="releaseVersion" type="text" class="form-control" />
                </div>
                <div class="col-md-4">
                    <label class="form-label" for="dev-version">Next dev version</label>
                    <input id="dev-version" v-model="nextDevVersion" type="text" class="form-control" />
                </div>
            </div>
        </AppSection>

        <LoadingBar :loading="loading" />

        <!-- v-model keeps the click optimistic; @toggle round-trips so the server can pull in
             dependencies, then overwrites the selection with the closure it resolved -->
        <ModuleTable
            v-if="modules.length"
            v-model:selected="selected"
            :modules="modules"
            select-header="Release"
            @toggle="toggle"
        />

        <EmptyState
            v-else-if="!loading && currentVersion"
            message="No modules available for this version."
            icon="bi-box"
        />
        <EmptyState v-else-if="!loading" message="Choose a version to see its modules." icon="bi-arrow-up" />

        <AppModal :open="confirmOpen" title="Start this release?" size="sm" @close="confirmOpen = false">
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
            <p class="fw-semibold mb-0">Be sure you have permission to push commits to these repositories.</p>

            <template #footer>
                <button type="button" class="btn btn-secondary" @click="confirmOpen = false">Cancel</button>
                <button type="button" class="btn btn-success" :disabled="submitting" @click="startRelease">
                    <span
                        v-if="submitting"
                        class="spinner-border spinner-border-sm me-2"
                        aria-hidden="true"
                    ></span>
                    Confirm
                </button>
            </template>
        </AppModal>
    </PageShell>
</template>
