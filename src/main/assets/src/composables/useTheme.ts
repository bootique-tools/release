import { readonly, ref, watch } from 'vue'

export type Theme = 'light' | 'dark' | 'auto'

const STORAGE_KEY = 'bq.theme'

function stored(): Theme {
    const value = localStorage.getItem(STORAGE_KEY)
    return value === 'light' || value === 'dark' || value === 'auto' ? value : 'auto'
}

const theme = ref<Theme>(stored())
const media = window.matchMedia('(prefers-color-scheme: dark)')

function apply() {
    const dark = theme.value === 'dark' || (theme.value === 'auto' && media.matches)
    document.documentElement.setAttribute('data-bs-theme', dark ? 'dark' : 'light')
}

watch(theme, (value) => {
    localStorage.setItem(STORAGE_KEY, value)
    apply()
})
media.addEventListener('change', () => {
    if (theme.value === 'auto') {
        apply()
    }
})
apply()

const ORDER: Theme[] = ['light', 'dark', 'auto']

/** Theme selection, shared app-wide. The initial value is also applied by an inline script in index.html. */
export function useTheme() {
    return {
        theme: readonly(theme),
        cycle: () => {
            theme.value = ORDER[(ORDER.indexOf(theme.value) + 1) % ORDER.length]!
        },
    }
}
