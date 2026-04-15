package com.pch.mng.dashboard;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DashboardGadgetRepository extends JpaRepository<DashboardGadget, Long> {

    Optional<DashboardGadget> findByIdAndDashboard_Id(Long id, Long dashboardId);
}
