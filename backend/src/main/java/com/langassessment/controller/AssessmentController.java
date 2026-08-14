package com.langassessment.controller;

import com.langassessment.dto.AssessmentDTO;
import com.langassessment.dto.AuthDTO;
import com.langassessment.entity.Assessment;
import com.langassessment.entity.User;
import com.langassessment.service.AssessmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
@Slf4j
public class AssessmentController {

    private final AssessmentService assessmentService;

    @PostMapping
    public ResponseEntity<AuthDTO.ApiResponse<AssessmentDTO>> createAssessment(
            @RequestBody AssessmentDTO dto,
            Authentication authentication) {
        try {
            User admin = (User) authentication.getPrincipal();
            Assessment assessment = assessmentService.createAssessment(dto, admin);
            AssessmentDTO responseDto = AssessmentDTO.builder()
                    .id(assessment.getId())
                    .title(assessment.getTitle())
                    .description(assessment.getDescription())
                    .languageId(assessment.getLanguage().getId())
                    .languageName(assessment.getLanguage().getName())
                    .status(assessment.getStatus().toString())
                    .createdAt(assessment.getCreatedAt())
                    .build();
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Assessment created", responseDto));
        } catch (Exception e) {
            log.error("Failed to create assessment: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<AuthDTO.ApiResponse<Page<AssessmentDTO>>> getAdminAssessments(
            Authentication authentication,
            Pageable pageable) {
        try {
            User admin = (User) authentication.getPrincipal();
            Page<Assessment> assessments = assessmentService.getAdminAssessments(admin, pageable);
            Page<AssessmentDTO> dtos = assessments.map(a -> AssessmentDTO.builder()
                    .id(a.getId())
                    .title(a.getTitle())
                    .description(a.getDescription())
                    .languageId(a.getLanguage().getId())
                    .languageName(a.getLanguage().getName())
                    .status(a.getStatus().toString())
                    .createdAt(a.getCreatedAt())
                    .updatedAt(a.getUpdatedAt())
                    .build());
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Assessments retrieved", dtos));
        } catch (Exception e) {
            log.error("Failed to retrieve assessments: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthDTO.ApiResponse<AssessmentDTO>> getAssessment(
            @PathVariable Integer id,
            Authentication authentication) {
        try {
            User admin = (User) authentication.getPrincipal();
            Assessment assessment = assessmentService.getAssessmentByIdAndAdmin(id, admin);
            AssessmentDTO dto = AssessmentDTO.builder()
                    .id(assessment.getId())
                    .title(assessment.getTitle())
                    .description(assessment.getDescription())
                    .languageId(assessment.getLanguage().getId())
                    .languageName(assessment.getLanguage().getName())
                    .status(assessment.getStatus().toString())
                    .createdAt(assessment.getCreatedAt())
                    .updatedAt(assessment.getUpdatedAt())
                    .build();
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Assessment retrieved", dto));
        } catch (Exception e) {
            log.error("Failed to retrieve assessment: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AuthDTO.ApiResponse<AssessmentDTO>> updateAssessmentStatus(
            @PathVariable Integer id,
            @RequestParam String status,
            Authentication authentication) {
        try {
            User admin = (User) authentication.getPrincipal();
            Assessment.AssessmentStatus assessmentStatus = Assessment.AssessmentStatus.valueOf(status);
            Assessment assessment = assessmentService.updateAssessmentStatus(id, admin, assessmentStatus);
            AssessmentDTO dto = AssessmentDTO.builder()
                    .id(assessment.getId())
                    .title(assessment.getTitle())
                    .status(assessment.getStatus().toString())
                    .updatedAt(assessment.getUpdatedAt())
                    .build();
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Assessment status updated", dto));
        } catch (Exception e) {
            log.error("Failed to update assessment status: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<AuthDTO.ApiResponse<Void>> deleteAssessment(
            @PathVariable Integer id,
            Authentication authentication) {
        try {
            User admin = (User) authentication.getPrincipal();
            assessmentService.deleteAssessment(id, admin);
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Assessment deleted"));
        } catch (Exception e) {
            log.error("Failed to delete assessment: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }
}
