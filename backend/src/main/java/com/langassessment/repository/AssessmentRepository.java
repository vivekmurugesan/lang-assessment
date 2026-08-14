package com.langassessment.repository;

import com.langassessment.entity.Assessment;
import com.langassessment.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Integer> {
    Page<Assessment> findByAdmin(User admin, Pageable pageable);
    List<Assessment> findByAdminAndStatus(User admin, Assessment.AssessmentStatus status);
    Optional<Assessment> findByIdAndAdmin(Integer id, User admin);
    List<Assessment> findByStatus(Assessment.AssessmentStatus status);
}
