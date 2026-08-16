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
public class QuestionWithOptionsDTO {
    private Integer id;
    private String moduleType;
    private String cefrLevel;
    private String questionText;
    private Integer questionNumber;
    private String audioUrl;
    private String imageUrl;
    private String questionOptionsUri;
    private String explanationUri;
    private List<String> options;
    private LocalDateTime createdAt;
}
