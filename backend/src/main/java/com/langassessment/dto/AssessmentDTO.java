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
public class AssessmentDTO {

    private Integer id;
    private String title;
    private String description;
    private Integer languageId;
    private String languageName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
    private List<AssessmentModuleDTO> modules;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssessmentModuleDTO {
        private Integer id;
        private String moduleType;
        private Integer numQuestions;
        private String difficultyLevel;
        private Boolean isEnabled;
    }
}
