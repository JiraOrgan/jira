package com.pch.mng.jql;

import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.issue.Issue;
import com.pch.mng.issue.IssueResponse;
import com.pch.mng.issue.QIssue;
import com.pch.mng.jql.ast.JqlQuery;
import com.pch.mng.project.Project;
import com.pch.mng.project.ProjectRepository;
import com.pch.mng.user.UserAccount;
import com.pch.mng.user.UserAccountRepository;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JqlSearchService {

    private final JPAQueryFactory queryFactory;
    private final ProjectRepository projectRepository;
    private final SavedJqlFilterRepository savedJqlFilterRepository;
    private final UserAccountRepository userAccountRepository;
    private final JqlSearchProperties jqlSearchProperties;

    public JqlSearchResponse search(Long projectId, JqlSearchRequest req) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        JqlQuery ast = JqlParser.parse(req.getJql());
        JqlQueryTranslator.validateProjectClauses(ast.where(), project.getKey());

        QIssue issue = QIssue.issue;
        BooleanExpression where = issue.project.id.eq(projectId);
        if (!ast.matchesAll()) {
            where = where.and(JqlQueryTranslator.buildPredicate(ast.where(), issue));
        }

        int max = resolveMaxResults(req.getMaxResults());
        int start = req.getStartAt() == null || req.getStartAt() < 0 ? 0 : req.getStartAt();
        OrderSpecifier<?>[] order = JqlQueryTranslator.toOrderSpecifiers(ast.orderBy(), issue);

        Long totalRow = queryFactory.select(issue.id.count()).from(issue).where(where).fetchOne();
        long total = totalRow == null ? 0L : totalRow;

        // JOIN FETCH + 페이징(offset/limit) 조합은 Hibernate에서 예외·오동작을 유발할 수 있어
        // assignee는 동일 @Transactional 내 MinDTO.of()에서 지연 로딩한다.
        List<Issue> rows = queryFactory.selectFrom(issue)
                .where(where)
                .orderBy(order)
                .offset(start)
                .limit(max)
                .fetch();

        List<IssueResponse.MinDTO> dtos = rows.stream().map(IssueResponse.MinDTO::of).toList();
        return JqlSearchResponse.builder()
                .startAt(start)
                .maxResults(max)
                .total(total)
                .issues(dtos)
                .build();
    }

    private int resolveMaxResults(Integer reqMax) {
        int cap = jqlSearchProperties.getMaxResultsCap();
        int def = jqlSearchProperties.getDefaultMaxResults();
        int m = reqMax == null ? def : reqMax;
        return Math.min(Math.max(1, m), cap);
    }

    @Transactional
    public SavedJqlFilterResponse saveFilter(Long projectId, SavedJqlFilterRequest req, Long ownerId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        UserAccount owner = userAccountRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        JqlQuery ast = JqlParser.parse(req.getJql());
        JqlQueryTranslator.validateProjectClauses(ast.where(), project.getKey());

        SavedJqlFilter entity = SavedJqlFilter.builder()
                .project(project)
                .owner(owner)
                .name(req.getName().trim())
                .jql(req.getJql().strip())
                .build();
        return SavedJqlFilterResponse.of(savedJqlFilterRepository.save(entity));
    }

    public List<SavedJqlFilterResponse> listFilters(Long projectId, Long ownerId) {
        return savedJqlFilterRepository.findByProjectIdAndOwnerIdOrderByCreatedAtDesc(projectId, ownerId).stream()
                .map(SavedJqlFilterResponse::of)
                .toList();
    }

    @Transactional
    public void deleteFilter(Long projectId, Long filterId, Long actorId) {
        SavedJqlFilter f = savedJqlFilterRepository.findByIdAndProjectId(filterId, projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        if (!f.getOwner().getId().equals(actorId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        savedJqlFilterRepository.delete(f);
    }
}
