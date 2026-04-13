package com.pch.mng.dashboard;

import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import com.pch.mng.user.UserAccount;
import com.pch.mng.user.UserAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private DashboardRepository dashboardRepository;
    @Mock
    private DashboardGadgetRepository dashboardGadgetRepository;
    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("findById: 없으면 ENTITY_NOT_FOUND")
    void findByIdMissing() {
        when(dashboardRepository.findByIdWithGadgets(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> dashboardService.findById(99L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("findById: 비공개·타인 소유면 FORBIDDEN")
    void findByIdForbidden() {
        UserAccount owner = UserAccount.builder().email("o@x.com").password("p").name("O").build();
        ReflectionTestUtils.setField(owner, "id", 2L);
        Dashboard d = Dashboard.builder().name("Priv").owner(owner).shared(false).build();
        ReflectionTestUtils.setField(d, "id", 1L);
        when(dashboardRepository.findByIdWithGadgets(1L)).thenReturn(Optional.of(d));

        assertThatThrownBy(() -> dashboardService.findById(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("findById: 공유 대시보드는 비소유자 조회 가능")
    void findByIdSharedOk() {
        UserAccount owner = UserAccount.builder().email("o@x.com").password("p").name("O").build();
        ReflectionTestUtils.setField(owner, "id", 2L);
        Dashboard d = Dashboard.builder().name("Team").owner(owner).shared(true).build();
        ReflectionTestUtils.setField(d, "id", 1L);
        d.setGadgets(new ArrayList<>());
        when(dashboardRepository.findByIdWithGadgets(1L)).thenReturn(Optional.of(d));

        DashboardResponse.DetailDTO dto = dashboardService.findById(1L, 1L);
        assertThat(dto.getName()).isEqualTo("Team");
        assertThat(dto.isShared()).isTrue();
    }

    @Test
    @DisplayName("addGadget: 알 수 없는 gadgetType이면 INVALID_INPUT_VALUE")
    void addGadgetInvalidType() {
        DashboardRequest.GadgetDTO req = new DashboardRequest.GadgetDTO();
        req.setGadgetType("UNKNOWN_WIDGET");
        req.setPosition(0);

        assertThatThrownBy(() -> dashboardService.addGadget(1L, req))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("addGadget: 유효 타입이면 저장")
    void addGadgetOk() {
        UserAccount owner = UserAccount.builder().email("o@x.com").password("p").name("O").build();
        Dashboard d = Dashboard.builder().name("My").owner(owner).shared(false).build();
        ReflectionTestUtils.setField(d, "id", 1L);
        d.setGadgets(new ArrayList<>());

        DashboardRequest.GadgetDTO req = new DashboardRequest.GadgetDTO();
        req.setGadgetType(DashboardGadgetType.BURNDOWN.name());
        req.setPosition(2);
        req.setConfigJson("{\"projectId\":5}");

        when(dashboardRepository.findByIdWithGadgets(1L)).thenReturn(Optional.of(d));

        DashboardResponse.DetailDTO res = dashboardService.addGadget(1L, req);
        assertThat(res.getGadgets()).hasSize(1);
        assertThat(res.getGadgets().get(0).getGadgetType()).isEqualTo(DashboardGadgetType.BURNDOWN.name());
        assertThat(res.getGadgets().get(0).getPosition()).isEqualTo(2);
    }

    @Test
    @DisplayName("addGadget: 웹 UI 프리셋 TEXT_NOTE 허용")
    void addGadgetTextNoteOk() {
        UserAccount owner = UserAccount.builder().email("o@x.com").password("p").name("O").build();
        Dashboard d = Dashboard.builder().name("My").owner(owner).shared(false).build();
        ReflectionTestUtils.setField(d, "id", 1L);
        d.setGadgets(new ArrayList<>());

        DashboardRequest.GadgetDTO req = new DashboardRequest.GadgetDTO();
        req.setGadgetType("TEXT_NOTE");
        req.setPosition(0);
        req.setConfigJson("{\"text\":\"hi\"}");

        when(dashboardRepository.findByIdWithGadgets(1L)).thenReturn(Optional.of(d));

        DashboardResponse.DetailDTO res = dashboardService.addGadget(1L, req);
        assertThat(res.getGadgets()).hasSize(1);
        assertThat(res.getGadgets().get(0).getGadgetType()).isEqualTo("TEXT_NOTE");
    }

    @Test
    @DisplayName("removeGadget: 다른 대시보드 가젯이면 DASHBOARD_GADGET_MISMATCH")
    void removeGadgetWrongDashboard() {
        when(dashboardGadgetRepository.findByIdAndDashboard_Id(9L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dashboardService.removeGadget(1L, 9L))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.DASHBOARD_GADGET_MISMATCH);
    }

    @Test
    @DisplayName("removeGadget: 소속 일치 시 삭제")
    void removeGadgetOk() {
        DashboardGadget g = DashboardGadget.builder()
                .gadgetType("BURNDOWN")
                .position(0)
                .build();
        ReflectionTestUtils.setField(g, "id", 9L);
        when(dashboardGadgetRepository.findByIdAndDashboard_Id(9L, 1L)).thenReturn(Optional.of(g));

        dashboardService.removeGadget(1L, 9L);
        verify(dashboardGadgetRepository).delete(g);
    }

    @Test
    @DisplayName("updateGadget: 변경 필드 없으면 INVALID_INPUT_VALUE")
    void updateGadgetEmpty() {
        DashboardRequest.GadgetUpdateDTO req = new DashboardRequest.GadgetUpdateDTO();

        assertThatThrownBy(() -> dashboardService.updateGadget(1L, 9L, req))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("updateGadget: 위치·설정 반영")
    void updateGadgetOk() {
        UserAccount owner = UserAccount.builder().email("o@x.com").password("p").name("O").build();
        Dashboard d = Dashboard.builder().name("My").owner(owner).shared(false).build();
        ReflectionTestUtils.setField(d, "id", 1L);

        DashboardGadget g = DashboardGadget.builder()
                .dashboard(d)
                .gadgetType("VELOCITY")
                .position(0)
                .configJson("{}")
                .build();
        ReflectionTestUtils.setField(g, "id", 9L);

        when(dashboardGadgetRepository.findByIdAndDashboard_Id(9L, 1L)).thenReturn(Optional.of(g));
        when(dashboardRepository.findByIdWithGadgets(1L)).thenReturn(Optional.of(d));

        DashboardRequest.GadgetUpdateDTO req = new DashboardRequest.GadgetUpdateDTO();
        req.setPosition(5);
        req.setConfigJson("{\"sprintId\":3}");

        DashboardResponse.DetailDTO res = dashboardService.updateGadget(1L, 9L, req);
        assertThat(g.getPosition()).isEqualTo(5);
        assertThat(g.getConfigJson()).isEqualTo("{\"sprintId\":3}");
        assertThat(res.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("reorderGadgets: 가젯 수와 항목 수 불일치 시 오류")
    void reorderMismatch() {
        UserAccount owner = UserAccount.builder().email("o@x.com").password("p").name("O").build();
        Dashboard d = Dashboard.builder().name("My").owner(owner).shared(false).build();
        DashboardGadget g1 = DashboardGadget.builder().dashboard(d).gadgetType("CFD").position(0).build();
        ReflectionTestUtils.setField(g1, "id", 10L);
        d.setGadgets(new ArrayList<>(List.of(g1)));

        when(dashboardRepository.findByIdWithGadgets(1L)).thenReturn(Optional.of(d));

        DashboardRequest.GadgetReorderDTO req = new DashboardRequest.GadgetReorderDTO();
        DashboardRequest.GadgetPositionDTO p = new DashboardRequest.GadgetPositionDTO();
        p.setGadgetId(10L);
        p.setPosition(0);
        DashboardRequest.GadgetPositionDTO p2 = new DashboardRequest.GadgetPositionDTO();
        p2.setGadgetId(99L);
        p2.setPosition(1);
        req.setPositions(List.of(p, p2));

        assertThatThrownBy(() -> dashboardService.reorderGadgets(1L, req))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("reorderGadgets: 전체 가젯 위치 갱신")
    void reorderOk() {
        UserAccount owner = UserAccount.builder().email("o@x.com").password("p").name("O").build();
        Dashboard d = Dashboard.builder().name("My").owner(owner).shared(false).build();
        DashboardGadget g1 = DashboardGadget.builder().dashboard(d).gadgetType("CFD").position(0).build();
        ReflectionTestUtils.setField(g1, "id", 10L);
        DashboardGadget g2 = DashboardGadget.builder().dashboard(d).gadgetType("BURNDOWN").position(1).build();
        ReflectionTestUtils.setField(g2, "id", 11L);
        d.setGadgets(new ArrayList<>(List.of(g1, g2)));

        when(dashboardRepository.findByIdWithGadgets(1L)).thenReturn(Optional.of(d));

        DashboardRequest.GadgetReorderDTO req = new DashboardRequest.GadgetReorderDTO();
        DashboardRequest.GadgetPositionDTO p1 = new DashboardRequest.GadgetPositionDTO();
        p1.setGadgetId(11L);
        p1.setPosition(0);
        DashboardRequest.GadgetPositionDTO p2 = new DashboardRequest.GadgetPositionDTO();
        p2.setGadgetId(10L);
        p2.setPosition(1);
        req.setPositions(List.of(p1, p2));

        dashboardService.reorderGadgets(1L, req);
        assertThat(g1.getPosition()).isEqualTo(1);
        assertThat(g2.getPosition()).isEqualTo(0);
    }
}
