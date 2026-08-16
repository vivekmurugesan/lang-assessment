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
public class GeneratedQuestionDTO {
    private Integer id;
    private String questionText;
    private String moduleType;
    private String cefrLevel;
    private List<String> options;
    private String correctAnswer;
    private String explanation;
    private String audioUrl;
    private String imageUrl;
    private String status;
    private LocalDateTime generatedAt;
    private String generatedBy;
    private String reviewNotes;
}
