package com.lecture.user.repository;

import com.lecture.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // 사용자 목록 조회 (역할 필터 / id 목록 + 역할 교집합)
    List<User> findByRole(User.Role role);
    List<User> findByIdInAndRole(List<Long> ids, User.Role role);
}
