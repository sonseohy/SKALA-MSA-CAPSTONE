package com.lecture.user.controller;

import com.lecture.user.dto.UserDto;
import com.lecture.user.entity.User;
import com.lecture.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * POST /users/register - 회원가입 (인증 불필요)
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto.ApiResponse<UserDto.UserResponse>> register(
            @Valid @RequestBody UserDto.RegisterRequest request) {
        UserDto.UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserDto.ApiResponse.success(response));
    }

    /**
     * GET /users - 사용자 목록 조회 (인증 필요)
     * ids: 콤마 구분 id 목록, role: STUDENT|INSTRUCTOR. 둘 다 선택 사항.
     */
    @GetMapping
    public ResponseEntity<UserDto.ApiResponse<List<UserDto.UserResponse>>> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(required = false) User.Role role) {
        return ResponseEntity.ok(UserDto.ApiResponse.success(userService.getUsers(ids, role)));
    }

    /**
     * GET /users/{id} - 사용자 조회 (인증 필요)
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto.ApiResponse<UserDto.UserResponse>> getUser(
            @PathVariable Long id) {
        UserDto.UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(UserDto.ApiResponse.success(response));
    }

    /**
     * PUT /users/{id} - 사용자 정보 수정 (이름, 이메일)
     * 본인 계정만 수정 가능 — 게이트웨이가 넣어준 X-User-Id 와 대상 id 가 다르면 403.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDto.ApiResponse<UserDto.UserResponse>> updateUser(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UserDto.UpdateRequest request) {
        if (!userId.equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(UserDto.ApiResponse.error("본인 계정만 수정할 수 있습니다."));
        }
        return userService.updateUser(id, request)
                .map(response -> ResponseEntity.ok(UserDto.ApiResponse.success(response)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(UserDto.ApiResponse.error("사용자를 찾을 수 없습니다: " + id)));
    }

    /**
     * GET /users/me - 내 정보 조회
     * API Gateway가 전달한 X-User-Id 헤더(숫자 userId)를 사용
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto.ApiResponse<UserDto.UserResponse>> getMe(
            @RequestHeader("X-User-Id") Long userId) {

        UserDto.UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(UserDto.ApiResponse.success(response));
    }

    /**
     * POST /users/password/reset-request - 비밀번호 재설정 토큰 발급 (인증 불필요)
     * 이메일+이름이 일치하는 계정이 없어도 매칭 성공 시와 동일하게 HTTP 200을 반환하고,
     * data 없이 안내 메시지만 내려준다 (사용자 열거 공격 방지 — UserService#requestPasswordReset 참고).
     */
    @PostMapping("/password/reset-request")
    public ResponseEntity<UserDto.ApiResponse<UserDto.PasswordResetTokenResponse>> requestPasswordReset(
            @Valid @RequestBody UserDto.PasswordResetRequest request) {
        Optional<UserDto.PasswordResetTokenResponse> result = userService.requestPasswordReset(request);
        UserDto.ApiResponse<UserDto.PasswordResetTokenResponse> body =
                UserDto.ApiResponse.<UserDto.PasswordResetTokenResponse>builder()
                        .success(true)
                        .message(result.isPresent()
                                ? "재설정 링크가 생성되었습니다."
                                : "입력하신 정보와 일치하는 계정이 있으면 재설정 링크를 보냈습니다.")
                        .data(result.orElse(null))
                        .build();
        return ResponseEntity.ok(body);
    }

    /**
     * POST /users/password/reset-confirm - 비밀번호 재설정 확정 (인증 불필요)
     * 토큰이 유효하지 않거나(존재하지 않음/이미 사용/만료) 하면 UserService에서 던진
     * IllegalArgumentException을 GlobalExceptionHandler가 400으로 매핑한다.
     */
    @PostMapping("/password/reset-confirm")
    public ResponseEntity<UserDto.ApiResponse<Void>> confirmPasswordReset(
            @Valid @RequestBody UserDto.PasswordResetConfirmRequest request) {
        userService.confirmPasswordReset(request);
        UserDto.ApiResponse<Void> body = UserDto.ApiResponse.<Void>builder()
                .success(true)
                .message("비밀번호가 재설정되었습니다.")
                .build();
        return ResponseEntity.ok(body);
    }

    /**
     * GET /users/internal/{id} - 서비스 간 내부 호출용 (Client Credentials)
     */
    @GetMapping("/internal/{id}")
    public ResponseEntity<UserDto.UserResponse> getUserInternal(@PathVariable Long id) {
        UserDto.UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }
}