import { computed, readonly, ref } from 'vue'

/** Pages that can leave a batch job running after navigation or a reload. */
export type JobOwner = 'milestone' | 'branches' | 'maven'

const STORAGE_KEY = 'bq.activeJob'

function stored(): JobOwner | null {
    const value = localStorage.getItem(STORAGE_KEY)
    return value === 'milestone' || value === 'branches' || value === 'maven' ? value : null
}

const active = ref<JobOwner | null>(stored())

/**
 * Which page started the batch job that is still running, surviving a reload. Lets a page resume
 * showing progress after F5, and the nav show a spinner from anywhere.
 */
export function useBackgroundJob() {
    return {
        active: readonly(active),
        isBusy: computed(() => active.value !== null),

        start(owner: JobOwner) {
            active.value = owner
            localStorage.setItem(STORAGE_KEY, owner)
        },

        finish() {
            active.value = null
            localStorage.removeItem(STORAGE_KEY)
        },

        /** True when the job still running belongs to this page, so it should resume tracking it. */
        isMine(owner: JobOwner): boolean {
            return active.value === owner
        },
    }
}
