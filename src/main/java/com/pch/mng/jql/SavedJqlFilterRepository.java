package com.pch.mng.jql;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedJqlFilterRepository extends JpaRepository<SavedJqlFilter, Long> {

    List<SavedJqlFilter> findByProjectIdAndOwnerIdOrderByCreatedAtDesc(Long projectId, Long ownerId);

    Optional<SavedJqlFilter> findByIdAndProjectId(Long id, Long projectId);
}
