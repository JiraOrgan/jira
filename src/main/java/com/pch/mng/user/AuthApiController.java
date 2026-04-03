package com.pch.mng.user;

import com.pch.mng.global.response.ApiResponse;
import com.pch.mng.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthApiController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse.TokenDTO>> signup(
            @Valid @RequestBody AuthRequest.SignupDTO reqDTO) {
        AuthResponse.TokenDTO result = authService.signup(reqDTO);
        return ResponseEntity.status(201).body(ApiResponse.created(result));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse.TokenDTO>> login(
            @Valid @RequestBody AuthRequest.LoginDTO reqDTO) {
        AuthResponse.TokenDTO result = authService.login(reqDTO);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse.TokenDTO>> refresh(
            @Valid @RequestBody AuthRequest.RefreshDTO reqDTO) {
        AuthResponse.TokenDTO result = authService.refresh(reqDTO);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.logout(userDetails.getUserAccount().getId());
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
