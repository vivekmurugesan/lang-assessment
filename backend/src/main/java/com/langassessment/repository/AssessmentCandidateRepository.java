package com.langassessment.repository;

import com.langassessment.entity.Assessment;
import com.langassessment.entity.AssessmentCandidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentCandidateRepository extends JpaRepository<AssessmentCandidate, Integer> {
    Page<AssessmentCandidate> findByAssessment(Assessment assessment, Pageable pageable);

    List<AssessmentCandidate> findByAssessment(Assessment assessment);

    Optional<AssessmentCandidate> findBySecureLink(String secureLink);

    Optional<AssessmentCandidate> findByAssessmentAndUser(Assessment assessment, Integer userId);
}
