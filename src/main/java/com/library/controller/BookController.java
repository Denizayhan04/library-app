package com.library.controller;

import com.library.model.Book;
import com.library.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class BookController {

    private final BookService bookService;
    private final com.library.service.BorrowService borrowService;

    public BookController(BookService bookService, com.library.service.BorrowService borrowService) {
        this.bookService = bookService;
        this.borrowService = borrowService;
    }

    // Ana sayfa -> kitap listesine yönlendir
    @GetMapping("/")
    public String home() {
        return "redirect:/books";
    }

    // Kitap listesi + arama
    @GetMapping("/books")
    public String listBooks(@RequestParam(required = false) String keyword, Model model) {
        List<Book> books;

        if (keyword != null && !keyword.trim().isEmpty()) {
            books = bookService.searchBooks(keyword);
            model.addAttribute("keyword", keyword);
            model.addAttribute("searchActive", true);
        } else {
            books = bookService.getAllBooks();
            model.addAttribute("searchActive", false);
        }

        model.addAttribute("books", books);
        model.addAttribute("totalBooks", bookService.getAllBooks().size());
        return "books/list";
    }

    // Kitap arama (ayrı endpoint - aynı işlev)
    @GetMapping("/books/search")
    public String searchBooks(@RequestParam String keyword) {
        return "redirect:/books?keyword=" + keyword;
    }

    // Kitap detay
    @GetMapping("/books/{id}")
    public String bookDetail(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookService.getBookById(id));
        return "books/detail";
    }

    @PostMapping("/books/{id}/borrow")
    public String borrowBook(@PathVariable Long id, org.springframework.security.core.Authentication authentication, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        try {
            borrowService.borrowBook(id, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Kitap başarıyla ödünç alındı.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/books/" + id;
    }

    @GetMapping("/books/{id}/image")
    public org.springframework.http.ResponseEntity<byte[]> getBookImage(@PathVariable Long id) {
        Book book = bookService.getBookById(id);
        if (book != null && book.getImage() != null) {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.IMAGE_JPEG);
            return new org.springframework.http.ResponseEntity<>(book.getImage(), headers, org.springframework.http.HttpStatus.OK);
        }
        return new org.springframework.http.ResponseEntity<>(org.springframework.http.HttpStatus.NOT_FOUND);
    }

    @GetMapping("/my-books")
    public String myBooks(Model model, org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        List<com.library.model.BorrowLog> logs = borrowService.getUserBorrowLogs(authentication.getName());
        model.addAttribute("borrowLogs", logs);
        return "books/my-books";
    }

    @PostMapping("/my-books/return/{id}")
    public String returnBook(@PathVariable Long id, org.springframework.security.core.Authentication authentication, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            borrowService.returnBook(id, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Kitap başarıyla iade edildi.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/my-books";
    }
}
