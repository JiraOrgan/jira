package com.pch.mng.user;

import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserAccountService userAccountService;

    @Test
    @DisplayName("이미 존재하는 이메일이면 EMAIL_ALREADY_EXISTS")
    void saveRejectsDuplicateEmail() {
        UserAccountRequest.JoinDTO dto = new UserAccountRequest.JoinDTO();
        dto.setEmail("dup@ex.com");
        dto.setPassword("pw");
        dto.setName("N");

        when(userAccountRepository.existsByEmail("dup@ex.com")).thenReturn(true);

        assertThatThrownBy(() -> userAccountService.save(dto))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("가입 시 비밀번호 인코딩 후 저장")
    void saveEncodesPassword() {
        UserAccountRequest.JoinDTO dto = new UserAccountRequest.JoinDTO();
        dto.setEmail("new@ex.com");
        dto.setPassword("raw12");
        dto.setName("Neo");

        when(userAccountRepository.existsByEmail("new@ex.com")).thenReturn(false);
        when(passwordEncoder.encode("raw12")).thenReturn("ENC");

        userAccountService.save(dto);

        ArgumentCaptor<UserAccount> cap = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(cap.capture());
        assertThat(cap.getValue().getEmail()).isEqualTo("new@ex.com");
        assertThat(cap.getValue().getPassword()).isEqualTo("ENC");
        verify(passwordEncoder).encode("raw12");
    }
}
