/**
 * Contrast maths for GitHub label chips. The label colour is arbitrary user data from the API, so
 * the foreground has to be derived rather than assumed - the UI previously hardcoded white, which
 * is unreadable on the pale half of GitHub's own palette.
 *
 * Only the light-theme foreground is computed here. Dark mode re-derives its own from `--chip-bg`
 * in CSS, because the resolved theme is a DOM attribute rather than application state: `useTheme`
 * exposes `'auto'`, not the value `'auto'` resolved to.
 */

/** A colour as 0-255 sRGB channels. */
export interface Rgb {
    readonly r: number
    readonly g: number
    readonly b: number
}

/**
 * Parses a GitHub label colour. The API returns six hex digits with no leading `#` ("f29513");
 * three-digit and `#`-prefixed forms are accepted too. Returns null for anything else, so callers
 * can fall back to a neutral chip rather than rendering an invalid colour.
 */
export function parseHexColor(value: string): Rgb | null {
    const hex = value.trim().replace(/^#/, '')
    const full = hex.length === 3 ? hex.replace(/./g, (c) => c + c) : hex
    if (!/^[0-9a-f]{6}$/i.test(full)) {
        return null
    }
    const n = Number.parseInt(full, 16)
    return { r: (n >> 16) & 0xff, g: (n >> 8) & 0xff, b: n & 0xff }
}

/** WCAG 2.2 relative luminance: sRGB gamma-expanded, then weighted by BT.709. 0 = black, 1 = white. */
export function relativeLuminance({ r, g, b }: Rgb): number {
    const linear = (channel: number): number => {
        const c = channel / 255
        return c <= 0.04045 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4
    }
    return 0.2126 * linear(r) + 0.7152 * linear(g) + 0.0722 * linear(b)
}

/** WCAG contrast ratio between two colours: 1 (identical) to 21 (black on white). */
export function contrastRatio(a: Rgb, b: Rgb): number {
    const la = relativeLuminance(a)
    const lb = relativeLuminance(b)
    return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05)
}

const BLACK: Rgb = { r: 0, g: 0, b: 0 }
const WHITE: Rgb = { r: 255, g: 255, b: 255 }

/**
 * Black or white, whichever contrasts more against the background. The crossover sits near a
 * luminance of 0.179, which splits GitHub's own `d73a4a` and `0075ca` - both near-ties that clear
 * WCAG AA either way, so this simply picks the mathematical winner.
 */
export function readableForeground(background: Rgb): '#000000' | '#ffffff' {
    return contrastRatio(background, BLACK) >= contrastRatio(background, WHITE) ? '#000000' : '#ffffff'
}

/** The custom properties for a GitHub label chip, or null when the colour is unusable. */
export function labelChipVars(color: string): Record<string, string> | null {
    const rgb = parseHexColor(color)
    if (rgb === null) {
        return null
    }
    const hex = [rgb.r, rgb.g, rgb.b].map((c) => c.toString(16).padStart(2, '0')).join('')
    return { '--chip-bg': `#${hex}`, '--chip-fg': readableForeground(rgb) }
}
