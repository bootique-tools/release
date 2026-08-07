import { post } from './http'

/** Runs `mvn clean install` across every module as a batch job. A no-op if a job is already running. */
export function verifyAll(): Promise<void> {
    return post('/maven/verify')
}
