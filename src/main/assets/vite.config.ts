import { fileURLToPath, URL } from 'node:url'
// from 'vitest/config', not 'vite': Vitest no longer augments Vite's config type, so the `test`
// block below would otherwise be untyped
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
    const dev = mode !== 'production'

    return {
        // the app is served from /ui/, so every emitted asset URL is /ui/assets/...
        base: '/ui/',
        plugins: [vue()],
        resolve: {
            alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) },
        },
        build: {
            // a dedicated subdirectory, so emptying it can never touch anything else on the classpath
            outDir: fileURLToPath(new URL('../../../target/classes/static/ui', import.meta.url)),
            emptyOutDir: true,
            // explicit, because Vite 8 defaults to 'baseline-widely-available' instead
            target: 'es2022',
            sourcemap: true,
            cssCodeSplit: false,
            rolldownOptions: {
                output: {
                    // watch mode only empties outDir on the first build, so keep names stable there
                    // to avoid accumulating an orphan bundle per save
                    entryFileNames: dev ? 'assets/[name].js' : 'assets/[name]-[hash].js',
                    chunkFileNames: dev ? 'assets/[name].js' : 'assets/[name]-[hash].js',
                    assetFileNames: dev ? 'assets/[name][extname]' : 'assets/[name]-[hash][extname]',
                    // Rolldown's replacement for the removed object form of `manualChunks`
                    codeSplitting: {
                        groups: [{ name: 'vendor', test: /node_modules[\\/](vue|vue-router)[\\/]/ }],
                    },
                },
            },
        },
        css: {
            preprocessorOptions: {
                scss: {
                    // Bootstrap 5.3 still uses @import, if(), and the legacy color functions
                    // internally. This is the exact minimal set - notably NOT 'mixed-decls', which
                    // is obsolete as of Sass 1.100 and warns if you silence it.
                    silenceDeprecations: ['import', 'global-builtin', 'color-functions', 'if-function'],
                },
            },
        },
        test: {
            environment: 'node',
            include: ['src/**/*.spec.ts'],
        },
    }
})
