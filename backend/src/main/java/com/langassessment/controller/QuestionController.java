package com.langassessment.controller;

import com.langassessment.dto.AuthDTO;
import com.langassessment.dto.QuestionDTO;
import com.langassessment.entity.Question;
import com.langassessment.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/questions")
@RequiredArgsConstructor
@Slf4j
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    public ResponseEntity<AuthDTO.ApiResponse<QuestionDTO>> createQuestion(
            @RequestBody QuestionDTO dto) {
        try {
            log.info("Creating question for language: {}, module: {}", dto.getLanguageId(), dto.getModuleType());
            Question question = questionService.createQuestion(dto);
            QuestionDTO responseDto = QuestionDTO.builder()
                    .id(question.getId())
                    .languageId(question.getLanguage().getId())
                    .languageName(question.getLanguage().getName())
                    .moduleType(question.getModuleType().toString())
                    .cefrLevel(question.getCefrLevel())
                    .questionText(question.getQuestionText())
                    .questionNumber(question.getQuestionNumber())
                    .status(question.getStatus().toString())
                    .createdAt(question.getCreatedAt())
                    .build();
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Question created", responseDto));
        } catch (Exception e) {
            log.error("Failed to create question: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<AuthDTO.ApiResponse<Page<QuestionDTO>>> getQuestions(
            @RequestParam Integer languageId,
            @RequestParam String moduleType,
            Pageable pageable) {
        try {
            Page<QuestionDTO> questions = questionService.getQuestionsByLanguageAndModule(
                    languageId, moduleType, pageable);
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Questions retrieved", questions));
        } catch (Exception e) {
            log.error("Failed to retrieve questions: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @GetMapping("/by-cefr")
    public ResponseEntity<AuthDTO.ApiResponse<List<QuestionDTO>>> getQuestionsByCefrLevel(
            @RequestParam Integer languageId,
            @RequestParam String moduleType,
            @RequestParam String cefrLevel) {
        try {
            List<QuestionDTO> questions = questionService.getQuestionsByCefrLevel(
                    languageId, moduleType, cefrLevel);
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Questions retrieved", questions));
        } catch (Exception e) {
            log.error("Failed to retrieve questions by CEFR: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuthDTO.ApiResponse<QuestionDTO>> updateQuestion(
            @PathVariable Integer id,
            @RequestBody QuestionDTO dto) {
        try {
            Question question = questionService.updateQuestion(id, dto);
            QuestionDTO responseDto = QuestionDTO.builder()
                    .id(question.getId())
                    .languageId(question.getLanguage().getId())
                    .languageName(question.getLanguage().getName())
                    .moduleType(question.getModuleType().toString())
                    .cefrLevel(question.getCefrLevel())
                    .questionText(question.getQuestionText())
                    .status(question.getStatus().toString())
                    .updatedAt(question.getUpdatedAt())
                    .build();
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Question updated", responseDto));
        } catch (Exception e) {
            log.error("Failed to update question: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<AuthDTO.ApiResponse<Void>> deleteQuestion(@PathVariable Integer id) {
        try {
            questionService.deleteQuestion(id);
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Question deleted"));
        } catch (Exception e) {
            log.error("Failed to delete question: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }
}
