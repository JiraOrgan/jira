import type { ApiResponse } from '../types/api'
import type { BoardSwimlane, SprintBoardData } from '../types/domain'
import { unwrapApi } from '../types/domain'
import { api } from './api'

export async function fetchSprintBoard(
  sprintId: number,
  swimlane: BoardSwimlane = 'NONE',
): Promise<SprintBoardData> {
  const { data } = await api.get<ApiResponse<SprintBoardData>>(
    `/api/v1/sprints/${sprintId}/board`,
    { params: { swimlane } },
  )
  return unwrapApi(data)
}
