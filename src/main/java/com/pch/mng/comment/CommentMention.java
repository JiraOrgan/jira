package com.pch.mng.comment;

import com.pch.mng.user.UserAccount;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
@Table(
        name = "comment_mention_tb",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_comment_mention_comment_user",
                        columnNames = {"comment_id", "user_id"}))
public class CommentMention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Builder
    public CommentMention(Comment comment, UserAccount user) {
        this.comment = comment;
        this.user = user;
    }
}
