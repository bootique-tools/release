import { describe, expect, it } from 'vitest'
import { allModulesSelected, selectableNames } from './moduleSelection'
import type { Project } from '@/api/types'

/** A module row as the listing endpoints return it; only the name and `disable` flag matter here. */
function module(name: string, disable = false): Project {
    return {
        id: name,
        branchName: null,
        disable,
        groupId: 'io.bootique',
        pathStr: `/repos/${name}`,
        version: '3.0-SNAPSHOT',
        repository: {
            id: name,
            url: `https://github.com/bootique/${name}`,
            name,
            description: null,
            localStatus: 'OK',
            pushedAtStr: null,
            sshUrl: null,
            upstream: true,
        },
    }
}

const MODULES = [module('bootique'), module('bootique-jetty', true), module('bootique-jdbc')]

describe('selectableNames', () => {
    it('skips the modules the server disabled, keeping dependency order', () => {
        expect(selectableNames(MODULES)).toEqual(['bootique', 'bootique-jdbc'])
    })

    it('is empty when every module is disabled', () => {
        expect(selectableNames([module('bootique', true)])).toEqual([])
    })
})

describe('allModulesSelected', () => {
    it('is true when every selectable module is selected', () => {
        expect(allModulesSelected(MODULES, ['bootique', 'bootique-jdbc'])).toBe(true)
    })

    it('is false when one selectable module is missing', () => {
        expect(allModulesSelected(MODULES, ['bootique'])).toBe(false)
    })

    it('ignores a disabled module that somehow made it into the selection', () => {
        expect(allModulesSelected(MODULES, ['bootique', 'bootique-jdbc', 'bootique-jetty'])).toBe(true)
    })

    it('stays true when the selection names a module outside the listing', () => {
        expect(allModulesSelected(MODULES, ['bootique', 'bootique-jdbc', 'bootique-agrest'])).toBe(true)
    })

    it('is false for an empty listing', () => {
        expect(allModulesSelected([], [])).toBe(false)
    })

    it('is false when nothing is selectable - the state before a version is chosen', () => {
        expect(allModulesSelected([module('bootique', true)], ['bootique'])).toBe(false)
    })
})
