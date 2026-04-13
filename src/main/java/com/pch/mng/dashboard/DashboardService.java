package com.pch.mng.dashboard;

import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.user.UserAccount;
import com.pch.mng.user.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
        if (!DashboardGadgetType.isValid(reqDTO.getGadgetType())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        Dashboard dashboard = dashboardRepository.findByIdWithGadgets(dashboardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        DashboardGadget gadget = DashboardGadget.builder()
                .dashboard(dashboard)
                .gadgetType(reqDTO.getGadgetType().trim())
                .position(reqDTO.getPosition())
                .configJson(reqDTO.getConfigJson())
                .build();
        dashboard.getGadgets().add(gadget);
        return DashboardResponse.DetailDTO.of(dashboard);
    }

    @Transactional
    public DashboardResponse.DetailDTO updateGadget(Long dashboardId, Long gadgetId, DashboardRequest.GadgetUpdateDTO reqDTO) {
        if (reqDTO.getGadgetType() == null && reqDTO.getPosition() == null && reqDTO.getConfigJson() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        DashboardGadget gadget = dashboardGadgetRepository.findByIdAndDashboard_Id(gadgetId, dashboardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DASHBOARD_GADGET_MISMATCH));
        if (reqDTO.getGadgetType() != null) {
            if (!DashboardGadgetType.isValid(reqDTO.getGadgetType())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            gadget.setGadgetType(reqDTO.getGadgetType().trim());
        }
        if (reqDTO.getPosition() != null) {
            gadget.setPosition(reqDTO.getPosition());
        }
        if (reqDTO.getConfigJson() != null) {
            gadget.setConfigJson(reqDTO.getConfigJson());
        }
        Dashboard dashboard = dashboardRepository.findByIdWithGadgets(dashboardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        return DashboardResponse.DetailDTO.of(dashboard);
    }

    @Transactional
    public DashboardResponse.DetailDTO reorderGadgets(Long dashboardId, DashboardRequest.GadgetReorderDTO reqDTO) {
        Dashboard dashboard = dashboardRepository.findByIdWithGadgets(dashboardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        List<DashboardGadget> gadgets = dashboard.getGadgets();
        if (gadgets == null || gadgets.isEmpty()) {
            if (!reqDTO.getPositions().isEmpty()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            return DashboardResponse.DetailDTO.of(dashboard);
        }
        Set<Long> owned = new HashSet<>();
        for (DashboardGadget g : gadgets) {
            owned.add(g.getId());
        }
        if (reqDTO.getPositions().size() != owned.size()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        Set<Long> seen = new HashSet<>();
        for (DashboardRequest.GadgetPositionDTO slot : reqDTO.getPositions()) {
            if (!owned.contains(slot.getGadgetId()) || !seen.add(slot.getGadgetId())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }
        for (DashboardRequest.GadgetPositionDTO slot : reqDTO.getPositions()) {
            DashboardGadget g = gadgets.stream()
                    .filter(x -> Objects.equals(x.getId(), slot.getGadgetId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));
            g.setPosition(slot.getPosition());
        }
        return DashboardResponse.DetailDTO.of(dashboard);
    }

    @Transactional
    public void removeGadget(Long dashboardId, Long gadgetId) {
        DashboardGadget gadget = dashboardGadgetRepository.findByIdAndDashboard_Id(gadgetId, dashboardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DASHBOARD_GADGET_MISMATCH));
        dashboardGadgetRepository.delete(gadget);
    }
}
