/**
 * Increments the last number in a version string, leaving everything else alone.
 * `2.0.M1` -> `2.0.M2`, `3.0.5` -> `3.0.6`, `3.0` -> `3.1`.
 */
export function incrementLast(version: string): string {
    return version.replace(/[0-9]+(?!.*[0-9])/, (match) => String(parseInt(match, 10) + 1))
}

/** The release version implied by a current version: `3.0-SNAPSHOT` -> `3.0`. */
export function releaseVersionOf(currentVersion: string): string {
    return currentVersion.split('-')[0] ?? ''
}

/** The development version that follows a release: `3.0` -> `3.1-SNAPSHOT`. */
export function nextDevVersionOf(releaseVersion: string): string {
    const base = releaseVersion.split('-')[0] ?? ''
    return base ? `${incrementLast(base)}-SNAPSHOT` : ''
}
