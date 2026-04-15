package com.pch.mng.sprint;

import com.pch.mng.global.enums.SprintIncompleteIssueDisposition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

public class SprintRequest {

    @Data
    public static class SaveDTO {
        @NotNull
        private Long projectId;
        @NotBlank
        private String name;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer goalPoints;
    }

    @Data
    public static class UpdateDTO {
        private String name;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer goalPoints;
    }

    /** 스프린트 완료 시 미완료(DONE 아님) 이슈 처리. 본문 생략 시 BACKLOG와 동일. */
    @Data
    public static class CompleteDTO {
        /** null이면 BACKLOG로 간주 */
        private SprintIncompleteIssueDisposition disposition;
        /** disposition이 NEXT_SPRINT일 때 필수. 동일 프로젝트의 PLANNING 스프린트 ID. */
        private Long nextSprintId;
    }
}
