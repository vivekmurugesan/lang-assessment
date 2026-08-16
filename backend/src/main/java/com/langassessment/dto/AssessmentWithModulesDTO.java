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
public class AssessmentWithModulesDTO {
    private Integer id;
    private String title;
    private String description;
    private Integer languageId;
    private String languageName;
    private String status;
    private List<AssessmentModuleDTO> modules;
    private LocalDateTime createdAt;
}
