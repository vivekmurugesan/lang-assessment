package com.langassessment.repository;

import com.langassessment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndIsActive(String email, Boolean isActive);
    boolean existsByEmail(String email);
}
