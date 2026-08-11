package com.lecture.user.service;

import com.lecture.user.dto.UserDto;
import com.lecture.user.entity.PasswordResetToken;
import com.lecture.user.entity.User;
import com.lecture.user.repository.PasswordResetTokenRepository;
import com.lecture.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    /** 재설정 토큰의 유효 기간(분). */
    private static final long RESET_TOKEN_EXPIRY_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     */
    @Transactional
    public UserDto.UserResponse register(UserDto.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + request.getEmail());
        }

        User.Role role = request.getRole() != null ? request.getRole() : User.Role.STUDENT;

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(role)
                .build();

        User savedUser = userRepository.save(user);
        return UserDto.UserResponse.from(savedUser);
    }

    /**
     * 사용자 단건 조회
     */
    public UserDto.UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + id));
        return UserDto.UserResponse.from(user);
    }

    /**
     * 사용자 정보 수정 (이름, 이메일).
     * 대상이 없으면 빈 Optional을 반환하며, 컨트롤러에서 404로 매핑한다. 소유권 검증은 컨트롤러에서 선행.
     */
    @Transactional
    public Optional<UserDto.UserResponse> updateUser(Long id, UserDto.UpdateRequest request) {
        return userRepository.findById(id)
                .map(user -> {
                    user.updateProfile(request.getName(), request.getEmail());
                    return UserDto.UserResponse.from(user);
                });
    }

    /**
     * 이메일로 사용자 조회 (서비스 간 내부 호출용)
     */
    public UserDto.UserResponse getUserByEmail(String email) {
        System.out.println(">>> getUserByEmail email = " + email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));
        return UserDto.UserResponse.from(user);
    }

    /**
     * 비밀번호 재설정 토큰 발급.
     * 이메일+이름이 모두 일치하는 계정이 있을 때만 토큰을 만들어 반환한다. 일치하지 않아도 예외를 던지지
     * 않고 빈 Optional을 반환한다 — "이 이메일로 가입되어 있다/없다"를 응답으로 구분할 수 있으면 계정
     * 존재 여부를 캐내는 사용자 열거(user enumeration) 공격에 악용될 수 있으므로, 컨트롤러는 매칭 실패
     * 시에도 매칭 성공 시와 동일한 HTTP 200으로 응답한다.
     *
     * 데모 모드: 운영 환경이라면 이 토큰/링크를 가입 이메일로 발송하고 응답 본문에는 담지 않아야 한다.
     * SMTP 연동이 없는 실습 환경이라 흐름을 시연할 수 있도록 토큰을 응답으로 그대로 반환한다.
     */
    @Transactional
    public Optional<UserDto.PasswordResetTokenResponse> requestPasswordReset(UserDto.PasswordResetRequest request) {
        Optional<User> matchedUser = userRepository.findByEmail(request.getEmail())
                .filter(user -> user.getName().equals(request.getName()));

        if (matchedUser.isEmpty()) {
            return Optional.empty();
        }

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .userId(matchedUser.get().getId())
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRY_MINUTES))
                .build();
        passwordResetTokenRepository.save(resetToken);

        return Optional.of(UserDto.PasswordResetTokenResponse.builder()
                .resetToken(token)
                .resetUrl("/reset-password?token=" + token)
                .build());
    }

    /**
     * 비밀번호 재설정 확정.
     * 토큰이 존재하고, 미사용이며, 만료 전일 때만 비밀번호를 변경한다. 검증에 실패하면
     * IllegalArgumentException을 던져 GlobalExceptionHandler가 400으로 매핑하게 한다
     * (register 등 기존 검증 실패 처리와 동일한 관례).
     */
    @Transactional
    public void confirmPasswordReset(UserDto.PasswordResetConfirmRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndUsedFalse(request.getToken())
                .filter(t -> !t.isExpired())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않거나 만료된 링크입니다."));

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않거나 만료된 링크입니다."));

        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
        resetToken.markUsed();
    }
}
