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
public class QuestionResponseDTO {
    private Integer id;
    private Integer questionId;
    private String responseText;
    private String audioFilePath;
    private String selectedOption;
    private Double score;
    private String feedback;
    private LocalDateTime createdAt;
}
