package com.pch.mng.report;

import com.pch.mng.global.enums.IssueStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

public final class ReportResponse {

    private ReportResponse() {}

    @Data
    @Builder
    public static class BurndownDTO {
        private Long projectId;
        private Long sprintId;
        private String sprintName;
        private LocalDate startDate;
        private LocalDate endDate;
        /** 스프린트에 배정된 이슈 스토리 포인트 합(완료 포함, null 포인트는 0) */
        private int totalScopePoints;
        private List<BurndownPointDTO> series;
    }

    @Data
    @Builder
    public static class BurndownPointDTO {
        private LocalDate date;
        /** 해당 일 종료 시점 기준 미완료(DONE 제외) 스토리 포인트 합 */
        private int remainingStoryPoints;
        /** 이상적인 잔량(직선 감소) */
        private double idealRemainingPoints;
    }

    @Data
    @Builder
    public static class VelocityDTO {
        private Long projectId;
        private List<VelocityBarDTO> sprints;
    }

    @Data
    @Builder
    public static class VelocityBarDTO {
        private Long sprintId;
        private String sprintName;
        private LocalDate endDate;
        private long completedStoryPoints;
    }

    @Data
    @Builder
    public static class CfdDTO {
        private Long projectId;
        private Long sprintId;
        private int windowDays;
        private List<CfdDayDTO> series;
    }

    @Data
    @Builder
    public static class CfdDayDTO {
        private LocalDate date;
        private List<CfdStatusCountDTO> byStatus;
    }

    @Data
    @Builder
    public static class CfdStatusCountDTO {
        private IssueStatus status;
        private int count;
    }
}
