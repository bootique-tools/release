import js from '@eslint/js'
import pluginVue from 'eslint-plugin-vue'
import { defineConfigWithVueTs, vueTsConfigs } from '@vue/eslint-config-typescript'
import skipFormatting from '@vue/eslint-config-prettier/skip-formatting'

export default defineConfigWithVueTs(
    { files: ['**/*.{ts,vue}'] },
    { ignores: ['node/**', 'node_modules/**', 'dist/**'] },

    js.configs.recommended,
    pluginVue.configs['flat/recommended'],
    // not `recommendedTypeChecked`: vue-tsc already runs with strict, noUnusedLocals,
    // noUnusedParameters and noImplicitOverride, so type-aware rules would mostly duplicate it
    // while roughly doubling lint time
    vueTsConfigs.recommended,
    skipFormatting,

    {
        rules: {
            'vue/multi-word-component-names': 'off', // App.vue
            'vue/html-self-closing': 'off', // <LoadingBar :loading="loading"/> is house style
            // a type-only optional prop is undefined when omitted, which is exactly what `?:` says;
            // this rule predates <script setup lang="ts"> and would force meaningless defaults
            'vue/require-default-prop': 'off',
            '@typescript-eslint/consistent-type-imports': ['error', { fixStyle: 'inline-type-imports' }],
            '@typescript-eslint/no-unused-vars': 'off', // tsconfig's noUnused* already gates this
        },
    },
)
