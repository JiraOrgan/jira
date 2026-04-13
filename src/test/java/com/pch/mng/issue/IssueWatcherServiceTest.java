package com.pch.mng.issue;

import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.user.UserAccount;
import com.pch.mng.user.UserAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueWatcherServiceTest {

    @Mock
    private IssueRepository issueRepository;
    @Mock
    private IssueWatcherRepository issueWatcherRepository;
    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private IssueWatcherService issueWatcherService;

    @Test
    @DisplayName("listForIssue: 이슈 없으면 ENTITY_NOT_FOUND")
    void list_notFound() {
        when(issueRepository.findByIssueKey("X-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> issueWatcherService.listForIssue("X-1", 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("listForIssue: selfWatching 및 watcher 목록")
    void list_ok() {
        Issue issue = new Issue();
        issue.setId(10L);
        when(issueRepository.findByIssueKey("P-1")).thenReturn(Optional.of(issue));

        UserAccount u = UserAccount.builder().email("a@b.c").password("x").name("Alice").build();
        org.springframework.test.util.ReflectionTestUtils.setField(u, "id", 5L);
        IssueWatcher w = new IssueWatcher();
        w.setUser(u);
        when(issueWatcherRepository.findByIssueIdWithUserOrderByName(10L)).thenReturn(List.of(w));
        when(issueWatcherRepository.existsByIssueIdAndUserId(10L, 5L)).thenReturn(true);

        IssueWatcherResponse.ListDTO dto = issueWatcherService.listForIssue("P-1", 5L);

        assertThat(dto.isSelfWatching()).isTrue();
        assertThat(dto.getWatchers()).hasSize(1);
        assertThat(dto.getWatchers().getFirst().getUserId()).isEqualTo(5L);
        assertThat(dto.getWatchers().getFirst().getName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("watch: 이미 구독 중이면 save 호출 없음")
    void watch_idempotent() {
        Issue issue = new Issue();
        issue.setId(10L);
        when(issueRepository.findByIssueKey("P-1")).thenReturn(Optional.of(issue));
        UserAccount user = UserAccount.builder().email("a@b.c").password("x").name("Bob").build();
        when(userAccountRepository.findById(3L)).thenReturn(Optional.of(user));
        when(issueWatcherRepository.existsByIssueIdAndUserId(10L, 3L)).thenReturn(true);

        issueWatcherService.watch("P-1", 3L);

        verify(issueWatcherRepository, never()).save(any());
    }

    @Test
    @DisplayName("watch: 신규 구독 시 저장")
    void watch_new() {
        Issue issue = new Issue();
        issue.setId(10L);
        when(issueRepository.findByIssueKey("P-1")).thenReturn(Optional.of(issue));
        UserAccount user = UserAccount.builder().email("a@b.c").password("x").name("Bob").build();
        when(userAccountRepository.findById(3L)).thenReturn(Optional.of(user));
        when(issueWatcherRepository.existsByIssueIdAndUserId(10L, 3L)).thenReturn(false);

        issueWatcherService.watch("P-1", 3L);

        ArgumentCaptor<IssueWatcher> cap = ArgumentCaptor.forClass(IssueWatcher.class);
        verify(issueWatcherRepository).save(cap.capture());
        assertThat(cap.getValue().getIssue()).isSameAs(issue);
        assertThat(cap.getValue().getUser()).isSameAs(user);
    }
}
