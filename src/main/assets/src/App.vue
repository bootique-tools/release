<script setup lang="ts">
import { ref } from 'vue'
import AppNav from './components/AppNav.vue'
import ValidationModal from './components/ValidationModal.vue'

const validationOpen = ref(false)
</script>

<!--
  /issue and /pr resolve to the same component, so without a key Vue reuses the instance across them
  and onMounted never re-fires - the new route would render the previous route's rows.
-->

<template>
    <AppNav @validate-pom="validationOpen = true"/>

    <main class="container-xxl py-4">
        <RouterView v-slot="{ Component, route }">
            <Suspense>
                <component :is="Component" :key="route.path"/>
                <template #fallback>
                    <div class="text-center text-body-secondary py-5">
                        <span class="spinner-border" aria-hidden="true"></span>
                    </div>
                </template>
            </Suspense>
        </RouterView>
    </main>

    <ValidationModal :open="validationOpen" @close="validationOpen = false"/>
</template>
