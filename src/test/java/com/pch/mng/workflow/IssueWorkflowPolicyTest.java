package com.pch.mng.workflow;

import com.pch.mng.global.enums.IssueStatus;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class IssueWorkflowPolicyTest {

    private IssueWorkflowPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new IssueWorkflowPolicy();
    }

    @Test
    @DisplayName("동일 상태 전환은 허용(멱등)")
    void sameStatusIsNoOp() {
        assertThatCode(() -> policy.assertTransition(IssueStatus.QA, IssueStatus.QA))
                .doesNotThrowAnyException();
    }

    static Stream<Arguments> allowedEdges() {
        return Stream.of(
                arguments(IssueStatus.BACKLOG, IssueStatus.SELECTED),
                arguments(IssueStatus.SELECTED, IssueStatus.BACKLOG),
                arguments(IssueStatus.SELECTED, IssueStatus.IN_PROGRESS),
                arguments(IssueStatus.IN_PROGRESS, IssueStatus.CODE_REVIEW),
                arguments(IssueStatus.CODE_REVIEW, IssueStatus.IN_PROGRESS),
                arguments(IssueStatus.CODE_REVIEW, IssueStatus.QA),
                arguments(IssueStatus.QA, IssueStatus.IN_PROGRESS),
                arguments(IssueStatus.QA, IssueStatus.DONE)
        );
    }

    @ParameterizedTest
    @MethodSource("allowedEdges")
    @DisplayName("표준·재작업 허용 엣지")
    void allowedTransitions(IssueStatus from, IssueStatus to) {
        assertThatCode(() -> policy.assertTransition(from, to))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("BACKLOG에서 DONE 직행은 금지")
    void backlogToDoneRejected() {
        assertThatThrownBy(() -> policy.assertTransition(IssueStatus.BACKLOG, IssueStatus.DONE))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.WORKFLOW_VIOLATION);
    }

    @Test
    @DisplayName("DONE에서 다른 상태로 복귀는 금지")
    void doneIsTerminal() {
        assertThatThrownBy(() -> policy.assertTransition(IssueStatus.DONE, IssueStatus.QA))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.WORKFLOW_VIOLATION);
    }

    @Test
    @DisplayName("IN_PROGRESS에서 QA 직행은 금지")
    void inProgressToQaSkipped() {
        assertThatThrownBy(() -> policy.assertTransition(IssueStatus.IN_PROGRESS, IssueStatus.QA))
                .isInstanceOf(BusinessException.class);
    }
}
