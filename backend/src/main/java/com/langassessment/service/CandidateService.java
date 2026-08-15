package com.langassessment.service;

import com.langassessment.dto.CandidateDTO;
import com.langassessment.entity.Assessment;
import com.langassessment.entity.AssessmentCandidate;
import com.langassessment.entity.User;
import com.langassessment.repository.AssessmentCandidateRepository;
import com.langassessment.repository.AssessmentRepository;
import com.langassessment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateService {

    private final AssessmentCandidateRepository assessmentCandidateRepository;
    private final AssessmentRepository assessmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public AssessmentCandidate addCandidate(Integer assessmentId, String email, String name) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            String tempPassword = UUID.randomUUID().toString().substring(0, 8);
            return userRepository.save(User.builder()
                    .email(email)
                    .name(name)
                    .passwordHash(passwordEncoder.encode(tempPassword))
                    .role(User.UserRole.CANDIDATE)
                    .isActive(true)
                    .build());
        });

        String secureLink = UUID.randomUUID().toString();
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);

        AssessmentCandidate candidate = AssessmentCandidate.builder()
                .assessment(assessment)
                .user(user)
                .secureLink(secureLink)
                .tempPassword(passwordEncoder.encode(tempPassword))
                .status(AssessmentCandidate.CandidateStatus.INVITED)
                .build();

        AssessmentCandidate savedCandidate = assessmentCandidateRepository.save(candidate);

        try {
            emailService.sendCandidateInvitation(email, name, assessment.getTitle(), secureLink);
        } catch (Exception e) {
            log.warn("Email sending failed for candidate {}, but candidate was created: {}", email, e.getMessage());
        }

        return savedCandidate;
    }

    @Transactional(readOnly = true)
    public Page<CandidateDTO> getCandidatesByAssessment(Integer assessmentId, Pageable pageable) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        return assessmentCandidateRepository.findByAssessment(assessment, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public CandidateDTO getCandidateBySecureLink(String secureLink) {
        AssessmentCandidate candidate = assessmentCandidateRepository.findBySecureLink(secureLink)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));
        return convertToDTO(candidate);
    }

    @Transactional(readOnly = true)
    public AssessmentCandidate getCandidateBySecureLinkEntity(String secureLink) {
        return assessmentCandidateRepository.findBySecureLink(secureLink)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));
    }

    @Transactional(readOnly = true)
    public List<CandidateDTO> getCandidateAssessments(User user) {
        List<AssessmentCandidate> candidates = assessmentCandidateRepository.findByUser(user);
        return candidates.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void updateCandidateStatus(Integer candidateId, String status) {
        AssessmentCandidate candidate = assessmentCandidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));
        candidate.setStatus(AssessmentCandidate.CandidateStatus.valueOf(status));
        assessmentCandidateRepository.save(candidate);
    }

    @Transactional
    public List<CandidateDTO> bulkAddCandidates(Integer assessmentId, List<Map<String, String>> candidatesList) {
        return candidatesList.stream()
                .map(candidateData -> addCandidate(
                        assessmentId,
                        candidateData.get("email"),
                        candidateData.get("name")
                ))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void resendInvitation(Integer candidateId) {
        AssessmentCandidate candidate = assessmentCandidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));
        String newSecureLink = UUID.randomUUID().toString();
        candidate.setSecureLink(newSecureLink);
        assessmentCandidateRepository.save(candidate);

        try {
            emailService.sendCandidateInvitation(
                    candidate.getUser().getEmail(),
                    candidate.getUser().getName(),
                    candidate.getAssessment().getTitle(),
                    newSecureLink
            );
        } catch (Exception e) {
            log.warn("Email resend failed for candidate {}, but secure link was updated: {}", candidateId, e.getMessage());
        }

        log.info("Invitation resent for candidate: {}", candidateId);
    }

    private CandidateDTO convertToDTO(AssessmentCandidate candidate) {
        return CandidateDTO.builder()
                .id(candidate.getId())
                .assessmentId(candidate.getAssessment().getId())
                .userId(candidate.getUser().getId())
                .email(candidate.getUser().getEmail())
                .name(candidate.getUser().getName())
                .secureLink(candidate.getSecureLink())
                .status(candidate.getStatus().toString())
                .startedAt(candidate.getStartedAt())
                .completedAt(candidate.getCompletedAt())
                .createdAt(candidate.getCreatedAt())
                .updatedAt(candidate.getUpdatedAt())
                .build();
    }
}
