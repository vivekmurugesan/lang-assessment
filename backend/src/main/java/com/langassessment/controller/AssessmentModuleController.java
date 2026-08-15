package com.langassessment.controller;

import com.langassessment.dto.AssessmentModuleDTO;
import com.langassessment.dto.AuthDTO;
import com.langassessment.entity.AssessmentModule;
import com.langassessment.service.AssessmentModuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/assessments/{assessmentId}/modules")
@RequiredArgsConstructor
@Slf4j
public class AssessmentModuleController {

    private final AssessmentModuleService assessmentModuleService;

    @PostMapping
    public ResponseEntity<AuthDTO.ApiResponse<AssessmentModuleDTO>> createModule(
            @PathVariable Integer assessmentId,
            @RequestBody AssessmentModuleDTO dto) {
        try {
            AssessmentModule module = assessmentModuleService.createAssessmentModule(assessmentId, dto);
            AssessmentModuleDTO responseDto = AssessmentModuleDTO.builder()
                    .id(module.getId())
                    .assessmentId(module.getAssessment().getId())
                    .moduleType(module.getModuleType().toString())
                    .numQuestions(module.getNumQuestions())
                    .difficultyLevel(module.getDifficultyLevel())
                    .isEnabled(module.getIsEnabled())
                    .createdAt(module.getCreatedAt())
                    .build();
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Module created", responseDto));
        } catch (Exception e) {
            log.error("Failed to create module: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<AuthDTO.ApiResponse<List<AssessmentModuleDTO>>> getModules(
            @PathVariable Integer assessmentId) {
        try {
            List<AssessmentModuleDTO> modules = assessmentModuleService.getAssessmentModules(assessmentId);
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Modules retrieved", modules));
        } catch (Exception e) {
            log.error("Failed to retrieve modules: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @PutMapping("/{moduleId}")
    public ResponseEntity<AuthDTO.ApiResponse<AssessmentModuleDTO>> updateModule(
            @PathVariable Integer assessmentId,
            @PathVariable Integer moduleId,
            @RequestBody AssessmentModuleDTO dto) {
        try {
            AssessmentModule module = assessmentModuleService.updateAssessmentModule(moduleId, dto);
            AssessmentModuleDTO responseDto = AssessmentModuleDTO.builder()
                    .id(module.getId())
                    .assessmentId(module.getAssessment().getId())
                    .moduleType(module.getModuleType().toString())
                    .numQuestions(module.getNumQuestions())
                    .difficultyLevel(module.getDifficultyLevel())
                    .isEnabled(module.getIsEnabled())
                    .createdAt(module.getCreatedAt())
                    .build();
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Module updated", responseDto));
        } catch (Exception e) {
            log.error("Failed to update module: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @DeleteMapping("/{moduleId}")
    public ResponseEntity<AuthDTO.ApiResponse<Void>> deleteModule(
            @PathVariable Integer assessmentId,
            @PathVariable Integer moduleId) {
        try {
            assessmentModuleService.deleteAssessmentModule(moduleId);
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Module deleted"));
        } catch (Exception e) {
            log.error("Failed to delete module: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }
}
