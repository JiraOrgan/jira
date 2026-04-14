package com.pch.mng.comment;

import com.pch.mng.project.ProjectMember;
import com.pch.mng.project.ProjectMemberRepository;
import com.pch.mng.user.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * `@토큰`을 프로젝트 멤버 사용자로 해석한다. (이름 공백 제거·전체 이름·이메일 로컬파트, 대소문자 무시)
 */
@Component
@RequiredArgsConstructor
public class CommentMentionResolver {

    private final ProjectMemberRepository projectMemberRepository;

    public Set<Long> resolveMentionedUserIds(Long projectId, String body) {
        List<String> tokens = CommentMentionParser.distinctTokens(body);
        if (tokens.isEmpty()) {
            return Set.of();
        }
        List<ProjectMember> members = projectMemberRepository.findByProjectIdWithUser(projectId);
        Set<Long> result = new LinkedHashSet<>();
        for (String token : tokens) {
            members.stream()
                    .map(ProjectMember::getUser)
                    .filter(u -> matchesUser(u, token))
                    .map(UserAccount::getId)
                    .findFirst()
                    .ifPresent(result::add);
        }
        return result;
    }

    private static boolean matchesUser(UserAccount u, String rawToken) {
        if (u == null || rawToken == null || rawToken.isEmpty()) {
            return false;
        }
        String token = rawToken.toLowerCase(Locale.ROOT);
        if (normalizeName(u.getName()).equals(token)) {
            return true;
        }
        if (u.getName() != null && u.getName().toLowerCase(Locale.ROOT).equals(token)) {
            return true;
        }
        String email = u.getEmail();
        if (email == null) {
            return false;
        }
        int at = email.indexOf('@');
        if (at > 0 && email.substring(0, at).toLowerCase(Locale.ROOT).equals(token)) {
            return true;
        }
        return false;
    }

    static String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        return name.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }
}
