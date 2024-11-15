package com.example.library_management.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.library_management.entity.Book;
import com.example.library_management.entity.Borrowing;
import com.example.library_management.entity.Inventory;
import com.example.library_management.entity.Reader;
import com.example.library_management.enums.BorrowingStatus;
import com.example.library_management.exception.ResourceNotFoundException;
import com.example.library_management.repository.JpaRepository.BookRepository;
import com.example.library_management.repository.JpaRepository.BorrowingRepository;
import com.example.library_management.repository.JpaRepository.ReaderRepository;

@Service
public class BorrowingService {
    
    private final BorrowingRepository borrowingRepository;
    private final ReaderRepository readerRepository;
    private final BookRepository bookRepository;

    public BorrowingService(BorrowingRepository borrowingRepository,
                            ReaderRepository readerRepository,
                            BookRepository bookRepository){
        this.borrowingRepository = borrowingRepository;
        this.readerRepository = readerRepository;
        this.bookRepository = bookRepository;
    }

    // Lấy tất cả các lần mượn
    public List<Borrowing> getAllBorrowings(){
        return borrowingRepository.findAll();
    }

    // Lấy lần mượn theo ID
    public Borrowing getBorrowingById(Long id){
        return borrowingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrowing not found with id " + id));
    }

    // Tạo lần mượn mới
    @Transactional
    public Borrowing createBorrowing(Long readerId, Long bookId, LocalDate borrowDate, LocalDate returnDate){
        Reader reader = readerRepository.findById(readerId)
                .orElseThrow(() -> new ResourceNotFoundException("Reader not found with id " + readerId));
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id " + bookId));
        
        // Kiểm tra số lượng sách có sẵn
        Inventory inventory = book.getInventory();
        if(inventory.getAvailableStock() <= 0){
            throw new IllegalStateException("No available stock for book id " + bookId);
        }
        
        // Giảm số lượng sách có sẵn
        inventory.setAvailableStock(inventory.getAvailableStock() - 1);
        // Cập nhật Inventory
        book.getInventory().setAvailableStock(inventory.getAvailableStock());
        
        // Tạo Borrowing
        Borrowing borrowing = new Borrowing();
        borrowing.setReader(reader);
        borrowing.setBook(book);
        borrowing.setBorrowDate(borrowDate);
        borrowing.setReturnDate(returnDate);
        borrowing.setStatus(BorrowingStatus.DANG_MUON);
        
        return borrowingRepository.save(borrowing);
    }
    @Transactional
    public void decrementBookStock(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));

        if (book.getInventory().getAvailableStock() <= 0) {
            throw new IllegalStateException("No available stock for book ID: " + bookId);
        }

        // Decrement the available stock
        book.getInventory().setAvailableStock(book.getInventory().getAvailableStock() - 1);

        // Save the updated book inventory
        bookRepository.save(book);
    }
    @Transactional
    public void incrementBookStock(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));

        // Increment the available stock
        book.getInventory().setAvailableStock(book.getInventory().getAvailableStock() + 1);

        // Save the updated book inventory
        bookRepository.save(book);
    }

    // Cập nhật lần mượn (ví dụ: trả sách)
    @Transactional
    public Borrowing returnBook(Long borrowingId, LocalDate actualReturnDate){
        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrowing not found with id " + borrowingId));
        
        if(borrowing.getStatus() == BorrowingStatus.DA_TRA){
            throw new IllegalStateException("Book already returned");
        }
        
        borrowing.setActualReturnDate(actualReturnDate);
        borrowing.setStatus(BorrowingStatus.DA_TRA);
        
        // Tăng số lượng sách có sẵn
        Inventory inventory = borrowing.getBook().getInventory();
        inventory.setAvailableStock(inventory.getAvailableStock() + 1);
        
        return borrowingRepository.save(borrowing);
    }

    // Xóa lần mượn
    public void deleteBorrowing(Long id){
        borrowingRepository.deleteById(id);
    }


    public List<Borrowing> getBorrowingsByWeek(int year, int week) {
        LocalDate startDate = LocalDate.ofYearDay(year, 1).with(WeekFields.ISO.weekOfYear(), week).with(DayOfWeek.MONDAY);
        LocalDate endDate = startDate.plusDays(6);

        return borrowingRepository.findByBorrowDateBetween(startDate, endDate);
    }

    public List<Borrowing> getBorrowingsByMonth(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        return borrowingRepository.findByBorrowDateBetween(startDate, endDate);
    }

    public List<Borrowing> getBorrowingsByYear(int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = startDate.withDayOfYear(startDate.lengthOfYear());

        return borrowingRepository.findByBorrowDateBetween(startDate, endDate);
    }

    public long countBorrowingsByStatus(List<Borrowing> borrowings, BorrowingStatus status) {
        return borrowings.stream().filter(borrowing -> borrowing.getStatus() == status).count();
    }
    // Thêm các phương thức nghiệp vụ khác nếu cần

}
