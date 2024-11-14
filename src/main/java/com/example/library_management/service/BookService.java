package com.example.library_management.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.library_management.entity.Book;
import com.example.library_management.entity.Category;
import com.example.library_management.exception.ResourceNotFoundException;
import com.example.library_management.repository.JpaRepository.BookRepository;
import com.example.library_management.repository.JpaRepository.CategoryRepository;

@Service
public class BookService {
    
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final Path fileStorageLocation;

    // Constructor with Dependency Injection
    public BookService(BookRepository bookRepository, 
                       CategoryRepository categoryRepository,
                       @Value("${file.upload-dir}") String uploadDir) throws IOException {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        // Create directories if they do not exist
        Files.createDirectories(this.fileStorageLocation);
    }

    // Get all books
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // Get book by ID
    public Optional<Book> getBookById(Long id){
        return bookRepository.findById(id);
    }

    // Get file by book ID (Assuming it's similar to getBookById)
    public Optional<Book> getFileById(Long id){
        return bookRepository.findById(id);
    }

    // Create a new book
    @Transactional
    public Book saveBook(Book book, Set<Long> categoryIds){
        Set<Category> categories = categoryRepository.findAllById(categoryIds)
                                        .stream().collect(Collectors.toSet());
        book.setCategories(categories);
        return bookRepository.save(book);
    }

    // Update an existing book
    @Transactional
    public Book updateBook(Long id, Book bookDetails, Set<Long> categoryIds){
        return bookRepository.findById(id).map(book -> {
            book.setTitle(bookDetails.getTitle());
            book.setQuantity(bookDetails.getQuantity());
            Set<Category> categories = categoryRepository.findAllById(categoryIds)
                                            .stream().collect(Collectors.toSet());
            book.setCategories(categories);
            book.setAuthors(bookDetails.getAuthors());
            // Update other attributes if needed
            return bookRepository.save(book);
        }).orElseThrow(() -> new ResourceNotFoundException("Book not found with id " + id));
    }

    // Delete a book by ID
    public void deleteBook(Long id){
        bookRepository.deleteById(id);
    }

    // Upload Image for a Book
    public Book uploadBookImage(Long bookId, MultipartFile file) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        // Optionally, validate file type (e.g., JPEG, PNG)
        String contentType = file.getContentType();
        if (!isImage(contentType)) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        // Retrieve the book
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        // Generate a unique file name
        String fileName = "book_" + bookId + "_" + System.currentTimeMillis() + "_" + sanitizeFileName(file.getOriginalFilename());

        // Resolve the file path
        Path targetLocation = this.fileStorageLocation.resolve(fileName);

        // Save the file
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // Update the book's image path
        book.setPath_Imange(targetLocation.toString());
        return bookRepository.save(book);
    }

    // Helper method to check if the file is an image
    private boolean isImage(String contentType) {
        return contentType != null && (
               contentType.equalsIgnoreCase("image/jpeg") ||
               contentType.equalsIgnoreCase("image/png") ||
               contentType.equalsIgnoreCase("image/gif"));
    }

    // Helper method to sanitize file names
    private String sanitizeFileName(String originalFileName) {
        return originalFileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
    }

    // Additional business methods if needed
}
