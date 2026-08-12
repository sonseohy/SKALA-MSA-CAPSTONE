package com.lecture.enrollment.repository;

import com.lecture.enrollment.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByUserId(Long userId);

    // 내 수강 목록 서버 페이징 (status 미지정 / 지정)
    Page<Enrollment> findByUserId(Long userId, Pageable pageable);

    Page<Enrollment> findByUserIdAndStatus(Long userId, Enrollment.Status status, Pageable pageable);

    // 요약 카드용 집계 - 현재 페이지와 무관하게 전체 기준으로 센다
    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, Enrollment.Status status);

    List<Enrollment> findByUserIdAndStatus(Long userId, Enrollment.Status status);

    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    // 수강 완료(ACTIVE)된 강의 ID 목록 - 추천 서비스용
    List<Enrollment> findByUserIdAndStatusIn(Long userId, List<Enrollment.Status> statuses);
}
