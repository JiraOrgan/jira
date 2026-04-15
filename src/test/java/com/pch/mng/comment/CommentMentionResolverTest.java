package com.pch.mng.comment;

import com.pch.mng.global.enums.ProjectRole;
import com.pch.mng.project.Project;
import com.pch.mng.project.ProjectMember;
import com.pch.mng.project.ProjectMemberRepository;
import com.pch.mng.user.UserAccount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentMentionResolverTest {

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @InjectMocks
    private CommentMentionResolver resolver;

    @Test
    void resolvesCompactName() {
        UserAccount u =
                UserAccount.builder()
                        .email("kim@ex.com")
                        .password("x")
                        .name("Kim Lee")
                        .build();
        ReflectionTestUtils.setField(u, "id", 7L);
        Project p = new Project();
        ReflectionTestUtils.setField(p, "id", 1L);
        ProjectMember pm =
                ProjectMember.builder()
                        .project(p)
                        .user(u)
                        .role(ProjectRole.DEVELOPER)
                        .build();

        when(projectMemberRepository.findByProjectIdWithUser(10L)).thenReturn(List.of(pm));

        Set<Long> ids = resolver.resolveMentionedUserIds(10L, "hey @KimLee there");
        assertThat(ids).containsExactly(7L);
    }

    @Test
    void resolvesEmailLocalPart() {
        UserAccount u =
                UserAccount.builder()
                        .email("alpha@ex.com")
                        .password("x")
                        .name("Beta")
                        .build();
        ReflectionTestUtils.setField(u, "id", 3L);
        Project p = new Project();
        ProjectMember pm =
                ProjectMember.builder()
                        .project(p)
                        .user(u)
                        .role(ProjectRole.VIEWER)
                        .build();

        when(projectMemberRepository.findByProjectIdWithUser(1L)).thenReturn(List.of(pm));

        assertThat(resolver.resolveMentionedUserIds(1L, "@alpha")).containsExactly(3L);
    }
}
