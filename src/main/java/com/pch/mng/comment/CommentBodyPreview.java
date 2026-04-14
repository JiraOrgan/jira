package com.pch.mng.comment;

/** 댓글 알림용 본문 미리보기 (과도한 길이 절단). */
public final class CommentBodyPreview {

    private static final int MAX = 300;

    private CommentBodyPreview() {}

    public static String of(String body) {
        if (body == null) {
            return "";
        }
        String t = body.trim();
        if (t.length() <= MAX) {
            return t;
        }
        return t.substring(0, MAX - 3) + "...";
    }
}
