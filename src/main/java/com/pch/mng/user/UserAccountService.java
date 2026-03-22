package com.pch.mng.user;

import com.pch.mng.global.exception.BusinessException;
import com.pch.mng.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;

    public List<UserAccountResponse.MinDTO> findAll() {
        return userAccountRepository.findAll().stream()
                .map(UserAccountResponse.MinDTO::of)
                .toList();
    }

    public UserAccountResponse.DetailDTO findById(Long id) {
        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserAccountResponse.DetailDTO.of(user);
    }

    @Transactional
    public UserAccountResponse.DetailDTO save(UserAccountRequest.JoinDTO reqDTO) {
        if (userAccountRepository.existsByEmail(reqDTO.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        UserAccount user = UserAccount.builder()
                .email(reqDTO.getEmail())
                .password(reqDTO.getPassword()) // TODO: BCrypt 암호화 적용 필요
                .name(reqDTO.getName())
                .build();
        userAccountRepository.save(user);
        return UserAccountResponse.DetailDTO.of(user);
    }

    @Transactional
    public UserAccountResponse.DetailDTO update(Long id, UserAccountRequest.UpdateDTO reqDTO) {
        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.setName(reqDTO.getName());
        return UserAccountResponse.DetailDTO.of(user);
    }

    @Transactional
    public void delete(Long id) {
        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        userAccountRepository.delete(user);
    }
}
