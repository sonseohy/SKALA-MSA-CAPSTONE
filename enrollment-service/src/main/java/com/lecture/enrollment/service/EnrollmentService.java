package com.lecture.enrollment.service;

import com.lecture.enrollment.dto.EnrollmentDto;
import com.lecture.enrollment.entity.Enrollment;
import com.lecture.enrollment.entity.Survey;
import com.lecture.enrollment.kafka.EnrollmentKafkaProducer;
import com.lecture.enrollment.kafka.KafkaEvent;
import com.lecture.enrollment.repository.EnrollmentRepository;
import com.lecture.enrollment.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final SurveyRepository surveyRepository;
    private final CourseServiceClient courseServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final EnrollmentKafkaProducer kafkaProducer;
    private final EnrollmentWriteService enrollmentWriteService;

    /**
     * 수강신청 전체 흐름
     * 1. 강의 존재 확인
     * 2. 중복 수강 확인
     * 3. Enrollment 생성 및 즉시 커밋 (PENDING)
     * 4. 결제 요청
     */
    public EnrollmentDto.EnrollmentResponse enroll(Long userId, Long courseId) {
        if (!courseServiceClient.existsCourse(courseId)) {
            throw new IllegalArgumentException("존재하지 않는 강의입니다: " + courseId);
        }

        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new IllegalArgumentException("이미 수강신청한 강의입니다");
        }

        Enrollment enrollment = enrollmentWriteService.createPendingEnrollment(userId, courseId);

        paymentServiceClient.requestPayment(userId, courseId, BigDecimal.valueOf(99000));

        log.info("[EnrollmentService] 수강신청 완료 (결제 대기) - enrollmentId: {}", enrollment.getId());
        return EnrollmentDto.EnrollmentResponse.from(enrollment);
    }

    /**
     * 수강 활성화
     */
    @Transactional
    public void activateEnrollment(Long userId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "수강 정보를 찾을 수 없습니다 - userId: " + userId + ", courseId: " + courseId));

        enrollment.activate();

        courseServiceClient.increaseEnrollmentCount(courseId);

        kafkaProducer.publishEnrollmentCompleted(
                KafkaEvent.EnrollmentCompletedEvent.builder()
                        .enrollmentId(enrollment.getId())
                        .userId(userId)
                        .courseId(courseId)
                        .build()
        );

        log.info("[EnrollmentService] 수강 활성화 완료 - enrollmentId: {}", enrollment.getId());
    }

    /**
     * 사용자 수강 목록 조회
     * - course-service에서 강의 상세 정보를 붙여서 반환
     */
    public List<EnrollmentDto.EnrollmentResponse> getEnrollmentsByUser(Long userId) {
        List<Enrollment> enrollments = enrollmentRepository.findByUserId(userId);

        return enrollments.stream()
                .map(enrollment -> EnrollmentDto.EnrollmentResponse.from(
                        enrollment, toCourseSummary(enrollment.getCourseId())))
                .collect(Collectors.toList());
    }

    /**
     * 내 수강 목록 - 서버 페이징 + 상태 필터 + 만족도 제출 여부
     * - status가 null이면 전체 상태를 대상으로 한다.
     * - surveySubmitted는 현재 페이지의 enrollmentId를 한 번에 조회해 판별한다(건별 조회 금지).
     */
    public EnrollmentDto.MyEnrollmentsResponse getMyEnrollments(
            Long userId, Enrollment.Status status, Pageable pageable) {

        Page<Enrollment> page = status == null
                ? enrollmentRepository.findByUserId(userId, pageable)
                : enrollmentRepository.findByUserIdAndStatus(userId, status, pageable);

        Set<Long> surveyedEnrollmentIds = findSurveyedEnrollmentIds(userId, page.getContent());

        List<EnrollmentDto.EnrollmentResponse> content = page.getContent().stream()
                .map(enrollment -> EnrollmentDto.EnrollmentResponse.from(
                        enrollment,
                        toCourseSummary(enrollment.getCourseId()),
                        surveyedEnrollmentIds.contains(enrollment.getId())))
                .collect(Collectors.toList());

        EnrollmentDto.MyEnrollmentsResponse.Summary summary =
                EnrollmentDto.MyEnrollmentsResponse.Summary.builder()
                        .active(enrollmentRepository.countByUserIdAndStatus(userId, Enrollment.Status.ACTIVE))
                        .pending(enrollmentRepository.countByUserIdAndStatus(userId, Enrollment.Status.PENDING))
                        .total(enrollmentRepository.countByUserId(userId))
                        .build();

        return EnrollmentDto.MyEnrollmentsResponse.builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .summary(summary)
                .build();
    }

    private Set<Long> findSurveyedEnrollmentIds(Long userId, List<Enrollment> enrollments) {
        if (enrollments.isEmpty()) {
            return Set.of();
        }

        List<Long> enrollmentIds = enrollments.stream()
                .map(Enrollment::getId)
                .collect(Collectors.toList());

        return surveyRepository.findByUserIdAndEnrollmentIdIn(userId, enrollmentIds).stream()
                .map(Survey::getEnrollmentId)
                .collect(Collectors.toSet());
    }

    /**
     * course-service의 강의 상세 응답을 목록 표시용 요약으로 변환.
     * 필드명이 camelCase/snake_case 어느 쪽으로도 올 수 있어 둘 다 받아준다.
     */
    private EnrollmentDto.CourseSummary toCourseSummary(Long courseId) {
        Map<String, Object> courseInfo = courseServiceClient.getCourse(courseId);

        return EnrollmentDto.CourseSummary.builder()
                .id(toLong(courseInfo.get("id")))
                .title((String) courseInfo.get("title"))
                .description((String) courseInfo.get("description"))
                .category(normalizeCategory((String) courseInfo.get("category")))
                .price(toInteger(courseInfo.get("price")))
                .durationDays(toInteger(
                        firstNonNullObject(
                                courseInfo.get("durationDays"),
                                courseInfo.get("duration_days")
                        )
                ))
                .startDate(toStringValue(
                        firstNonNullObject(
                                courseInfo.get("startDate"),
                                courseInfo.get("start_date")
                        )
                ))
                .endDate(toStringValue(
                        firstNonNullObject(
                                courseInfo.get("endDate"),
                                courseInfo.get("end_date")
                        )
                ))
                .deliveryType(toStringValue(
                        firstNonNullObject(
                                courseInfo.get("deliveryType"),
                                courseInfo.get("delivery_type")
                        )
                ))
                .targetAudience(toStringValue(
                        firstNonNullObject(
                                courseInfo.get("targetAudience"),
                                courseInfo.get("target_audience")
                        )
                ))
                .region(toStringValue(courseInfo.get("region")))
                .difficulty(toStringValue(courseInfo.get("difficulty")))
                .thumbnail((String) courseInfo.get("thumbnail"))
                .instructorName(
                        firstNonNull(
                                (String) courseInfo.get("instructorName"),
                                (String) courseInfo.get("teacherName"),
                                (String) courseInfo.get("instructor_name")
                        )
                )
                .enrollmentCount(toInteger(
                        firstNonNullObject(
                                courseInfo.get("enrollmentCount"),
                                courseInfo.get("enrollment_count")
                        )
                ))
                .build();
    }

    /**
     * 수강 이력 조회 - 추천 서비스용
     */
    public EnrollmentDto.EnrollmentHistoryResponse getEnrollmentHistory(Long userId) {
        List<Long> activeCourseIds = enrollmentRepository
                .findByUserIdAndStatus(userId, Enrollment.Status.ACTIVE)
                .stream()
                .map(Enrollment::getCourseId)
                .collect(Collectors.toList());

        return EnrollmentDto.EnrollmentHistoryResponse.builder()
                .userId(userId)
                .activeCourseIds(activeCourseIds)
                .build();
    }

    private String normalizeCategory(String category) {
        if (category == null) return null;

        return switch (category) {
            case "BACKEND" -> "백엔드";
            case "FRONTEND" -> "프론트엔드";
            case "DEVOPS" -> "DevOps";
            case "DATA" -> "데이터";
            case "AI" -> "AI";
            default -> category;
        };
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        return Long.parseLong(value.toString());
    }

    private Integer toInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.intValue();
        return Integer.parseInt(value.toString());
    }

    private String toStringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private String firstNonNull(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private Object firstNonNullObject(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
