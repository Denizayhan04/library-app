package com.library.config;

import com.library.model.Book;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      BookRepository bookRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            // Kullanıcılar oluştur
            if (userRepository.count() == 0) {
                userRepository.save(new User("admin", passwordEncoder.encode("admin123"), "ROLE_ADMIN"));
                userRepository.save(new User("user", passwordEncoder.encode("user123"), "ROLE_USER"));
                System.out.println("✅ Kullanıcılar oluşturuldu: admin/admin123 ve user/user123");
            }

            // Örnek kitaplar ekle
            if (bookRepository.count() == 0) {
                bookRepository.save(new Book("Suç ve Ceza", "Fyodor Dostoyevski", "978-975-10-0001-1",
                        "Roman", 1866, "Psikolojik gerilim türünün başyapıtı.", 5));
                bookRepository.save(new Book("1984", "George Orwell", "978-975-10-0002-2",
                        "Distopya", 1949, "Totaliter bir gelecek dünyasını anlatan roman.", 5));
                bookRepository.save(new Book("Küçük Prens", "Antoine de Saint-Exupéry", "978-975-10-0003-3",
                        "Çocuk", 1943, "Dünyanın en çok okunan kitaplarından biri.", 5));
                bookRepository.save(new Book("Savaş ve Barış", "Lev Tolstoy", "978-975-10-0004-4",
                        "Roman", 1869, "Napolyon savaşları döneminde Rus toplumunu anlatan epik roman.", 5));
                bookRepository.save(new Book("Dune", "Frank Herbert", "978-975-10-0005-5",
                        "Bilim Kurgu", 1965, "Çöl gezegen Arrakis'te geçen bilim kurgu klasiği.", 5));
                bookRepository.save(new Book("Tutunamayanlar", "Oğuz Atay", "978-975-10-0006-6",
                        "Roman", 1972, "Türk edebiyatının postmodern başyapıtı.", 5));
                bookRepository.save(new Book("İnce Memed", "Yaşar Kemal", "978-975-10-0007-7",
                        "Roman", 1955, "Anadolu'nun destansı romanı.", 5));
                bookRepository.save(new Book("Sherlock Holmes", "Arthur Conan Doyle", "978-975-10-0008-8",
                        "Polisiye", 1887, "Dünyaca ünlü dedektif Sherlock Holmes'un maceraları.", 5));
                System.out.println("✅ Örnek kitaplar eklendi.");
            }
        };
    }
}
