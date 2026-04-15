package com.pch.mng.comment;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommentBodyPreviewTest {

    @Test
    void truncatesLongBody() {
        String longText = "x".repeat(400);
        assertThat(CommentBodyPreview.of(longText)).hasSize(300).endsWith("...");
    }

    @Test
    void nullSafe() {
        assertThat(CommentBodyPreview.of(null)).isEmpty();
    }
}
