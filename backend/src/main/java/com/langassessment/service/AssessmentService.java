package com.langassessment.service;

import com.langassessment.dto.AssessmentDTO;
import com.langassessment.entity.Assessment;
import com.langassessment.entity.Language;
import com.langassessment.entity.User;
import com.langassessment.repository.AssessmentRepository;
import com.langassessment.repository.LanguageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final LanguageRepository languageRepository;

    @Transactional
    public Assessment createAssessment(AssessmentDTO dto, User admin) {
        Language language = languageRepository.findById(dto.getLanguageId())
                .orElseThrow(() -> new RuntimeException("Language not found"));

        Assessment assessment = Assessment.builder()
                .admin(admin)
                .language(language)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(Assessment.AssessmentStatus.DRAFT)
                .build();

        return assessmentRepository.save(assessment);
    }

    @Transactional(readOnly = true)
    public Page<Assessment> getAdminAssessments(User admin, Pageable pageable) {
        return assessmentRepository.findByAdmin(admin, pageable);
    }

    @Transactional(readOnly = true)
    public Assessment getAssessmentById(Integer id) {
        return assessmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));
    }

    @Transactional(readOnly = true)
    public Assessment getAssessmentByIdAndAdmin(Integer id, User admin) {
        return assessmentRepository.findByIdAndAdmin(id, admin)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));
    }

    @Transactional
    public Assessment updateAssessmentStatus(Integer id, User admin, Assessment.AssessmentStatus status) {
        Assessment assessment = getAssessmentByIdAndAdmin(id, admin);
        assessment.setStatus(status);

        if (status == Assessment.AssessmentStatus.CLOSED) {
            assessment.setClosedAt(LocalDateTime.now());
        }

        return assessmentRepository.save(assessment);
    }

    @Transactional(readOnly = true)
    public List<Assessment> getActiveAssessments() {
        return assessmentRepository.findByStatus(Assessment.AssessmentStatus.ACTIVE);
    }

    @Transactional
    public void deleteAssessment(Integer id, User admin) {
        Assessment assessment = getAssessmentByIdAndAdmin(id, admin);

        if (assessment.getStatus() != Assessment.AssessmentStatus.DRAFT) {
            throw new RuntimeException("Can only delete assessments in DRAFT status");
        }

        assessmentRepository.delete(assessment);
    }
}
