package com.library.controller;

import com.library.model.Book;
import com.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final BookService bookService;
    private final com.library.repository.UserRepository userRepository;
    private final com.library.repository.BorrowLogRepository borrowLogRepository;
    private final com.library.repository.BookRepository bookRepository;

    public AdminController(BookService bookService, 
                           com.library.repository.UserRepository userRepository,
                           com.library.repository.BorrowLogRepository borrowLogRepository,
                           com.library.repository.BookRepository bookRepository) {
        this.bookService = bookService;
        this.userRepository = userRepository;
        this.borrowLogRepository = borrowLogRepository;
        this.bookRepository = bookRepository;
    }

    // Admin ana sayfa - kitap listesi
    @GetMapping
    public String adminPanel(Model model) {
        model.addAttribute("books", bookService.getAllBooks());
        
        long totalUsers = userRepository.count();
        long borrowedBooks = borrowLogRepository.countByStatus("BORROWED");
        long totalBooks = bookRepository.count();
        Integer totalStock = bookRepository.sumStock();
        if (totalStock == null) totalStock = 0;
        
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("borrowedBooks", borrowedBooks);
        model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("totalStock", totalStock);
        model.addAttribute("borrowLogs", borrowLogRepository.findAllByOrderByBorrowDateDesc());
        
        return "admin/panel";
    }

    // Yeni kitap formu
    @GetMapping("/books/new")
    public String newBookForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("pageTitle", "Yeni Kitap Ekle");
        return "admin/book-form";
    }

    // Yeni kitap kaydet
    @PostMapping("/books/save")
    public String saveBook(@Valid @ModelAttribute Book book,
                           BindingResult result,
                           @RequestParam(value = "imageFile", required = false) org.springframework.web.multipart.MultipartFile imageFile,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", book.getId() == null ? "Yeni Kitap Ekle" : "Kitabı Düzenle");
            return "admin/book-form";
        }
        
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                book.setImage(imageFile.getBytes());
            } else if (book.getId() != null) {
                // Mevcut kitabı güncellerken yeni resim yüklenmemişse eski resmi koru
                Book existingBook = bookService.getBookById(book.getId());
                if (existingBook != null) {
                    book.setImage(existingBook.getImage());
                }
            }
        } catch (java.io.IOException e) {
            result.rejectValue("image", "error.book", "Resim yüklenirken bir hata oluştu");
            return "admin/book-form";
        }

        bookService.saveBook(book);
        redirectAttributes.addFlashAttribute("successMessage",
                "Kitap başarıyla " + (book.getId() == null ? "eklendi" : "güncellendi") + "!");
        return "redirect:/admin";
    }

    // Kitap düzenleme formu
    @GetMapping("/books/edit/{id}")
    public String editBookForm(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookService.getBookById(id));
        model.addAttribute("pageTitle", "Kitabı Düzenle");
        return "admin/book-form";
    }

    // Kitap sil
    @PostMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bookService.deleteBook(id);
        redirectAttributes.addFlashAttribute("successMessage", "Kitap başarıyla silindi!");
        return "redirect:/admin";
    }
}
