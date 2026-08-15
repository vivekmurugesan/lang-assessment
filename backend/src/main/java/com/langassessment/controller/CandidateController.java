package com.langassessment.controller;

import com.langassessment.dto.AuthDTO;
import com.langassessment.dto.CandidateDTO;
import com.langassessment.service.CandidateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/assessments/{assessmentId}/candidates")
@RequiredArgsConstructor
@Slf4j
public class CandidateController {

    private final CandidateService candidateService;

    @PostMapping
    public ResponseEntity<AuthDTO.ApiResponse<CandidateDTO>> addCandidate(
            @PathVariable Integer assessmentId,
            @RequestBody Map<String, String> candidateData) {
        try {
            var candidate = candidateService.addCandidate(
                    assessmentId,
                    candidateData.get("email"),
                    candidateData.get("name")
            );
            CandidateDTO dto = CandidateDTO.builder()
                    .id(candidate.getId())
                    .assessmentId(candidate.getAssessment().getId())
                    .email(candidate.getUser().getEmail())
                    .name(candidate.getUser().getName())
                    .secureLink(candidate.getSecureLink())
                    .status(candidate.getStatus().toString())
                    .createdAt(candidate.getCreatedAt())
                    .build();
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Candidate added", dto));
        } catch (Exception e) {
            log.error("Failed to add candidate: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @PostMapping("/bulk-upload")
    public ResponseEntity<AuthDTO.ApiResponse<List<CandidateDTO>>> bulkAddCandidates(
            @PathVariable Integer assessmentId,
            @RequestBody List<Map<String, String>> candidatesList) {
        try {
            List<CandidateDTO> candidates = candidateService.bulkAddCandidates(assessmentId, candidatesList);
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Candidates added", candidates));
        } catch (Exception e) {
            log.error("Failed to add candidates: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<AuthDTO.ApiResponse<Page<CandidateDTO>>> getCandidates(
            @PathVariable Integer assessmentId,
            Pageable pageable) {
        try {
            Page<CandidateDTO> candidates = candidateService.getCandidatesByAssessment(assessmentId, pageable);
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Candidates retrieved", candidates));
        } catch (Exception e) {
            log.error("Failed to retrieve candidates: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @GetMapping("/by-link/{secureLink}")
    public ResponseEntity<AuthDTO.ApiResponse<CandidateDTO>> getCandidateByLink(
            @PathVariable String secureLink) {
        try {
            CandidateDTO candidate = candidateService.getCandidateBySecureLink(secureLink);
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Candidate retrieved", candidate));
        } catch (Exception e) {
            log.error("Failed to retrieve candidate: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @PutMapping("/{candidateId}/status")
    public ResponseEntity<AuthDTO.ApiResponse<Void>> updateCandidateStatus(
            @PathVariable Integer assessmentId,
            @PathVariable Integer candidateId,
            @RequestParam String status) {
        try {
            candidateService.updateCandidateStatus(candidateId, status);
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Candidate status updated"));
        } catch (Exception e) {
            log.error("Failed to update candidate status: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }
}
