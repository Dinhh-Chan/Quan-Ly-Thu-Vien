package com.example.library_management.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.library_management.entity.Author;
import com.example.library_management.exception.ResourceNotFoundException;
import com.example.library_management.repository.JpaRepository.AuthorRepository;
@Service
public class AuthorService {
    
    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository){
        this.authorRepository = authorRepository;
    }

    // Lấy tất cả tác giả
    public List<Author> getAllAuthors(){
        return authorRepository.findAll();
    }

    // Lấy tác giả theo ID
    public Optional<Author> getAuthorById(Long id){
        return authorRepository.findById(id);
    }

    // Tạo tác giả mới
    public Author createAuthor(Author author){
        return authorRepository.save(author);
    }

    // Cập nhật tác giả
    public Author updateAuthor(Long id, Author authorDetails){
        return authorRepository.findById(id).map(author -> {
            author.setName(authorDetails.getName());
            author.setBio(authorDetails.getBio());
            // Cập nhật các thuộc tính khác nếu cần
            return authorRepository.save(author);
        }).orElseThrow(() -> new ResourceNotFoundException("Author not found with id " + id));
    }

    // Xóa tác giả
    public void deleteAuthor(Long id){
        authorRepository.deleteById(id);
    }
    // Existing: Search authors by name
    public List<Author> searchAuthorsByName(String name) {
        return authorRepository.findByNameContainingIgnoreCase(name);
    }

    // **New: Search authors by keyword across multiple fields**
    public List<Author> searchAuthorsByKeyword(String keyword, int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        Page<Author> authorPage = authorRepository.findByNameContainingIgnoreCaseOrBioContainingIgnoreCase(keyword, keyword, pageable);
        return authorPage.getContent();
    }

    // Thêm các phương thức nghiệp vụ khác nếu cần
}