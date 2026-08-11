package com.lecture.user.dto;

import com.lecture.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class UserDto {

    /**
     * 비밀번호 정책: 8자 이상 · 영문 대문자 1개 이상 · 특수문자 1개 이상.
     * 전방탐색으로 각 조건을 독립 검사한다 — (?=.*[A-Z]) 대문자, (?=.*[^A-Za-z0-9]) 특수문자, .{8,} 길이.
     * 프론트엔드 utils/password.js 의 규칙과 반드시 동일하게 유지한다(한쪽만 바꾸면 검증이 어긋난다).
     */
    private static final String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{8,}$";
    private static final String PASSWORD_MESSAGE =
            "비밀번호는 8자 이상이며 영문 대문자와 특수문자를 각각 1개 이상 포함해야 합니다";

    // 회원가입 요청
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegisterRequest {
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다")
        @Pattern(regexp = PASSWORD_PATTERN, message = PASSWORD_MESSAGE)
        private String password;

        @NotBlank(message = "이름은 필수입니다")
        private String name;

        private User.Role role; // STUDENT or INSTRUCTOR
    }

    /** 사용자 정보 수정 요청. 이름/이메일만 변경 가능(비밀번호·역할은 별도 API). */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        @NotBlank(message = "이름은 필수입니다")
        private String name;

        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;
    }

    /** 비밀번호 재설정 요청(1단계). 토큰이 없는 상태에서 호출되므로 이메일+이름으로 본인 확인한다. */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PasswordResetRequest {
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;

        @NotBlank(message = "이름은 필수입니다")
        private String name;
    }

    /** 비밀번호 재설정 확정 요청(2단계). 발급받은 토큰과 새 비밀번호를 담는다. */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PasswordResetConfirmRequest {
        @NotBlank(message = "토큰은 필수입니다")
        private String token;

        @NotBlank(message = "새 비밀번호는 필수입니다")
        @Pattern(regexp = PASSWORD_PATTERN, message = PASSWORD_MESSAGE)
        private String newPassword;
    }

    /**
     * 재설정 토큰 발급 응답.
     * 데모 모드 전용 필드 — 운영 환경이라면 이 값을 응답으로 내려주지 않고 가입 이메일로 발송해야 한다.
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PasswordResetTokenResponse {
        private String resetToken;
        private String resetUrl;
    }

    // 사용자 정보 응답
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserResponse {
        private Long id;
        private String email;
        private String name;
        private User.Role role;
        private LocalDateTime createdAt;

        public static UserResponse from(User user) {
            return UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(user.getRole())
                    .createdAt(user.getCreatedAt())
                    .build();
        }
    }

    // 공통 API 응답 래퍼
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public static <T> ApiResponse<T> success(T data) {
            return ApiResponse.<T>builder()
                    .success(true)
                    .message("성공")
                    .data(data)
                    .build();
        }

        public static <T> ApiResponse<T> error(String message) {
            return ApiResponse.<T>builder()
                    .success(false)
                    .message(message)
                    .build();
        }
    }
}
