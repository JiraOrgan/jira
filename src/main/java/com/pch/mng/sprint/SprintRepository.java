package com.pch.mng.sprint;

import com.pch.mng.global.enums.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SprintRepository extends JpaRepository<Sprint, Long> {
    List<Sprint> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    Optional<Sprint> findByProjectIdAndStatus(Long projectId, SprintStatus status);
}
