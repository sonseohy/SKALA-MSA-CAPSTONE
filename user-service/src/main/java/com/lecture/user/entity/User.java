package com.lecture.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum Role {
        STUDENT, INSTRUCTOR
    }

    /** 이름·이메일만 변경한다(비밀번호·역할은 이 메서드로 바꾸지 않는다). */
    public void updateProfile(String name, String email) {
        this.name = name;
        this.email = email;
    }

    /** 비밀번호를 변경한다. 이미 인코딩된 값을 받는다(평문을 이 메서드에 넘기지 않는다). */
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
