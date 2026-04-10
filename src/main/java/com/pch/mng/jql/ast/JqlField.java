package com.pch.mng.jql.ast;

import com.pch.mng.jql.JqlParseException;

import java.util.Arrays;
import java.util.Locale;

/** SPIKE MVP: 필터 허용 필드 (대소문자 무시). */
public enum JqlField {
    PROJECT,
    STATUS,
    TYPE,
    ASSIGNEE,
    PRIORITY,
    SPRINT,
    TEXT;

    public static JqlField fromLexeme(String raw, int pos) {
        String k = raw.toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(v -> v.name().toLowerCase(Locale.ROOT).equals(k))
                .findFirst()
                .orElseThrow(() -> new JqlParseException("알 수 없는 필드: " + raw, pos));
    }
}
