package com.pch.mng.security;

import com.pch.mng.auth.CustomUserDetails;
import com.pch.mng.dashboard.DashboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("dashboardSecurity")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardSecurityService {

    private final DashboardRepository dashboardRepository;

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CustomUserDetails cd)) {
            return null;
        }
        return cd.getId();
    }

    public boolean canReadDashboard(Long dashboardId) {
        Long uid = currentUserId();
        if (uid == null || dashboardId == null) {
            return false;
        }
        return dashboardRepository.findById(dashboardId)
                .map(d -> d.getOwner().getId().equals(uid) || d.isShared())
                .orElse(false);
    }

    public boolean canWriteDashboard(Long dashboardId) {
        Long uid = currentUserId();
        if (uid == null || dashboardId == null) {
            return false;
        }
        return dashboardRepository.findById(dashboardId)
                .map(d -> d.getOwner().getId().equals(uid))
                .orElse(false);
    }
}
