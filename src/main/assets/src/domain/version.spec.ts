import { describe, expect, it } from 'vitest'
import { incrementLast, nextDevVersionOf, releaseVersionOf } from './version'

describe('incrementLast', () => {
    it.each([
        ['2.0.M1', '2.0.M2'],
        ['3.0.5', '3.0.6'],
        ['3.0', '3.1'],
        ['1.0.RC9', '1.0.RC10'],
        ['3.0.M1-SNAPSHOT', '3.0.M2-SNAPSHOT'],
    ])('turns %s into %s', (input, expected) => {
        expect(incrementLast(input)).toBe(expected)
    })

    it('leaves a version with no digits alone', () => {
        expect(incrementLast('SNAPSHOT')).toBe('SNAPSHOT')
    })
})

describe('releaseVersionOf', () => {
    it.each([
        ['3.0-SNAPSHOT', '3.0'],
        ['2.0.M1-SNAPSHOT', '2.0.M1'],
        ['3.0', '3.0'],
    ])('turns %s into %s', (input, expected) => {
        expect(releaseVersionOf(input)).toBe(expected)
    })
})

describe('nextDevVersionOf', () => {
    it.each([
        ['2.0.M1', '2.0.M2-SNAPSHOT'],
        ['3.0', '3.1-SNAPSHOT'],
        ['3.0.5', '3.0.6-SNAPSHOT'],
    ])('turns %s into %s', (input, expected) => {
        expect(nextDevVersionOf(input)).toBe(expected)
    })

    it('is empty for an empty release version', () => {
        expect(nextDevVersionOf('')).toBe('')
    })
})
