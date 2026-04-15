package com.pch.mng.issue;

import com.pch.mng.global.enums.IssueType;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;

/**
 * PRD §3.1 이슈 타입 계층 (FR-002): Epic(L1) → Story/Task/Bug(L2) → Sub-task(L3).
 *
 * <p>허용/거절 (부모 타입 × 자식 타입, 동일 프로젝트는 별도 검증):
 * <pre>
 * 자식＼부모   (없음)   EPIC    STORY   TASK    BUG     SUBTASK
 * EPIC         허용     거절    거절    거절    거절    거절
 * STORY        허용     허용    거절    거절    거절    거절
 * TASK         허용     허용    거절    거절    거절    거절
 * BUG          허용     허용    거절    거절    거절    거절
 * SUBTASK      거절     거절    허용    허용    허용    거절
 * </pre>
 */
final class IssueHierarchyPolicy {

    private IssueHierarchyPolicy() {}

    static void assertValidParent(IssueType childType, Issue parentOrNull) {
        IssueType parentType = parentOrNull == null ? null : parentOrNull.getIssueType();
        switch (childType) {
            case EPIC -> {
                if (parentOrNull != null) {
                    throw new BusinessException(ErrorCode.INVALID_ISSUE_HIERARCHY);
                }
            }
            case STORY, TASK, BUG -> {
                if (parentOrNull == null) {
                    return;
                }
                if (parentType != IssueType.EPIC) {
                    throw new BusinessException(ErrorCode.INVALID_ISSUE_HIERARCHY);
                }
            }
            case SUBTASK -> {
                if (parentOrNull == null) {
                    throw new BusinessException(ErrorCode.INVALID_ISSUE_HIERARCHY);
                }
                if (parentType != IssueType.STORY && parentType != IssueType.TASK && parentType != IssueType.BUG) {
                    throw new BusinessException(ErrorCode.INVALID_ISSUE_HIERARCHY);
                }
            }
        }
    }
}
