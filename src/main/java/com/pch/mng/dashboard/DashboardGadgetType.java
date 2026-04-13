package com.pch.mng.dashboard;

/**
 * FR-021 역할별 대시보드 가젯 구성 시 클라이언트가 사용하는 타입 식별자.
 * {@code configJson}에 projectId·필터 등 부가 설정을 둔다.
 */
public enum DashboardGadgetType {

    /** 내게 할당된 이슈 */
    ASSIGNED_TO_ME,
    /** 최근 조회/변경 이슈 */
    RECENT_ISSUES,
    /** 내 미해결 이슈 요약 */
    MY_OPEN_ISSUES,
    /** 프로젝트 요약(멤버·스프린트 상태 등) */
    PROJECT_SUMMARY,
    /** 스프린트 진행률·남은 일수 */
    SPRINT_PROGRESS,
    /** 번다운(차트 데이터는 FR-022 API 연동) */
    BURNDOWN,
    /** 속도 */
    VELOCITY,
    /** 누적 플로우 */
    CFD,
    /** 저장 JQL 필터 결과 */
    FILTER_RESULTS;

    public static boolean isValid(String raw) {
        if (raw == null || raw.isBlank()) {
            return false;
        }
        try {
            valueOf(raw.trim());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
