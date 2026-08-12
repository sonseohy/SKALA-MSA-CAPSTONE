package com.lecture.course.service;

import com.lecture.course.dto.CourseDto;
import com.lecture.course.entity.Course;
import com.lecture.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;

    /**
     * 강의 등록 (강사만 가능 - SecurityConfig에서 role 검증)
     */
    @Transactional
    public CourseDto.CourseResponse createCourse(CourseDto.CreateRequest request, Long instructorId) {
        validateSchedule(request);

        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .price(request.getPrice())
                .durationDays(resolveDurationDays(request))
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .deliveryType(request.getDeliveryType() != null ? request.getDeliveryType() : Course.DeliveryType.TBD)
                .targetAudience(request.getTargetAudience())
                .region(request.getRegion())
                .difficulty(request.getDifficulty() != null ? request.getDifficulty() : Course.Difficulty.AUTO)
                .instructorId(instructorId)
                .build();

        return CourseDto.CourseResponse.from(courseRepository.save(course));
    }

    /**
     * 강의 단건 조회
     */
    public CourseDto.CourseResponse getCourse(Long id) {
        Course course = findCourseById(id);
        return CourseDto.CourseResponse.from(course);
    }

    /**
     * 전체 활성 강의 목록 조회
     */
    public List<CourseDto.CourseResponse> getAllCourses() {
        return courseRepository.findByStatus(Course.Status.ACTIVE).stream()
                .map(CourseDto.CourseResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 활성 강의 목록 - 서버 페이징 + 선택적 필터
     * - 필터는 전부 optional. 지정된 것만 AND로 조립한다.
     * - 정렬은 호출부(Controller)가 Pageable에 실어 전달한다.
     */
    public CourseDto.PageResult<CourseDto.CourseResponse> getCourses(
            Course.Category category,
            Course.DeliveryType deliveryType,
            String keyword,
            String region,
            Pageable pageable) {

        Specification<Course> spec =
                (root, query, cb) -> cb.equal(root.get("status"), Course.Status.ACTIVE);

        if (category != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), category));
        }
        if (deliveryType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("deliveryType"), deliveryType));
        }
        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("targetAudience")), pattern)));
        }
        if (region != null && !region.isBlank()) {
            String pattern = "%" + region.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("region")), pattern));
        }

        return CourseDto.PageResult.from(
                courseRepository.findAll(spec, pageable).map(CourseDto.CourseResponse::from));
    }

    /**
     * 카테고리별 강의 조회
     */
    public List<CourseDto.CourseResponse> getCoursesByCategory(Course.Category category) {
        return courseRepository.findByCategoryAndStatus(category, Course.Status.ACTIVE).stream()
                .map(CourseDto.CourseResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 강의 존재 여부 확인 (Enrollment Service → Course Service REST 호출용)
     */
    public boolean existsCourse(Long id) {
        return courseRepository.existsById(id);
    }

    /**
     * 수강생 수 증가 (Enrollment Service 수강 활성화 시 호출)
     */
    @Transactional
    public void increaseEnrollmentCount(Long courseId) {
        Course course = findCourseById(courseId);
        course.increaseEnrollmentCount();
    }

    /**
     * 추천 서비스용: 카테고리별 미수강 강의 조회
     * - excludeCourseIds: 이미 수강한 강의 ID 목록
     */
    public List<CourseDto.CourseResponse> getRecommendCourses(
            Course.Category category, List<Long> excludeCourseIds) {

        List<Course> courses = excludeCourseIds.isEmpty()
                ? courseRepository.findByCategoryAndStatus(category, Course.Status.ACTIVE)
                : courseRepository.findByCategoryAndStatusAndIdNotIn(
                        category, Course.Status.ACTIVE, excludeCourseIds);

        // 수강생 수 기준 내림차순 정렬
        return courses.stream()
                .sorted((a, b) -> b.getEnrollmentCount() - a.getEnrollmentCount())
                .map(CourseDto.CourseResponse::from)
                .collect(Collectors.toList());
    }

    private Course findCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다: " + id));
    }

    private void validateSchedule(CourseDto.CreateRequest request) {
        if (request.getStartDate() != null
                && request.getEndDate() != null
                && request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("교육 종료일은 시작일 이후여야 합니다");
        }
    }

    private Integer resolveDurationDays(CourseDto.CreateRequest request) {
        if (request.getDurationDays() != null) {
            return request.getDurationDays();
        }

        if (request.getStartDate() != null && request.getEndDate() != null) {
            return Math.toIntExact(ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1);
        }

        return null;
    }
}
