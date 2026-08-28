package com.langassessment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentSubmissionDTO {
    private Integer id;
    private Integer assessmentCandidateId;
    private String status;
    private Double totalScore;
    private String cefrLevel;
    private String evaluatorNotes;
    private String message;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private List<ModulePerformanceDTO> modulePerformance;
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
    private LocalDateTime evaluatedAt;
    private List<QuestionResponseDTO> responses;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ModulePerformanceDTO {
        private String moduleType;
        private Integer total;
        private Integer correct;
        private Double score;
    }
}
