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
public class QuestionWithOptionsDTO {
    private Integer id;
    private String moduleType;
    private String cefrLevel;
    private String questionText;
    private Integer questionNumber;
    private LocalDateTime createdAt;
}
