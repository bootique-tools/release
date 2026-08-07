<script setup lang="ts">
import { ref } from 'vue'
import AppNav from './components/AppNav.vue'
import ErrorAlert from './components/ErrorAlert.vue'
import ValidationModal from './components/ValidationModal.vue'
import { useCacheWarmup } from './composables/useCacheWarmup'

const validationOpen = ref(false)

// Without this, an unreachable backend just leaves every page spinning with no explanation.
const { error: warmupError, retry: retryWarmup } = useCacheWarmup()
</script>

<!--
  /issue and /pr resolve to the same component, so without a key Vue reuses the instance across them
  and onMounted never re-fires - the new route would render the previous route's rows.
-->

<template>
    <AppNav @validate-pom="validationOpen = true" />

    <main class="container-xxl app-main">
        <ErrorAlert
            :message="warmupError"
            action-label="Retry"
            @action="retryWarmup"
            @dismiss="retryWarmup"
        />

        <RouterView v-slot="{ Component, route }">
            <Suspense>
                <component :is="Component" :key="route.path" />
                <template #fallback>
                    <div class="app-placeholder" role="status">
                        <span class="spinner-border" aria-hidden="true"></span>
                        <span class="visually-hidden">Loading</span>
                    </div>
                </template>
            </Suspense>
        </RouterView>
    </main>

    <ValidationModal :open="validationOpen" @close="validationOpen = false" />
</template>
