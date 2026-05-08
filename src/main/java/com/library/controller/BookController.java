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

    public BookController(BookService bookService) {
        this.bookService = bookService;
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
}
