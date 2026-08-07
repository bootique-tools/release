import { getJson, getText } from './http'
import type { SessionInfo, ToolCheck } from './types'

export function fetchSession(): Promise<SessionInfo> {
    return getJson<SessionInfo>('/session')
}

/** True once the scheduled GitHub import has populated the local database. */
export function checkCache(): Promise<boolean> {
    return getJson<boolean>('/checkCache')
}

export function fetchToolCheck(): Promise<ToolCheck> {
    return getJson<ToolCheck>('/tool-check')
}

/** Plain-text POM validation report across all modules. */
export function validatePoms(): Promise<string> {
    return getText('/validation/pom', undefined, { noTimeout: true })
}
