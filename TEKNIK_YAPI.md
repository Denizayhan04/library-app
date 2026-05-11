# Kütüphane Yönetim Sistemi (Library App) - Teknik Yapı Belgesi

Bu belge, `library-app` projesinin teknik mimarisini, kullanılan teknolojileri, veritabanı yapısını ve uygulama akışını detaylı bir şekilde açıklamaktadır.

## 1. Genel Bakış

**Library App**, kitapların listelendiği, kullanıcıların kitap ödünç alabildiği ve yöneticilerin (admin) kitap stoklarını ve sistem istatistiklerini takip edebildiği tam yığın (full-stack) bir web uygulamasıdır. Proje, klasik **MVC (Model-View-Controller)** tasarım deseni ve **Katmanlı Mimari (Layered Architecture)** prensiplerine uygun olarak geliştirilmiştir.

- **Dil:** Java 17
- **Framework:** Spring Boot 3.2.0
- **Proje Yönetimi:** Maven

## 2. Kullanılan Teknolojiler ve Bağımlılıklar (Tech Stack)

Projede `pom.xml` içerisinde tanımlanan başlıca teknolojiler şunlardır:

*   **Spring Boot Starter Web:** RESTful API'ler ve MVC Controller'ları oluşturmak için.
*   **Spring Boot Starter Data JPA:** Veritabanı işlemleri (ORM - Hibernate tabanlı) için.
*   **Spring Boot Starter Security:** Kimlik doğrulama (Authentication) ve yetkilendirme (Authorization) işlemleri için.
*   **Spring Boot Starter Thymeleaf:** Sunucu tarafı HTML şablon motoru (Server-side rendering).
*   **Thymeleaf Extras Spring Security 6:** Thymeleaf şablonlarında role ve yetkiye dayalı görünüm (UI) kontrolleri yapmak için.
*   **H2 Database:** Geliştirme sürecini hızlandırmak için kullanılan bellek içi (in-memory) veritabanı.
*   **Spring Boot Starter Validation:** Veri doğrulama işlemleri (ör. `@NotBlank`, `@Min`) için.
*   **Lombok:** Boilerplate kodları (Getter, Setter, Constructor vb.) azaltmak için.

## 3. Mimari ve Katmanlar (Layered Architecture)

Uygulama `src/main/java/com/library` dizini altında modüler bir yapıda organize edilmiştir:

1.  **`config` (Yapılandırma Katmanı):** Güvenlik kurallarının ve başlangıç verilerinin tanımlandığı sınıflar.
2.  **`controller` (Sunum Katmanı):** HTTP isteklerini karşılayan, servis katmanı ile frontend (Thymeleaf) arasındaki iletişimi sağlayan sınıflar.
3.  **`service` (İş Mantığı Katmanı):** Uygulamanın temel iş kurallarının (ör. bir kitabın ödünç alınabilmesi için stok kontrolü) işletildiği katman.
4.  **`repository` (Veri Erişim Katmanı):** Spring Data JPA arayüzleri. Veritabanı CRUD işlemleri burada soyutlanmıştır.
5.  **`model` (Veri Modeli Katmanı):** Veritabanı tablolarına karşılık gelen Entity sınıfları.

## 4. Veritabanı Şeması ve Modeller (Entities)

Sistemde birbiriyle ilişkili 3 temel Entity (Varlık) bulunmaktadır:

