package com.pch.mng.user;

import com.pch.mng.global.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserAccountTest {

    private UserAccount user;

    @BeforeEach
    void setUp() {
        user = UserAccount.builder()
                .email("test@test.com")
                .password("encoded")
                .name("Tester")
                .role(UserRole.LEARNER)
                .build();
    }

    @Test
    @DisplayName("5회 실패 시 계정 잠금")
    void incrementFailedAttempts_locksAfter5() {
        for (int i = 0; i < 5; i++) {
            user.incrementFailedAttempts();
        }

        assertThat(user.isAccountLocked()).isTrue();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(5);
        assertThat(user.getLockedUntil()).isNotNull();
    }

    @Test
    @DisplayName("4회 실패 시 계정 잠금되지 않음")
    void incrementFailedAttempts_doesNotLockBefore5() {
        for (int i = 0; i < 4; i++) {
            user.incrementFailedAttempts();
        }

        assertThat(user.isAccountLocked()).isFalse();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(4);
        assertThat(user.getLockedUntil()).isNull();
    }

    @Test
    @DisplayName("잠금 해제 후 초기화")
    void resetFailedAttempts_unlocks() {
        for (int i = 0; i < 5; i++) {
            user.incrementFailedAttempts();
        }

        user.resetFailedAttempts();

        assertThat(user.isAccountLocked()).isFalse();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(0);
        assertThat(user.getLockedUntil()).isNull();
    }

    @Test
    @DisplayName("잠금되지 않은 계정은 isAccountLocked false")
    void isAccountLocked_returnsFalse_whenNotLocked() {
        assertThat(user.isAccountLocked()).isFalse();
    }

    @Test
    @DisplayName("role 미지정 시 기본 역할은 LEARNER")
    void defaultRole_isLearner() {
        UserAccount newUser = UserAccount.builder()
                .email("new@test.com")
                .password("pass")
                .name("New")
                .build();

        assertThat(newUser.getRole()).isEqualTo(UserRole.LEARNER);
    }

    @Test
    @DisplayName("role 지정 시 해당 역할로 설정")
    void explicitRole_isSet() {
        UserAccount admin = UserAccount.builder()
                .email("admin@test.com")
                .password("pass")
                .name("Admin")
                .role(UserRole.ADMIN)
                .build();

        assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("초기 상태에서 failedLoginAttempts는 0")
    void initialState_zeroFailedAttempts() {
        assertThat(user.getFailedLoginAttempts()).isEqualTo(0);
        assertThat(user.isLocked()).isFalse();
    }
}
