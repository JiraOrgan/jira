package com.jira.mng.dashboard;

import com.jira.mng.global.exception.BusinessException;
import com.jira.mng.global.exception.ErrorCode;
import com.jira.mng.user.UserAccount;
import com.jira.mng.user.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class DashboardService {

    private final DashboardRepository dashboardRepository;
    private final DashboardGadgetRepository dashboardGadgetRepository;
    private final UserAccountRepository userAccountRepository;

    public List<DashboardResponse.MinDTO> findAccessible(Long userId) {
        return dashboardRepository.findAccessible(userId).stream()
                .map(DashboardResponse.MinDTO::of)
                .toList();
    }

    public DashboardResponse.DetailDTO findById(Long id) {
        Dashboard dashboard = dashboardRepository.findByIdWithGadgets(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        return DashboardResponse.DetailDTO.of(dashboard);
    }

    @Transactional
    public DashboardResponse.DetailDTO save(DashboardRequest.SaveDTO reqDTO, Long ownerId) {
        UserAccount owner = userAccountRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Dashboard dashboard = Dashboard.builder()
                .name(reqDTO.getName())
                .owner(owner)
                .shared(reqDTO.isShared())
                .build();
        dashboardRepository.save(dashboard);
        return DashboardResponse.DetailDTO.of(dashboard);
    }

    @Transactional
    public DashboardResponse.DetailDTO update(Long id, DashboardRequest.UpdateDTO reqDTO) {
        Dashboard dashboard = dashboardRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        if (reqDTO.getName() != null) dashboard.setName(reqDTO.getName());
        if (reqDTO.getShared() != null) dashboard.setShared(reqDTO.getShared());
        return DashboardResponse.DetailDTO.of(dashboard);
    }

    @Transactional
    public void delete(Long id) {
        Dashboard dashboard = dashboardRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        dashboardRepository.delete(dashboard);
    }

    @Transactional
    public DashboardResponse.DetailDTO addGadget(Long dashboardId, DashboardRequest.GadgetDTO reqDTO) {
        Dashboard dashboard = dashboardRepository.findByIdWithGadgets(dashboardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        DashboardGadget gadget = DashboardGadget.builder()
                .dashboard(dashboard)
                .gadgetType(reqDTO.getGadgetType())
                .position(reqDTO.getPosition())
                .configJson(reqDTO.getConfigJson())
                .build();
        dashboard.getGadgets().add(gadget);
        return DashboardResponse.DetailDTO.of(dashboard);
    }

    @Transactional
    public void removeGadget(Long dashboardId, Long gadgetId) {
        DashboardGadget gadget = dashboardGadgetRepository.findById(gadgetId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        dashboardGadgetRepository.delete(gadget);
    }
}
