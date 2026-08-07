import { getJson } from './http'
import type { JobStatusResponse } from './types'

export const JOB_STATUS_SOCKET = '/job/status'

export function fetchJobStatus(): Promise<JobStatusResponse> {
    return getJson<JobStatusResponse>('/job/status')
}
