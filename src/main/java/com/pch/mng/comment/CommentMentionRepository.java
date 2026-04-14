package com.pch.mng.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CommentMentionRepository extends JpaRepository<CommentMention, Long> {

    void deleteByComment_Id(Long commentId);

    @Query(
            "SELECT m FROM CommentMention m JOIN FETCH m.user u WHERE m.comment.id IN :commentIds ORDER BY u.name ASC")
    List<CommentMention> findByComment_IdInWithUser(@Param("commentIds") Collection<Long> commentIds);
}
