package com.library.service;

import com.library.model.Book;
import com.library.model.BorrowLog;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.BorrowLogRepository;
import com.library.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class BorrowService {

    private final BorrowLogRepository borrowLogRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public BorrowService(BorrowLogRepository borrowLogRepository, BookRepository bookRepository, UserRepository userRepository) {
        this.borrowLogRepository = borrowLogRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void borrowBook(Long bookId, String username) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Kitap bulunamadı"));
        
        if (book.getStock() == null || book.getStock() <= 0) {
            throw new RuntimeException("Kitap stokta yok");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // Stoğu düşür
        book.setStock(book.getStock() - 1);
        bookRepository.save(book);

    // Log kaydı oluştur
        BorrowLog log = new BorrowLog(user, book, LocalDateTime.now(), "BORROWED");
        borrowLogRepository.save(log);
    }

    public java.util.List<BorrowLog> getUserBorrowLogs(String username) {
        return borrowLogRepository.findByUserUsernameOrderByBorrowDateDesc(username);
    }

    @Transactional
    public void returnBook(Long logId, String username) {
        BorrowLog log = borrowLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("Ödünç alma kaydı bulunamadı"));

        if (!log.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bu kayıt size ait değil");
        }

        if ("RETURNED".equals(log.getStatus())) {
            throw new RuntimeException("Bu kitap zaten iade edilmiş");
        }

        // Stoğu artır
        Book book = log.getBook();
        book.setStock((book.getStock() == null ? 0 : book.getStock()) + 1);
        bookRepository.save(book);

        // Logu güncelle
        log.setStatus("RETURNED");
        log.setReturnDate(LocalDateTime.now());
        borrowLogRepository.save(log);
    }
}
