import { getText } from './http'

export function generateReleaseNotes(milestoneTitle: string, todo: boolean): Promise<string> {
    return getText('/release-notes/generate', { milestoneTitle, todo })
}
