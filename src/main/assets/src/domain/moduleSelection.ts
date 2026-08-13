import type { Project } from '@/api/types'

/**
 * Names of the modules a user can actually pick. The server marks the rest `disable`, and the release
 * descriptor must not name them.
 *
 * @param modules the listing as the server ordered it - Maven dependency order, preserved here
 */
export function selectableNames(modules: Project[]): string[] {
    return modules.filter((m) => !m.disable).map((m) => m.repository.name)
}

/**
 * Whether the header select-all checkbox should read as checked: every selectable module is picked.
 * False when nothing is selectable, so the box does not look checked on a listing that offers no
 * choice - the state before a version is chosen.
 *
 * The selection may name modules outside `modules`; the server resolves it and is authoritative.
 */
export function allModulesSelected(modules: Project[], selected: string[]): boolean {
    const names = selectableNames(modules)
    return names.length > 0 && names.every((name) => selected.includes(name))
}
