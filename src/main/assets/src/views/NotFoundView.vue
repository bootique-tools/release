<script setup lang="ts">
import { useRoute } from 'vue-router'
import PageShell from '@/components/PageShell.vue'
import AppSection from '@/components/AppSection.vue'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
</script>

<template>
    <PageShell>
        <AppSection>
            <EmptyState message="That address does not match anything." icon="bi-signpost-split">
                <p class="mt-2">
                    <code>{{ route.fullPath }}</code>
                </p>
                <RouterLink class="btn btn-sm btn-primary" :to="{ name: 'repositories' }"
                    >Back to repositories</RouterLink
                >
            </EmptyState>
        </AppSection>
    </PageShell>
</template>
