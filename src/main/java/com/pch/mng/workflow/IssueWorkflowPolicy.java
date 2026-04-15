package com.pch.mng.workflow;

import com.pch.mng.global.enums.IssueStatus;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 표준 6단계 FSM + 재작업(코드리뷰·QA에서 진행 중으로 복귀) 허용.
 * 조건부 전환(스파이크의 PR/QA 플래그)은 후속에서 {@code TransitionDTO} 확장으로 연결한다.
 */
@Component
public class IssueWorkflowPolicy {

    private record Edge(IssueStatus from, IssueStatus to) {}

    private static final Set<Edge> ALLOWED = Set.of(
            new Edge(IssueStatus.BACKLOG, IssueStatus.SELECTED),
            new Edge(IssueStatus.SELECTED, IssueStatus.BACKLOG),
            new Edge(IssueStatus.SELECTED, IssueStatus.IN_PROGRESS),
            new Edge(IssueStatus.IN_PROGRESS, IssueStatus.CODE_REVIEW),
            new Edge(IssueStatus.CODE_REVIEW, IssueStatus.IN_PROGRESS),
            new Edge(IssueStatus.CODE_REVIEW, IssueStatus.QA),
            new Edge(IssueStatus.QA, IssueStatus.IN_PROGRESS),
            new Edge(IssueStatus.QA, IssueStatus.DONE)
    );

    public void assertTransition(IssueStatus from, IssueStatus to) {
        if (from == to) {
            return;
        }
        if (!ALLOWED.contains(new Edge(from, to))) {
            throw new BusinessException(ErrorCode.WORKFLOW_VIOLATION);
        }
    }
}
