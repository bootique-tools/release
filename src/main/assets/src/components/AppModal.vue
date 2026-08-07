<script setup lang="ts">
import { nextTick, onBeforeUnmount, ref, watch } from 'vue'

const props = withDefaults(
    defineProps<{
        open: boolean
        title: string
        size?: 'sm' | 'lg' | 'xl'
        /** Hide the footer entirely when the caller supplies no actions. */
        closeLabel?: string
    }>(),
    { size: 'lg', closeLabel: 'Close' },
)

const emit = defineEmits<{ close: [] }>()

const dialog = ref<HTMLElement | null>(null)

function onKeydown(event: KeyboardEvent) {
    if (event.key === 'Escape') {
        emit('close')
    }
}

// Bootstrap's own modal JS is not used: it manipulates DOM that Vue also owns.
watch(
    () => props.open,
    async (open) => {
        document.body.classList.toggle('modal-open', open)
        document.body.style.overflow = open ? 'hidden' : ''
        if (open) {
            document.addEventListener('keydown', onKeydown)
            await nextTick()
            dialog.value?.focus()
        } else {
            document.removeEventListener('keydown', onKeydown)
        }
    },
)

onBeforeUnmount(() => {
    document.removeEventListener('keydown', onKeydown)
    document.body.classList.remove('modal-open')
    document.body.style.overflow = ''
})
</script>

<template>
    <Teleport to="body">
        <template v-if="open">
            <div class="modal fade show d-block" tabindex="-1" role="dialog"
                 :aria-label="title" @click.self="emit('close')">
                <div class="modal-dialog modal-dialog-scrollable" :class="`modal-${size}`">
                    <div ref="dialog" class="modal-content" tabindex="-1">
                        <div class="modal-header">
                            <h5 class="modal-title">{{ title }}</h5>
                            <button type="button" class="btn-close" aria-label="Close"
                                    @click="emit('close')"></button>
                        </div>
                        <div class="modal-body">
                            <slot/>
                        </div>
                        <div class="modal-footer">
                            <slot name="footer">
                                <button type="button" class="btn btn-secondary" @click="emit('close')">
                                    {{ closeLabel }}
                                </button>
                            </slot>
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-backdrop fade show"></div>
        </template>
    </Teleport>
</template>
