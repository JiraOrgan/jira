package com.pch.mng.comment;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 본문에서 `@토큰`을 추출한다. 공백·중복 제거 순서 유지. */
public final class CommentMentionParser {

    private static final Pattern MENTION = Pattern.compile("@([^\\s@]+)");

    private CommentMentionParser() {}

    public static List<String> distinctTokens(String body) {
        if (body == null || body.isEmpty()) {
            return List.of();
        }
        Matcher m = MENTION.matcher(body);
        LinkedHashSet<String> set = new LinkedHashSet<>();
        while (m.find()) {
            String t = m.group(1).trim();
            if (!t.isEmpty()) {
                set.add(t);
            }
        }
        return List.copyOf(set);
    }
}
