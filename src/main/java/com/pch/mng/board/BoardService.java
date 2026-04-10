package com.pch.mng.board;

import com.pch.mng.global.enums.BoardSwimlane;
import com.pch.mng.global.enums.IssueStatus;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.issue.Issue;
import com.pch.mng.issue.IssueRepository;
import com.pch.mng.issue.IssueResponse;
import com.pch.mng.sprint.SprintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class BoardService {

    private static final IssueStatus[] COLUMN_ORDER = IssueStatus.values();

    private final SprintRepository sprintRepository;
    private final IssueRepository issueRepository;
    private final SprintBoardRedisCache sprintBoardRedisCache;

    public SprintBoardResponse getSprintBoard(Long sprintId, BoardSwimlane swimlane) {
        if (!sprintBoardRedisCache.isEnabled()) {
            return loadSprintBoard(sprintId, swimlane);
        }
        return sprintBoardRedisCache.get(sprintId, swimlane).orElseGet(() -> {
            SprintBoardResponse loaded = loadSprintBoard(sprintId, swimlane);
            sprintBoardRedisCache.put(sprintId, swimlane, loaded);
            return loaded;
        });
    }

    private SprintBoardResponse loadSprintBoard(Long sprintId, BoardSwimlane swimlane) {
        if (!sprintRepository.existsById(sprintId)) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }
        List<Issue> issues = issueRepository.findForSprintBoard(sprintId);
        Map<IssueStatus, List<Issue>> byStatus = issues.stream()
                .collect(Collectors.groupingBy(Issue::getStatus, () -> new EnumMap<>(IssueStatus.class), Collectors.toList()));

        SprintBoardResponse res = new SprintBoardResponse();
        res.setSwimlane(swimlane);
        List<SprintBoardResponse.ColumnDTO> columns = new ArrayList<>();
        for (IssueStatus status : COLUMN_ORDER) {
            List<Issue> inColumn = byStatus.getOrDefault(status, List.of());
            SprintBoardResponse.ColumnDTO col = new SprintBoardResponse.ColumnDTO();
            col.setStatus(status);
            col.setBuckets(buildBuckets(inColumn, swimlane));
            columns.add(col);
        }
        res.setColumns(columns);
        return res;
    }

    private List<SprintBoardResponse.SwimlaneBucketDTO> buildBuckets(List<Issue> issues, BoardSwimlane swimlane) {
        List<Issue> sorted = issues.stream()
                .sorted(Comparator.comparing(Issue::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
        if (swimlane != BoardSwimlane.ASSIGNEE) {
            SprintBoardResponse.SwimlaneBucketDTO b = new SprintBoardResponse.SwimlaneBucketDTO();
            b.setAssigneeId(null);
            b.setAssigneeName(null);
            b.setIssues(sorted.stream().map(IssueResponse.MinDTO::of).toList());
            return List.of(b);
        }
        if (sorted.isEmpty()) {
            SprintBoardResponse.SwimlaneBucketDTO b = new SprintBoardResponse.SwimlaneBucketDTO();
            b.setAssigneeId(null);
            b.setAssigneeName(null);
            b.setIssues(List.of());
            return List.of(b);
        }
        /* groupingBy는 null 키 불가 → 미배정은 센티널 -1L (DB id와 충돌 없음) */
        final long unassignedKey = -1L;
        Map<Long, List<Issue>> byAssignee = sorted.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getAssignee() != null ? i.getAssignee().getId() : unassignedKey,
                        Collectors.toList()));
        List<Long> keys = new ArrayList<>(byAssignee.keySet());
        keys.sort((a, b) -> {
            boolean ua = a == unassignedKey;
            boolean ub = b == unassignedKey;
            if (ua && !ub) {
                return -1;
            }
            if (!ua && ub) {
                return 1;
            }
            return Long.compare(a, b);
        });
        List<SprintBoardResponse.SwimlaneBucketDTO> out = new ArrayList<>();
        for (Long mapKey : keys) {
            List<Issue> list = byAssignee.get(mapKey);
            SprintBoardResponse.SwimlaneBucketDTO b = new SprintBoardResponse.SwimlaneBucketDTO();
            if (mapKey == unassignedKey) {
                b.setAssigneeId(null);
                b.setAssigneeName(null);
            } else {
                b.setAssigneeId(mapKey);
                b.setAssigneeName(list.get(0).getAssignee() != null ? list.get(0).getAssignee().getName() : null);
            }
            b.setIssues(list.stream().map(IssueResponse.MinDTO::of).toList());
            out.add(b);
        }
        return out;
    }
}
