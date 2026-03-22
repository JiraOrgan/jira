package com.jira.mng.user;

import com.jira.mng.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserAccountApiController {

    private final UserAccountService userAccountService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserAccountResponse.MinDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(userAccountService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserAccountResponse.DetailDTO>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userAccountService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserAccountResponse.DetailDTO>> save(
            @Valid @RequestBody UserAccountRequest.JoinDTO reqDTO) {
        return ResponseEntity.status(201).body(ApiResponse.created(userAccountService.save(reqDTO)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserAccountResponse.DetailDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody UserAccountRequest.UpdateDTO reqDTO) {
        return ResponseEntity.ok(ApiResponse.ok(userAccountService.update(id, reqDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        userAccountService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
