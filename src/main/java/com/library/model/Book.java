package com.library.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Kitap adı boş olamaz")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Yazar adı boş olamaz")
    @Column(nullable = false)
    private String author;

    @NotBlank(message = "ISBN boş olamaz")
    @Column(nullable = false, unique = true)
    private String isbn;

    @NotBlank(message = "Kategori boş olamaz")
    private String category;

    @NotNull(message = "Yayın yılı boş olamaz")
    @Min(value = 1000, message = "Geçerli bir yıl giriniz")
    private Integer publishYear;

    private String description;

    @Column(nullable = false)
    private boolean available = true;

    @NotNull(message = "Stok sayısı boş olamaz")
    @Min(value = 0, message = "Stok sayısı 0'dan küçük olamaz")
    @Column(nullable = false)
    private Integer stock = 0;

    @Column(columnDefinition = "bytea")
    private byte[] image;

    // Constructors
    public Book() {}

    public Book(String title, String author, String isbn, String category, Integer publishYear, String description, Integer stock) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.category = category;
        this.publishYear = publishYear;
        this.description = description;
        this.stock = stock != null ? stock : 0;
        this.available = this.stock > 0;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getPublishYear() { return publishYear; }
    public void setPublishYear(Integer publishYear) { this.publishYear = publishYear; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { 
        this.stock = stock;
        this.available = stock != null && stock > 0;
    }

    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }
}
