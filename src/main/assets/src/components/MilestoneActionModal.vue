<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import ConfirmModal from './ConfirmModal.vue'
import AppAlert from './AppAlert.vue'
import { listMilestoneTitles, type MilestoneAction } from '@/api/milestones'
import { errorMessage } from '@/api/http'

const props = defineProps<{
    action: MilestoneAction | null
    modules: string[]
}>()

const emit = defineEmits<{
    confirm: [titles: { milestoneTitle?: string; milestoneNewTitle?: string }]
    cancel: []
}>()

const titles = ref<string[]>([])
const loading = ref(false)
const error = ref('')
const currentTitle = ref('')
const newTitle = ref('')

// create invents a title; close acts on an existing one; rename needs both
const needsExisting = computed(() => props.action === 'close' || props.action === 'rename')
const needsNew = computed(() => props.action === 'create' || props.action === 'rename')

const canConfirm = computed(
    () =>
        !loading.value &&
        (!needsExisting.value || currentTitle.value !== '') &&
        (!needsNew.value || newTitle.value.trim() !== ''),
)

const heading = computed(() =>
    props.action ? `${props.action[0]!.toUpperCase()}${props.action.slice(1)} milestone` : '',
)

watch(
    () => props.action,
    async (action) => {
        if (!action) {
            return
        }
        currentTitle.value = ''
        newTitle.value = ''
        error.value = ''
        if (!needsExisting.value) {
            return
        }
        loading.value = true
        try {
            titles.value = await listMilestoneTitles(props.modules)
            currentTitle.value = titles.value[0] ?? ''
        } catch (e) {
            error.value = errorMessage(e)
        } finally {
            loading.value = false
        }
    },
)

function confirm() {
    emit('confirm', {
        milestoneTitle: needsExisting.value ? currentTitle.value : undefined,
        milestoneNewTitle: needsNew.value ? newTitle.value.trim() : undefined,
    })
}
</script>

<template>
    <ConfirmModal
        :open="action !== null"
        :title="heading"
        :confirm-disabled="!canConfirm"
        @confirm="confirm"
        @cancel="emit('cancel')"
    >
        <p class="text-body-secondary small">
            Applies to {{ modules.length }} selected module{{ modules.length === 1 ? '' : 's' }}.
        </p>

        <AppAlert :message="error" :dismissible="false" />

        <div v-if="needsExisting" class="mb-3">
            <label class="form-label" for="milestone-current">Current milestone title</label>
            <select id="milestone-current" v-model="currentTitle" class="form-select" :disabled="loading">
                <option v-for="title in titles" :key="title" :value="title">{{ title }}</option>
            </select>
            <p v-if="!loading && !titles.length" class="form-text text-warning-emphasis">
                No open milestones in the selected modules.
            </p>
        </div>

        <div v-if="needsNew">
            <label class="form-label" for="milestone-new">New milestone title</label>
            <input
                id="milestone-new"
                v-model="newTitle"
                type="text"
                class="form-control"
                placeholder="e.g. 4.0.M1"
            />
        </div>
    </ConfirmModal>
</template>
