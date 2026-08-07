import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
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
            target: 'es2022',
            sourcemap: true,
            cssCodeSplit: false,
            rollupOptions: {
                output: {
                    // watch mode only empties outDir on the first build, so keep names stable there
                    // to avoid accumulating an orphan bundle per save
                    entryFileNames: dev ? 'assets/[name].js' : 'assets/[name]-[hash].js',
                    chunkFileNames: dev ? 'assets/[name].js' : 'assets/[name]-[hash].js',
                    assetFileNames: dev ? 'assets/[name][extname]' : 'assets/[name]-[hash][extname]',
                    manualChunks: { vendor: ['vue', 'vue-router'] },
                },
            },
        },
        css: {
            preprocessorOptions: {
                scss: {
                    api: 'modern-compiler',
                    // Bootstrap 5.3 still uses @import, if(), and the legacy color functions internally
                    silenceDeprecations: [
                        'import',
                        'global-builtin',
                        'color-functions',
                        'mixed-decls',
                        'if-function',
                    ],
                },
            },
        },
        test: {
            environment: 'node',
            include: ['src/**/*.spec.ts'],
        },
    }
})
