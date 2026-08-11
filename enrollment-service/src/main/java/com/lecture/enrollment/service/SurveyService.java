package com.lecture.enrollment.service;

import com.lecture.enrollment.dto.SurveyDto;
import com.lecture.enrollment.entity.Enrollment;
import com.lecture.enrollment.entity.Survey;
import com.lecture.enrollment.repository.EnrollmentRepository;
import com.lecture.enrollment.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final EnrollmentRepository enrollmentRepository;

    /**
     * 만족도 등록/수정 (Upsert).
     * 본인 소유 + ACTIVE 상태(결제 완료)인 수강 건만 허용한다 — 취소/대기 건이나 남의 수강 건은 거부.
     * 동일 enrollment+user 조합의 기존 응답이 있으면 새 값으로 덮어쓰고, 없으면 새로 만든다.
     */
    @Transactional
    public SurveyDto.SurveyResponse submitSurvey(Long userId, Long enrollmentId, SurveyDto.SurveyRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("수강 정보를 찾을 수 없습니다: " + enrollmentId));

        if (!enrollment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 수강 정보만 만족도를 등록할 수 있습니다");
        }

        if (enrollment.getStatus() != Enrollment.Status.ACTIVE) {
            throw new IllegalArgumentException("결제가 완료된 수강 건만 만족도를 등록할 수 있습니다");
        }

        Survey survey = surveyRepository.findByEnrollmentIdAndUserId(enrollmentId, userId)
                .map(existing -> {
                    existing.update(request.getEducationScore(), request.getInstructorScore(),
                            request.getUsefulnessScore(), request.getDifficultyScore(), request.getComment());
                    return existing;
                })
                .orElseGet(() -> Survey.builder()
                        .enrollmentId(enrollmentId)
                        .courseId(enrollment.getCourseId())
                        .userId(userId)
                        .educationScore(request.getEducationScore())
                        .instructorScore(request.getInstructorScore())
                        .usefulnessScore(request.getUsefulnessScore())
                        .difficultyScore(request.getDifficultyScore())
                        .comment(request.getComment())
                        .build());

        Survey saved = surveyRepository.save(survey);
        log.info("[SurveyService] 만족도 저장 완료 - enrollmentId: {}, userId: {}", enrollmentId, userId);
        return SurveyDto.SurveyResponse.from(saved);
    }

    /**
     * 내 만족도 조회. 미제출이면 빈 Optional을 반환하며, 컨트롤러에서 404로 매핑한다.
     */
    public Optional<SurveyDto.SurveyResponse> getMySurvey(Long userId, Long enrollmentId) {
        return surveyRepository.findByEnrollmentIdAndUserId(enrollmentId, userId)
                .map(SurveyDto.SurveyResponse::from);
    }

    /**
     * 강의별 만족도 집계. 응답이 하나도 없으면 평균 계산(divide-by-zero) 대신 전부 0으로 반환한다.
     */
    public SurveyDto.SurveySummaryResponse getSurveySummary(Long courseId) {
        List<Survey> surveys = surveyRepository.findByCourseId(courseId);

        if (surveys.isEmpty()) {
            return SurveyDto.SurveySummaryResponse.builder()
                    .surveyCount(0)
                    .averageEducationScore(0)
                    .averageInstructorScore(0)
                    .averageUsefulnessScore(0)
                    .averageDifficultyScore(0)
                    .build();
        }

        return SurveyDto.SurveySummaryResponse.builder()
                .surveyCount(surveys.size())
                .averageEducationScore(surveys.stream().mapToInt(Survey::getEducationScore).average().orElse(0))
                .averageInstructorScore(surveys.stream().mapToInt(Survey::getInstructorScore).average().orElse(0))
                .averageUsefulnessScore(surveys.stream().mapToInt(Survey::getUsefulnessScore).average().orElse(0))
                .averageDifficultyScore(surveys.stream().mapToInt(Survey::getDifficultyScore).average().orElse(0))
                .build();
    }
}
