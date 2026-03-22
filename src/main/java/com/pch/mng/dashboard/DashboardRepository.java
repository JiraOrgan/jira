package com.pch.mng.dashboard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface DashboardRepository extends JpaRepository<Dashboard, Long> {
    List<Dashboard> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    @Query("SELECT d FROM Dashboard d WHERE d.shared = true OR d.owner.id = :userId ORDER BY d.createdAt DESC")
    List<Dashboard> findAccessible(@Param("userId") Long userId);

    @Query("SELECT d FROM Dashboard d LEFT JOIN FETCH d.gadgets WHERE d.id = :id")
    Optional<Dashboard> findByIdWithGadgets(@Param("id") Long id);
}
