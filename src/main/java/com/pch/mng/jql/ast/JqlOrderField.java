package com.pch.mng.jql.ast;

import com.pch.mng.jql.JqlParseException;

import java.util.Arrays;
import java.util.Locale;

/** ORDER BY 절에서 허용하는 정렬 필드. */
public enum JqlOrderField {
    PROJECT,
    STATUS,
    TYPE,
    PRIORITY,
    KEY,
    CREATED,
    UPDATED,
    ASSIGNEE,
    SPRINT;

    public static JqlOrderField fromLexeme(String raw, int pos) {
        String k = raw.toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(v -> v.name().toLowerCase(Locale.ROOT).equals(k))
                .findFirst()
                .orElseThrow(() -> new JqlParseException("정렬에 사용할 수 없는 필드: " + raw, pos));
    }
}
