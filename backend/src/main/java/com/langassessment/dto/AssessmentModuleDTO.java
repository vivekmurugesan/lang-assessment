package com.langassessment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentModuleDTO {
    private Integer id;
    private Integer assessmentId;
    private String moduleType;
    private Integer numQuestions;
    private String difficultyLevel;
    private Boolean isEnabled;
    private LocalDateTime createdAt;
}
