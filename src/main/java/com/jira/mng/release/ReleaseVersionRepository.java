package com.jira.mng.release;

import com.jira.mng.global.enums.VersionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReleaseVersionRepository extends JpaRepository<ReleaseVersion, Long> {
    List<ReleaseVersion> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    List<ReleaseVersion> findByProjectIdAndStatus(Long projectId, VersionStatus status);
}
