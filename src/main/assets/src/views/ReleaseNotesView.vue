<script setup lang="ts">
import { computed, ref } from 'vue'
import PageShell from '@/components/PageShell.vue'
import LoadingBar from '@/components/LoadingBar.vue'
import AppAlert from '@/components/AppAlert.vue'
import BusyButton from '@/components/BusyButton.vue'
import LogOutput from '@/components/LogOutput.vue'
import AppSection from '@/components/AppSection.vue'
import { generateReleaseNotes } from '@/api/releaseNotes'
import { errorMessage } from '@/api/http'

const milestoneTitle = ref('')
const notes = ref('')
const loading = ref(false)
const error = ref('')
const copied = ref(false)

const canGenerate = computed(() => milestoneTitle.value.trim() !== '' && !loading.value)

async function generate(todo: boolean) {
    loading.value = true
    error.value = ''
    copied.value = false
    try {
        notes.value = await generateReleaseNotes(milestoneTitle.value.trim(), todo)
    } catch (e) {
        error.value = errorMessage(e)
    } finally {
        loading.value = false
    }
}

async function copy() {
    try {
        await navigator.clipboard.writeText(notes.value)
        copied.value = true
    } catch (e) {
        error.value = errorMessage(e)
    }
}
</script>

<template>
    <PageShell subtitle="Generated from the issues in a milestone">
        <template #actions>
            <BusyButton v-if="notes" :icon="copied ? 'bi-check2' : 'bi-clipboard'" @click="copy">
                {{ copied ? 'Copied' : 'Copy' }}
            </BusyButton>
        </template>

        <AppAlert :message="error" @dismiss="error = ''" />

        <AppSection title="Milestone">
            <div class="row g-2 align-items-end">
                <div class="col-sm-5">
                    <label class="form-label" for="milestone-title">Milestone title</label>
                    <input
                        id="milestone-title"
                        v-model="milestoneTitle"
                        type="text"
                        class="form-control"
                        placeholder="e.g. 4.0.M1"
                        @keyup.enter="canGenerate && generate(false)"
                    />
                </div>
                <div class="col-sm-auto">
                    <BusyButton variant="success" :disabled="!canGenerate" @click="generate(false)">
                        Issues done
                    </BusyButton>
                </div>
                <div class="col-sm-auto">
                    <BusyButton variant="info" :disabled="!canGenerate" @click="generate(true)">
                        Issues to do
                    </BusyButton>
                </div>
            </div>
        </AppSection>

        <LoadingBar :loading="loading" />

        <AppSection v-if="notes" title="Notes">
            <LogOutput :text="notes" />
        </AppSection>
    </PageShell>
</template>
