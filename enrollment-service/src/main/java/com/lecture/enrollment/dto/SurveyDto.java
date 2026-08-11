package com.lecture.enrollment.dto;

import com.lecture.enrollment.entity.Survey;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

public class SurveyDto {

    /** 만족도 등록/수정 요청. 4개 항목 모두 1~5점 필수, 코멘트는 선택. */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SurveyRequest {
        @NotNull(message = "교육 만족도 점수는 필수입니다")
        @Min(value = 1, message = "점수는 1~5 사이여야 합니다")
        @Max(value = 5, message = "점수는 1~5 사이여야 합니다")
        private Integer educationScore;

        @NotNull(message = "강사 만족도 점수는 필수입니다")
        @Min(value = 1, message = "점수는 1~5 사이여야 합니다")
        @Max(value = 5, message = "점수는 1~5 사이여야 합니다")
        private Integer instructorScore;

        @NotNull(message = "유용성 점수는 필수입니다")
        @Min(value = 1, message = "점수는 1~5 사이여야 합니다")
        @Max(value = 5, message = "점수는 1~5 사이여야 합니다")
        private Integer usefulnessScore;

        @NotNull(message = "난이도 점수는 필수입니다")
        @Min(value = 1, message = "점수는 1~5 사이여야 합니다")
        @Max(value = 5, message = "점수는 1~5 사이여야 합니다")
        private Integer difficultyScore;

        private String comment;
    }

    /** 만족도 조회/등록 결과 응답. */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SurveyResponse {
        private Integer educationScore;
        private Integer instructorScore;
        private Integer usefulnessScore;
        private Integer difficultyScore;
        private String comment;

        public static SurveyResponse from(Survey survey) {
            return SurveyResponse.builder()
                    .educationScore(survey.getEducationScore())
                    .instructorScore(survey.getInstructorScore())
                    .usefulnessScore(survey.getUsefulnessScore())
                    .difficultyScore(survey.getDifficultyScore())
                    .comment(survey.getComment())
                    .build();
        }
    }

    /** 강의별 만족도 집계 응답. 응답이 없으면 surveyCount=0, 평균은 모두 0으로 채워진다. */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SurveySummaryResponse {
        private int surveyCount;
        private double averageEducationScore;
        private double averageInstructorScore;
        private double averageUsefulnessScore;
        private double averageDifficultyScore;
    }
}
