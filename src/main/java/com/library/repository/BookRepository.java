package com.library.repository;

import com.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Başlık, yazar veya kategoriye göre arama
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.category) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Book> search(@Param("keyword") String keyword);

    // Kategoriye göre listeleme
    List<Book> findByCategory(String category);

    // Yazara göre listeleme
    List<Book> findByAuthorContainingIgnoreCase(String author);

    @Query("SELECT SUM(b.stock) FROM Book b")
    Integer sumStock();
}
