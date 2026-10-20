package com.skripty.repository;

import com.skripty.entity.StudyDocument;
import com.skripty.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudyDocumentRepository extends JpaRepository<StudyDocument, Long> {
    List<StudyDocument> findByOwnerOrderByCreatedAtDesc(User owner);
    Optional<StudyDocument> findByIdAndOwner(Long id, User owner);
}
