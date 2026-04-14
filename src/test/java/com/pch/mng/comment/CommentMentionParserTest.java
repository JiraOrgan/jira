package com.pch.mng.comment;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMentionParserTest {

    @Test
    void emptyBody() {
        assertThat(CommentMentionParser.distinctTokens(null)).isEmpty();
        assertThat(CommentMentionParser.distinctTokens("")).isEmpty();
    }

    @Test
    void distinctOrdered() {
        assertThat(CommentMentionParser.distinctTokens("a @u1 @u2 @u1"))
                .containsExactly("u1", "u2");
    }

    @Test
    void skipsEmptyToken() {
        assertThat(CommentMentionParser.distinctTokens("@@hi @")).containsExactly("hi");
    }
}
