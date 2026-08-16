package com.langassessment.repository;

import com.langassessment.entity.AssessmentSubmission;
import com.langassessment.entity.AssessmentCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentSubmissionRepository extends JpaRepository<AssessmentSubmission, Integer> {
    Optional<AssessmentSubmission> findByAssessmentCandidate(AssessmentCandidate assessmentCandidate);
    List<AssessmentSubmission> findByAssessmentCandidateAssessmentId(Integer assessmentId);
}
