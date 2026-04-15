package com.pch.mng.issue;

import com.pch.mng.global.enums.IssueStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 프로젝트 로드맵용 Epic 행 (FR-012). effective* 는 미설정 시 created/updated 일자로 보강.
 */
@Value
@Builder
public class RoadmapEpicResponse {

    private static final int MIN_SPAN_DAYS = 14;

    Long id;
    String issueKey;
    String summary;
    IssueStatus status;
    LocalDate epicStartDate;
    LocalDate epicEndDate;
    LocalDate effectiveStart;
    LocalDate effectiveEnd;

    public static RoadmapEpicResponse of(Issue issue) {
        LocalDate created = issue.getCreatedAt() != null
                ? issue.getCreatedAt().toLocalDate()
                : LocalDate.now();
        LocalDate updated = issue.getUpdatedAt() != null
                ? issue.getUpdatedAt().toLocalDate()
                : created;

        LocalDate epicStart = issue.getEpicStartDate();
        LocalDate epicEnd = issue.getEpicEndDate();

        LocalDate effStart = epicStart != null ? epicStart : created;
        LocalDate effEnd = epicEnd != null ? epicEnd : updated;
        if (effEnd.isBefore(effStart)) {
            effEnd = effStart;
        }
        long daysBetween = ChronoUnit.DAYS.between(effStart, effEnd);
        if (daysBetween < MIN_SPAN_DAYS - 1) {
            effEnd = effStart.plusDays(MIN_SPAN_DAYS - 1L);
        }

        return RoadmapEpicResponse.builder()
                .id(issue.getId())
                .issueKey(issue.getIssueKey())
                .summary(issue.getSummary())
                .status(issue.getStatus())
                .epicStartDate(epicStart)
                .epicEndDate(epicEnd)
                .effectiveStart(effStart)
                .effectiveEnd(effEnd)
                .build();
    }
}
