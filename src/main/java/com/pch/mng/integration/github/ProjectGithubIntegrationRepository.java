package com.pch.mng.integration.github;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectGithubIntegrationRepository extends JpaRepository<ProjectGithubIntegration, Long> {

    Optional<ProjectGithubIntegration> findByProject_Id(Long projectId);

    Optional<ProjectGithubIntegration> findByGithubRepoFullNameIgnoreCase(String githubRepoFullName);
}
