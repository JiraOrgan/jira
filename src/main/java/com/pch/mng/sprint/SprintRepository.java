package com.pch.mng.sprint;

import com.pch.mng.global.enums.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SprintRepository extends JpaRepository<Sprint, Long> {
    List<Sprint> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    Optional<Sprint> findByProjectIdAndStatus(Long projectId, SprintStatus status);

    @Query("SELECT s FROM Sprint s JOIN FETCH s.project WHERE s.id = :id")
    Optional<Sprint> findByIdWithProject(@Param("id") Long id);
}
