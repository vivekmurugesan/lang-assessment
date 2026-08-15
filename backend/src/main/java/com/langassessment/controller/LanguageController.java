package com.langassessment.controller;

import com.langassessment.dto.AuthDTO;
import com.langassessment.dto.LanguageDTO;
import com.langassessment.service.LanguageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/languages")
@RequiredArgsConstructor
@Slf4j
public class LanguageController {

    private final LanguageService languageService;

    @GetMapping
    public ResponseEntity<AuthDTO.ApiResponse<List<LanguageDTO>>> getAllLanguages() {
        try {
            List<LanguageDTO> languages = languageService.getAllLanguages();
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Languages retrieved", languages));
        } catch (Exception e) {
            log.error("Failed to retrieve languages: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }
}
