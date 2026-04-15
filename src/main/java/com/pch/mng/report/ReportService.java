package com.pch.mng.report;

import com.pch.mng.global.enums.IssueStatus;
import com.pch.mng.global.enums.SprintStatus;
import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.issue.Issue;
import com.pch.mng.issue.IssueRepository;
import com.pch.mng.issue.IssueSecurityPolicy;
import com.pch.mng.security.IssueVisibilityEvaluator;
import com.pch.mng.sprint.Sprint;
import com.pch.mng.sprint.SprintRepository;
import com.pch.mng.workflow.WorkflowTransition;
import com.pch.mng.workflow.WorkflowTransitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ReportService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final SprintRepository sprintRepository;
    private final IssueRepository issueRepository;
    private final WorkflowTransitionRepository workflowTransitionRepository;
    private final IssueVisibilityEvaluator issueVisibilityEvaluator;

    public ReportResponse.BurndownDTO burndown(Long projectId, Long sprintId) {
        Sprint sprint = sprintRepository.findByIdWithProject(sprintId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        if (!sprint.getProject().getId().equals(projectId)) {
            throw new BusinessException(ErrorCode.SPRINT_PROJECT_MISMATCH);
        }

        LocalDate today = LocalDate.now(SEOUL);
        LocalDate start = sprint.getStartDate() != null
                ? sprint.getStartDate()
                : sprint.getCreatedAt().toLocalDate();
        LocalDate end = sprint.getEndDate() != null ? sprint.getEndDate() : today;
        if (end.isBefore(start)) {
            end = start;
        }
        LocalDate lastDay = end.isAfter(today) ? today : end;

        var viewCtx = issueVisibilityEvaluator.requiredContextForProject(projectId);
        List<Issue> issues = issueRepository.findByProjectIdAndSprintIdAndArchivedFalseOrderByCreatedAtDesc(
                        projectId, sprintId).stream()
                .filter(i -> IssueSecurityPolicy.canView(i, viewCtx.role(), viewCtx.userId()))
                .toList();
        int totalScope = issues.stream()
                .mapToInt(i -> i.getStoryPoints() == null ? 0 : i.getStoryPoints())
                .sum();

        List<Long> ids = issues.stream().map(Issue::getId).toList();
        Map<Long, List<WorkflowTransition>> transitionsByIssue = loadTransitionsGrouped(ids);

        long spanDays = ChronoUnit.DAYS.between(start, end) + 1;
        if (spanDays < 1) {
            spanDays = 1;
        }

        List<ReportResponse.BurndownPointDTO> series = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(lastDay); d = d.plusDays(1)) {
            int remaining = 0;
            LocalDateTime endOfDay = d.atTime(LocalTime.MAX);
            for (Issue issue : issues) {
                IssueStatus st = statusAtEndOfDay(issue, endOfDay, transitionsByIssue);
                if (st == null) {
                    continue;
                }
                if (st != IssueStatus.DONE) {
                    remaining += issue.getStoryPoints() == null ? 0 : issue.getStoryPoints();
                }
            }
            long dayIndex = ChronoUnit.DAYS.between(start, d);
            double ideal;
            if (spanDays <= 1) {
                ideal = 0;
            } else {
                ideal = totalScope * (spanDays - 1 - dayIndex) / (spanDays - 1.0);
            }
            series.add(ReportResponse.BurndownPointDTO.builder()
                    .date(d)
                    .remainingStoryPoints(remaining)
                    .idealRemainingPoints(Math.max(0, ideal))
                    .build());
        }

        return ReportResponse.BurndownDTO.builder()
                .projectId(projectId)
                .sprintId(sprintId)
                .sprintName(sprint.getName())
                .startDate(start)
                .endDate(end)
                .totalScopePoints(totalScope)
                .series(series)
                .build();
    }

    public ReportResponse.VelocityDTO velocity(Long projectId, int limit) {
        int cap = Math.min(Math.max(limit, 1), 24);
        var viewCtx = issueVisibilityEvaluator.requiredContextForProject(projectId);
        List<Sprint> done = sprintRepository.findByProjectIdAndStatusOrderByEndDateDesc(projectId, SprintStatus.COMPLETED);
        List<ReportResponse.VelocityBarDTO> bars = new ArrayList<>();
        int n = 0;
        for (Sprint s : done) {
            if (n >= cap) {
                break;
            }
            long pts = issueRepository.findByProjectIdAndSprintIdAndArchivedFalseOrderByCreatedAtDesc(
                            projectId, s.getId()).stream()
                    .filter(i -> i.getStatus() == IssueStatus.DONE)
                    .filter(i -> IssueSecurityPolicy.canView(i, viewCtx.role(), viewCtx.userId()))
                    .mapToLong(i -> i.getStoryPoints() == null ? 0L : i.getStoryPoints())
                    .sum();
            bars.add(ReportResponse.VelocityBarDTO.builder()
                    .sprintId(s.getId())
                    .sprintName(s.getName())
                    .endDate(s.getEndDate())
                    .completedStoryPoints(pts)
                    .build());
            n++;
        }
        return ReportResponse.VelocityDTO.builder()
                .projectId(projectId)
                .sprints(bars)
                .build();
    }

    public ReportResponse.CfdDTO cumulativeFlow(Long projectId, Long sprintId, int windowDays) {
        int days = Math.min(Math.max(windowDays, 7), 90);
        LocalDate today = LocalDate.now(SEOUL);
        LocalDate from = today.minusDays(days - 1L);
        var viewCtx = issueVisibilityEvaluator.requiredContextForProject(projectId);

        List<Issue> issues;
        if (sprintId != null) {
            Sprint sprint = sprintRepository.findByIdWithProject(sprintId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
            if (!sprint.getProject().getId().equals(projectId)) {
                throw new BusinessException(ErrorCode.SPRINT_PROJECT_MISMATCH);
            }
            issues = issueRepository.findByProjectIdAndSprintIdAndArchivedFalseOrderByCreatedAtDesc(
                        projectId, sprintId).stream()
                    .filter(i -> IssueSecurityPolicy.canView(i, viewCtx.role(), viewCtx.userId()))
                    .toList();
        } else {
            issues =
                    issueRepository
                            .findByProjectIdAndArchivedFalseOrderByCreatedAtDesc(
                                    projectId, Pageable.unpaged())
                            .getContent()
                            .stream()
                    .filter(i -> IssueSecurityPolicy.canView(i, viewCtx.role(), viewCtx.userId()))
                    .toList();
        }

        List<Long> ids = issues.stream().map(Issue::getId).toList();
        Map<Long, List<WorkflowTransition>> transitionsByIssue = loadTransitionsGrouped(ids);

        List<ReportResponse.CfdDayDTO> series = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(today); d = d.plusDays(1)) {
            LocalDateTime endOfDay = d.atTime(LocalTime.MAX);
            EnumMap<IssueStatus, Integer> counts = new EnumMap<>(IssueStatus.class);
            for (IssueStatus st : IssueStatus.values()) {
                counts.put(st, 0);
            }
            for (Issue issue : issues) {
                IssueStatus st = statusAtEndOfDay(issue, endOfDay, transitionsByIssue);
                if (st != null) {
                    counts.merge(st, 1, Integer::sum);
                }
            }
            List<ReportResponse.CfdStatusCountDTO> row = new ArrayList<>();
            for (IssueStatus st : IssueStatus.values()) {
                row.add(ReportResponse.CfdStatusCountDTO.builder()
                        .status(st)
                        .count(counts.getOrDefault(st, 0))
                        .build());
            }
            series.add(ReportResponse.CfdDayDTO.builder()
                    .date(d)
                    .byStatus(row)
                    .build());
        }

        return ReportResponse.CfdDTO.builder()
                .projectId(projectId)
                .sprintId(sprintId)
                .windowDays(days)
                .series(series)
                .build();
    }

    private Map<Long, List<WorkflowTransition>> loadTransitionsGrouped(List<Long> issueIds) {
        if (issueIds.isEmpty()) {
            return Map.of();
        }
        List<WorkflowTransition> all = workflowTransitionRepository.findByIssueIdInOrderByIssueAndTimeAsc(issueIds);
        return all.stream().collect(Collectors.groupingBy(wt -> wt.getIssue().getId(),
                LinkedHashMap::new,
                Collectors.toList()));
    }

    /**
     * 이슈 생성 전 시점이면 null. 전환 이력이 없으면 BACKLOG로 간주한다.
     */
    private static IssueStatus statusAtEndOfDay(
            Issue issue,
            LocalDateTime endOfDay,
            Map<Long, List<WorkflowTransition>> transitionsByIssue) {
        LocalDateTime created = issue.getCreatedAt();
        if (created != null && endOfDay.isBefore(created)) {
            return null;
        }
        List<WorkflowTransition> list = transitionsByIssue.getOrDefault(issue.getId(), List.of());
        IssueStatus last = IssueStatus.BACKLOG;
        for (WorkflowTransition wt : list) {
            if (wt.getTransitionedAt().isAfter(endOfDay)) {
                break;
            }
            last = wt.getToStatus();
        }
        return last;
    }
}
