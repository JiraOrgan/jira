package com.pch.mng.board;

import com.pch.mng.global.enums.BoardSwimlane;
import com.pch.mng.global.enums.IssueStatus;
import com.pch.mng.issue.IssueResponse;
import lombok.Data;

import java.util.List;

@Data
public class SprintBoardResponse {

    private BoardSwimlane swimlane;
    private List<ColumnDTO> columns;

    @Data
    public static class ColumnDTO {
        private IssueStatus status;
        private List<SwimlaneBucketDTO> buckets;
    }

    @Data
    public static class SwimlaneBucketDTO {
        private Long assigneeId;
        private String assigneeName;
        private List<IssueResponse.MinDTO> issues;
    }
}
