package com.pch.mng.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectComponentRepository extends JpaRepository<ProjectComponent, Long> {
    List<ProjectComponent> findByProjectId(Long projectId);

    @Query("SELECT c FROM ProjectComponent c JOIN FETCH c.project WHERE c.id = :id")
    Optional<ProjectComponent> findByIdWithProject(@Param("id") Long id);
}
