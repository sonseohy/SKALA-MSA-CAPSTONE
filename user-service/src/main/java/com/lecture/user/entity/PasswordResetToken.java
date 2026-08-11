package com.lecture.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 비밀번호 재설정용 1회성 토큰.
 * 발급 시점으로부터 일정 시간(서비스단 상수 참고) 이내, 미사용 상태에서만 유효하다.
 * 사용 완료 또는 만료 후에는 재사용할 수 없다.
 */
@Entity
@Table(name = "password_reset_tokens")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean used = false;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /** 토큰을 소비 상태로 표시한다(1회성 보장). */
    public void markUsed() {
        this.used = true;
    }

    /** 만료 시각이 지났는지 확인한다. */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}
