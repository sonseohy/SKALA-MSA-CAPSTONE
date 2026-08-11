package com.lecture.enrollment.repository;

import com.lecture.enrollment.entity.Survey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SurveyRepository extends JpaRepository<Survey, Long> {

    /** upsert 대상 판별(이미 제출했는지) 및 본인 조회에 사용. 유니크 제약(enrollment_id, user_id)과 짝을 이룬다. */
    Optional<Survey> findByEnrollmentIdAndUserId(Long enrollmentId, Long userId);

    /** 강의별 만족도 집계 계산용 원본 목록 조회. */
    List<Survey> findByCourseId(Long courseId);
}
