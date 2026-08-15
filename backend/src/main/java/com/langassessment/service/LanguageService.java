package com.langassessment.service;

import com.langassessment.dto.LanguageDTO;
import com.langassessment.entity.Language;
import com.langassessment.repository.LanguageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LanguageService {

    private final LanguageRepository languageRepository;

    @Transactional(readOnly = true)
    public List<LanguageDTO> getAllLanguages() {
        return languageRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LanguageDTO getLanguageById(Integer id) {
        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Language not found"));
        return convertToDTO(language);
    }

    private LanguageDTO convertToDTO(Language language) {
        return LanguageDTO.builder()
                .id(language.getId())
                .code(language.getCode())
                .name(language.getName())
                .createdAt(language.getCreatedAt())
                .build();
    }
}
