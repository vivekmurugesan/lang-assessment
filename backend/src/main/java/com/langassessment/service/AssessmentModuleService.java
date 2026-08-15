package com.langassessment.service;

import com.langassessment.dto.AssessmentModuleDTO;
import com.langassessment.entity.Assessment;
import com.langassessment.entity.AssessmentModule;
import com.langassessment.repository.AssessmentModuleRepository;
import com.langassessment.repository.AssessmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssessmentModuleService {

    private final AssessmentModuleRepository assessmentModuleRepository;
    private final AssessmentRepository assessmentRepository;

    @Transactional
    public AssessmentModule createAssessmentModule(Integer assessmentId, AssessmentModuleDTO dto) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        AssessmentModule module = AssessmentModule.builder()
                .assessment(assessment)
                .moduleType(AssessmentModule.ModuleType.valueOf(dto.getModuleType()))
                .numQuestions(dto.getNumQuestions())
                .difficultyLevel(dto.getDifficultyLevel())
                .isEnabled(dto.getIsEnabled() != null ? dto.getIsEnabled() : true)
                .build();

        return assessmentModuleRepository.save(module);
    }

    @Transactional(readOnly = true)
    public List<AssessmentModuleDTO> getAssessmentModules(Integer assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        return assessmentModuleRepository.findByAssessment(assessment).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AssessmentModule updateAssessmentModule(Integer moduleId, AssessmentModuleDTO dto) {
        AssessmentModule module = assessmentModuleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Assessment module not found"));

        if (dto.getNumQuestions() != null) {
            module.setNumQuestions(dto.getNumQuestions());
        }
        if (dto.getDifficultyLevel() != null) {
            module.setDifficultyLevel(dto.getDifficultyLevel());
        }
        if (dto.getIsEnabled() != null) {
            module.setIsEnabled(dto.getIsEnabled());
        }

        return assessmentModuleRepository.save(module);
    }

    @Transactional
    public void deleteAssessmentModule(Integer moduleId) {
        assessmentModuleRepository.deleteById(moduleId);
    }

    private AssessmentModuleDTO convertToDTO(AssessmentModule module) {
        return AssessmentModuleDTO.builder()
                .id(module.getId())
                .assessmentId(module.getAssessment().getId())
                .moduleType(module.getModuleType().toString())
                .numQuestions(module.getNumQuestions())
                .difficultyLevel(module.getDifficultyLevel())
                .isEnabled(module.getIsEnabled())
                .createdAt(module.getCreatedAt())
                .build();
    }
}
