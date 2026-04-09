package com.pch.mng.release;

import com.pch.mng.global.enums.VersionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReleaseVersionRepository extends JpaRepository<ReleaseVersion, Long> {
    List<ReleaseVersion> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    List<ReleaseVersion> findByProjectIdAndStatus(Long projectId, VersionStatus status);

    @Query("SELECT v FROM ReleaseVersion v JOIN FETCH v.project WHERE v.id = :id")
    Optional<ReleaseVersion> findByIdWithProject(@Param("id") Long id);
}
