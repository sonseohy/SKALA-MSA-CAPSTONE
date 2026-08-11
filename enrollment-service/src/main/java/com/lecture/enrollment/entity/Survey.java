package com.lecture.enrollment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 수강 건(enrollment) 1건에 대한 만족도 설문 응답.
 * enrollmentId+userId 유니크 제약으로 사용자당 1건만 존재하며, 재제출은 upsert(수정)로 처리한다.
 */
@Entity
@Table(name = "surveys",
       uniqueConstraints = @UniqueConstraint(columnNames = {"enrollment_id", "user_id"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enrollment_id", nullable = false)
    private Long enrollmentId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "education_score", nullable = false)
    private int educationScore;

    @Column(name = "instructor_score", nullable = false)
    private int instructorScore;

    @Column(name = "usefulness_score", nullable = false)
    private int usefulnessScore;

    @Column(name = "difficulty_score", nullable = false)
    private int difficultyScore;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /** 기존 만족도 응답을 새 점수/코멘트로 덮어쓴다 (재제출 시 upsert의 수정 경로). */
    public void update(int educationScore, int instructorScore, int usefulnessScore,
                        int difficultyScore, String comment) {
        this.educationScore = educationScore;
        this.instructorScore = instructorScore;
        this.usefulnessScore = usefulnessScore;
        this.difficultyScore = difficultyScore;
        this.comment = comment;
    }
}
