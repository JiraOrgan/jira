package com.jira.mng.project;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByKey(String key);
    boolean existsByKey(String key);
    List<Project> findByArchivedFalseOrderByCreatedAtDesc();
}
