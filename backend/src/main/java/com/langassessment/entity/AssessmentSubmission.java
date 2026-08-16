package com.langassessment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assessment_submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_candidate_id", nullable = false)
    private AssessmentCandidate assessmentCandidate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status;

    @Column(name = "total_score")
    private Double totalScore;

    @Column(name = "cefr_level", length = 20)
    private String cefrLevel;

    @Column(name = "evaluator_notes", columnDefinition = "TEXT")
    private String evaluatorNotes;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuestionResponse> responses = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt;

    public enum SubmissionStatus {
        IN_PROGRESS,
        SUBMITTED,
        EVALUATING,
        EVALUATED
    }
}
