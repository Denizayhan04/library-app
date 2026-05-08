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

    public AdminController(BookService bookService) {
        this.bookService = bookService;
    }

    // Admin ana sayfa - kitap listesi
    @GetMapping
    public String adminPanel(Model model) {
        model.addAttribute("books", bookService.getAllBooks());
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
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", book.getId() == null ? "Yeni Kitap Ekle" : "Kitabı Düzenle");
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
