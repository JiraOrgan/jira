package com.pch.mng.project;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectComponentRepository extends JpaRepository<ProjectComponent, Long> {
    List<ProjectComponent> findByProjectId(Long projectId);
}
