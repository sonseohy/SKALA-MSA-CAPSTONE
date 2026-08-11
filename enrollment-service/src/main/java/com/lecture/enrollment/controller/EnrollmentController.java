package com.lecture.enrollment.controller;

import com.lecture.enrollment.dto.EnrollmentDto;
import com.lecture.enrollment.dto.SurveyDto;
import com.lecture.enrollment.service.EnrollmentService;
import com.lecture.enrollment.service.SurveyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final SurveyService surveyService;

    /**
     * POST /enrollments - 수강신청
     * Gateway에서 X-User-Id 헤더로 사용자 ID 전달
     */
    @PostMapping
    public ResponseEntity<EnrollmentDto.ApiResponse<EnrollmentDto.EnrollmentResponse>> enroll(
            @Valid @RequestBody EnrollmentDto.EnrollRequest request,
            @RequestHeader("X-User-Id") Long userId) {

        EnrollmentDto.EnrollmentResponse response =
                enrollmentService.enroll(userId, request.getCourseId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EnrollmentDto.ApiResponse.success(response));
    }

    /**
     * GET /enrollments/my - 내 수강 목록 조회
     * Gateway가 전달한 X-User-Id 헤더를 사용
     */
    @GetMapping("/my")
    public ResponseEntity<EnrollmentDto.ApiResponse<List<EnrollmentDto.EnrollmentResponse>>> getMyEnrollments(
            @RequestHeader("X-User-Id") Long userId) {

        List<EnrollmentDto.EnrollmentResponse> response =
                enrollmentService.getEnrollmentsByUser(userId);
        return ResponseEntity.ok(EnrollmentDto.ApiResponse.success(response));
    }

    /**
     * GET /enrollments/user/{userId} - 특정 사용자 수강 목록 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<EnrollmentDto.ApiResponse<List<EnrollmentDto.EnrollmentResponse>>> getEnrollments(
            @PathVariable Long userId) {

        List<EnrollmentDto.EnrollmentResponse> response =
                enrollmentService.getEnrollmentsByUser(userId);
        return ResponseEntity.ok(EnrollmentDto.ApiResponse.success(response));
    }

    /**
     * GET /enrollments/internal/history/{userId} - 수강 이력 조회 (Recommend Service용)
     */
    @GetMapping("/internal/history/{userId}")
    public ResponseEntity<EnrollmentDto.EnrollmentHistoryResponse> getEnrollmentHistory(
            @PathVariable Long userId) {

        return ResponseEntity.ok(enrollmentService.getEnrollmentHistory(userId));
    }

    /**
     * POST /enrollments/{enrollmentId}/survey - 만족도 등록/수정 (Upsert)
     * Gateway에서 X-User-Id 헤더로 사용자 ID 전달. 본인 소유 + ACTIVE 상태 검증은 서비스 계층에서 수행.
     */
    @PostMapping("/{enrollmentId}/survey")
    public ResponseEntity<EnrollmentDto.ApiResponse<SurveyDto.SurveyResponse>> submitSurvey(
            @PathVariable Long enrollmentId,
            @Valid @RequestBody SurveyDto.SurveyRequest request,
            @RequestHeader("X-User-Id") Long userId) {

        SurveyDto.SurveyResponse response = surveyService.submitSurvey(userId, enrollmentId, request);
        return ResponseEntity.ok(EnrollmentDto.ApiResponse.success(response));
    }

    /**
     * GET /enrollments/{enrollmentId}/survey - 내 만족도 조회
     * 미제출(빈 Optional)은 404로 응답 — 클라이언트가 "아직 설문 안 함"과 서버 오류를 구분할 수 있도록.
     */
    @GetMapping("/{enrollmentId}/survey")
    public ResponseEntity<EnrollmentDto.ApiResponse<SurveyDto.SurveyResponse>> getSurvey(
            @PathVariable Long enrollmentId,
            @RequestHeader("X-User-Id") Long userId) {

        return surveyService.getMySurvey(userId, enrollmentId)
                .map(response -> ResponseEntity.ok(EnrollmentDto.ApiResponse.success(response)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(EnrollmentDto.ApiResponse.error("등록된 만족도가 없습니다")));
    }

    /**
     * GET /enrollments/courses/{courseId}/surveys/summary - 강의별 만족도 평균 점수 집계
     */
    @GetMapping("/courses/{courseId}/surveys/summary")
    public ResponseEntity<EnrollmentDto.ApiResponse<SurveyDto.SurveySummaryResponse>> getSurveySummary(
            @PathVariable Long courseId) {

        SurveyDto.SurveySummaryResponse response = surveyService.getSurveySummary(courseId);
        return ResponseEntity.ok(EnrollmentDto.ApiResponse.success(response));
    }
}