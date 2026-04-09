package com.pch.mng.auth;

import com.pch.mng.global.response.ApiResponse;
import com.pch.mng.user.UserAccountRequest;
import com.pch.mng.user.UserAccountResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserAccountResponse.DetailDTO>> register(
            @Valid @RequestBody UserAccountRequest.JoinDTO dto) {
        return ResponseEntity.status(201).body(ApiResponse.created(authService.register(dto)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse.TokenDTO>> login(
            @Valid @RequestBody AuthRequest.LoginDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(dto)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse.TokenDTO>> refresh(
            @Valid @RequestBody AuthRequest.RefreshDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(authService.refresh(dto)));
    }
}
