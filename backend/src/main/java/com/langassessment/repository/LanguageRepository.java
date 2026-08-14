package com.langassessment.repository;

import com.langassessment.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LanguageRepository extends JpaRepository<Language, Integer> {
    Optional<Language> findByCode(String code);
    Optional<Language> findByName(String name);
}
