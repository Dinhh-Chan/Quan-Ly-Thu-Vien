package com.example.library_management.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.library_management.entity.Author;
@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    // Tìm tác giả theo tên
    Optional<Author> findByName(String name);
    // Existing: Search by name
    List<Author> findByNameContainingIgnoreCase(String name);
    
    // **New: Search by name or bio containing keyword (case-insensitive)**
    Page<Author> findByNameContainingIgnoreCaseOrBioContainingIgnoreCase(String nameKeyword, String bioKeyword, Pageable pageable);

}