### 4.1. `User` (Kullanıcılar Tablosu)
Sistemdeki kullanıcıları ve yetkilerini tutar.
- `id` (PK, Long)
- `username` (String, Unique, NotBlank)
- `password` (String, Hash'lenmiş şifre)
- `role` (String, Örn: `ROLE_ADMIN`, `ROLE_USER`)

### 4.2. `Book` (Kitaplar Tablosu)
Kütüphanedeki kitapların detaylarını ve stok bilgisini tutar.
- `id` (PK, Long)
- `title`, `author`, `isbn` (Unique), `category`, `publishYear`, `description`
- `stock` (Integer, Min: 0): Kitabın güncel stok sayısı.
- `available` (Boolean): `stock > 0` durumuna göre otomatik güncellenen durum bayrağı.
- `image` (byte[], BLOB): Kitap kapağı resmi doğrudan veritabanında saklanır.

### 4.3. `BorrowLog` (Ödünç Alma Kayıtları Tablosu)
Kullanıcılar ile kitaplar arasındaki "Çoka-Çok" (Many-to-Many) ilişkiyi tarihsel olarak tutan işlem tablosudur.
- `id` (PK, Long)
- `user_id` (FK -> User tablosu)
- `book_id` (FK -> Book tablosu)
- `borrowDate` (LocalDateTime): Ödünç alma tarihi.
- `returnDate` (LocalDateTime): İade tarihi (Kitap iade edilene kadar NULL).
- `status` (String): `BORROWED` (Ödünçte) veya `RETURNED` (İade Edildi).

## 5. Önemli Bileşenler ve İş Akışları

### 5.1. Güvenlik ve Kimlik Doğrulama (`SecurityConfig.java`)
- **BCrypt Hashing:** Şifreler veritabanına açık metin (plaintext) yerine BCrypt ile şifrelenerek kaydedilir.
- **Role Tabanlı Erişim (RBAC):**
  - `/admin/**` endpoint'lerine sadece `ROLE_ADMIN` yetkisine sahip kullanıcılar erişebilir.
  - `/`, `/books`, `/books/search`, `/css/**`, `/h2-console/**` endpoint'leri herkese açıktır.
  - Geri kalan tüm işlemler (kitap ödünç alma vb.) için oturum açmak zorunludur (`authenticated()`).
- **CustomUserDetailsService:** Spring Security'nin kullanıcı bilgilerini veritabanından `UserRepository` üzerinden okumasını sağlar.

### 5.2. Kitap Ödünç Alma ve İade Süreci (`BorrowService.java`)
**Ödünç Alma:**
1. Kullanıcı `/books/{id}/borrow` endpoint'ine istek atar.
2. Servis, kitabın stok durumunu kontrol eder (`stock > 0`).
3. Daha önce aynı kullanıcı bu kitabı ödünç almış ve henüz iade etmemişse hata fırlatılır.
4. Kitap stoğu 1 azaltılır.
5. Yeni bir `BorrowLog` kaydı (`status="BORROWED"`) oluşturulur.

**İade Etme:**
1. Kullanıcı `/my-books/return/{id}` endpoint'ine istek atar.
2. İlgili `BorrowLog` bulunur, `returnDate` anlık zaman olarak atanır ve `status="RETURNED"` yapılır.
3. İlgili kitabın stoğu 1 artırılır.

### 5.3. Medya ve Görsel Yönetimi
- Kitap görselleri sunucu dosya sisteminde değil, veritabanında `BLOB` (Binary Large Object) olarak tutulur.
- Görseller, `BookController` içerisindeki `/books/{id}/image` endpoint'i üzerinden byte dizisi (byte[]) olarak, `IMAGE_JPEG` içerik tipiyle istemciye sunulur.

### 5.4. Veritabanı İlklendirme (`DataInitializer.java`)
Uygulama ilk kez ayağa kalktığında:
- Veritabanında kullanıcı yoksa 1 Admin (`admin`/`admin123`) ve 1 Standart Kullanıcı (`user`/`user123`) oluşturulur.
- Veritabanında kitap yoksa varsayılan olarak çeşitli kategorilerde örnek kitaplar (Suç ve Ceza, 1984, Dune vb.) eklenir.

## 6. Dizin Yapısı ve Frontend
- **`/src/main/resources/templates/`**: HTML sayfaları (Thymeleaf şablonları).
  - `/admin/`: Admin paneli ve kitap ekleme/düzenleme formları.
  - `/books/`: Kitap listesi, detay sayfası ve kullanıcıya ait "Kitaplarım" sayfası.
  - `/auth/`: Login sayfası.
  - `/fragments/`: Tekrar kullanılabilir HTML parçaları (Header, Navbar vb.).
- **`/src/main/resources/static/`**: CSS, JavaScript ve statik resim dosyaları.
- **`application.properties`**: Port bilgisi (8081), H2 in-memory veritabanı ayarları ve Hibernate yapılandırmaları (`ddl-auto=create-drop`).

## 7. Geliştirme İpuçları
- Uygulama çalışırken H2 veritabanına `http://localhost:8081/h2-console` adresinden ulaşılabilir. (JDBC URL: `jdbc:h2:mem:librarydb`, User: `sa`, Pass: boş)
- Uygulama portu varsayılan 8080 yerine **8081** olarak ayarlanmıştır.
