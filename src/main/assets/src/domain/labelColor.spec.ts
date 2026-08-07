import { describe, expect, it } from 'vitest'
import { contrastRatio, labelChipVars, parseHexColor, readableForeground } from './labelColor'

describe('parseHexColor', () => {
    it('accepts the bare six-digit form the GitHub API returns', () => {
        expect(parseHexColor('f29513')).toEqual({ r: 0xf2, g: 0x95, b: 0x13 })
    })

    it('accepts a leading hash, three digits and upper case', () => {
        expect(parseHexColor('#FFF')).toEqual({ r: 255, g: 255, b: 255 })
        expect(parseHexColor('  0e8a16  ')).toEqual({ r: 0x0e, g: 0x8a, b: 0x16 })
    })

    it.each(['', 'zzzzzz', '#12345', '12345678', 'rebeccapurple'])(
        'returns null for the unusable value %j',
        (value) => {
            expect(parseHexColor(value)).toBeNull()
        },
    )
})

describe('readableForeground', () => {
    // Real values from GitHub's default label palette. The last two straddle the crossover and are
    // the cases a naive lightness threshold gets wrong.
    it.each([
        ['ffffff', '#000000'],
        ['000000', '#ffffff'],
        ['f29513', '#000000'], // orange
        ['e4e669', '#000000'], // yellow
        ['a2eeef', '#000000'], // cyan
        ['b60205', '#ffffff'], // dark red
        ['5319e7', '#ffffff'], // purple
        ['0075ca', '#ffffff'], // docs blue - just below the crossover
        ['d73a4a', '#000000'], // bug red - just above it
        ['0e8a16', '#000000'], // green
    ])('picks %s for the label colour %s', (color, expected) => {
        expect(readableForeground(parseHexColor(color)!)).toBe(expected)
    })

    it('always clears WCAG AA for normal text', () => {
        for (const color of ['f29513', 'e4e669', 'a2eeef', 'b60205', '5319e7', '0075ca', 'd73a4a']) {
            const background = parseHexColor(color)!
            const foreground = parseHexColor(readableForeground(background))!
            expect(contrastRatio(background, foreground)).toBeGreaterThanOrEqual(4.5)
        }
    })
})

describe('labelChipVars', () => {
    it('emits a normalised background and a readable foreground', () => {
        expect(labelChipVars('#D73A4A')).toEqual({ '--chip-bg': '#d73a4a', '--chip-fg': '#000000' })
    })

    it('returns null for an unusable colour, so the caller can fall back to a neutral chip', () => {
        expect(labelChipVars('invalid')).toBeNull()
    })
})
