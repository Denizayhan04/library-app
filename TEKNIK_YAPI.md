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

### 4.4. PostgreSQL Veritabanı Kurulumu ve SQL Şeması
Projenin veritabanı altyapısı Docker üzerinde çalışan bir PostgreSQL konteyneri ile sağlanmaktadır.

**Konteyneri Başlatma Komutu:**
```bash
docker run --name library-postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_DB=postgres \
  -p 5432:5432 \
  -d postgres:15
```

**Oluşturulan Tabloların SQL Karşılıkları (Hibernate DDL):**
Uygulama ayağa kalktığında Spring Boot (Hibernate) tarafından otomatik olarak aşağıdaki SQL komutlarına eşdeğer tablolar oluşturulur:

```sql
-- Kullanıcılar Tablosu
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL
);

-- Kitaplar Tablosu
CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(255) NOT NULL UNIQUE,
    category VARCHAR(255),
    publish_year INTEGER,
    description VARCHAR(255),
    stock INTEGER NOT NULL,
    available BOOLEAN NOT NULL,
    image BYTEA -- Resimlerin binary formatta saklandığı alan
);

-- Ödünç Alma Kayıtları Tablosu
CREATE TABLE borrow_logs (
    id BIGSERIAL PRIMARY KEY,
    borrow_date TIMESTAMP,
    return_date TIMESTAMP,
    status VARCHAR(255),
    book_id BIGINT REFERENCES books(id),
    user_id BIGINT REFERENCES users(id)
);
```

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

## 8. Backend Sınıfları ve Fonksiyon Detayları

Bu bölümde projedeki tüm backend sınıfları ve içerdikleri fonksiyonların ne işe yaradığı detaylıca açıklanmıştır.

### 8.1. Controller (Sunum) Katmanı

**`AdminController.java`**
Yönetici paneli ve kitap yönetim işlemlerini karşılayan uç noktaları içerir.
*   `adminPanel(Model model)`: `/admin` sayfasına (Yönetim Paneli) girildiğinde çalışır. Veritabanından tüm kitapları, toplam kullanıcı sayısını, ödünç alınan kitap sayısını ve toplam stok miktarını hesaplayarak arayüze (Thymeleaf) gönderir.
*   `newBookForm(Model model)`: `/admin/books/new` adresine girildiğinde çalışır. Boş bir `Book` nesnesi oluşturarak "Yeni Kitap Ekle" formunu ekrana getirir.
*   `saveBook(@Valid Book book, BindingResult result, MultipartFile imageFile, ...)`: `/admin/books/save` adresine form gönderildiğinde (POST) çalışır. Gelen kitap verilerini doğrular (`@Valid`), yüklenen bir resim varsa bunu byte dizisine çevirip kitabın `image` alanına set eder, ardından veritabanına kaydeder.
*   `editBookForm(@PathVariable Long id, Model model)`: `/admin/books/edit/{id}` adresine girildiğinde çalışır. İlgili ID'ye sahip kitabı veritabanından bulup "Kitabı Düzenle" formuna doldurulmuş halde gönderir.
*   `deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes)`: `/admin/books/delete/{id}` adresine POST isteği atıldığında ilgili kitabı veritabanından siler.

**`BookController.java`**
Son kullanıcıların kitapları görüntülemesi ve ödünç alma/iade etme süreçlerini yönetir.
*   `home()`: Kök dizine (`/`) gelen istekleri direkt `/books` sayfasına yönlendirir (`redirect`).
*   `listBooks(@RequestParam String keyword, Model model)`: `/books` sayfasına girildiğinde çalışır. Eğer arama kelimesi (`keyword`) varsa isme/yazara göre filtreleme yapar, yoksa tüm kitapları listeler.
*   `searchBooks(@RequestParam String keyword)`: Arama formundan gelen `/books/search` isteklerini `/books?keyword=...` formatına yönlendirir.
*   `bookDetail(@PathVariable Long id, Model model)`: Kitap detay sayfasına girildiğinde çalışır. ID'si verilen kitabın detaylarını getirir.
*   `borrowBook(@PathVariable Long id, Authentication authentication, ...)`: Kullanıcı bir kitabı ödünç almak istediğinde çalışır. Kullanıcının giriş yapıp yapmadığını kontrol eder, giriş yapmışsa `BorrowService` üzerinden ödünç alma işlemini başlatır. Stok yetersizse hata döndürür.
*   `getBookImage(@PathVariable Long id)`: Kitapların kapak resimlerini veritabanındaki BLOB alanından okuyup `IMAGE_JPEG` formatında tarayıcıya doğrudan sunar.
*   `myBooks(Model model, Authentication authentication)`: Kullanıcının ödünç aldığı kitapları gördüğü `/my-books` sayfasını render eder. `BorrowService`'den o kullanıcının loglarını çeker.
*   `returnBook(@PathVariable Long id, Authentication authentication, ...)`: Kullanıcı kitabını iade butonuna bastığında çalışır. `BorrowService` üzerinden iade işlemini gerçekleştirir.

**`AuthController.java`**
*   `loginPage(...)` (Login İşlemi): `/login` adresine girildiğinde özel giriş (login) sayfasını gösterir. Hata veya çıkış (logout) durumlarına göre ekrana mesaj basar.

### 8.2. Service (İş Mantığı) Katmanı

**`BookService.java`**
*   `getAllBooks()`: Veritabanındaki tüm kitapları listeler.
*   `getBookById(Long id)`: ID'sine göre kitabı bulur, yoksa hata fırlatır.
*   `saveBook(Book book)`: Yeni kitabı kaydeder veya mevcut kitabı günceller.
*   `deleteBook(Long id)`: Kitabı veritabanından siler.
*   `searchBooks(String keyword)`: Anahtar kelime (keyword) doluysa arama yapar, boşsa tüm kitapları döndürür.

**`BorrowService.java`**
Bu servis kritik iş kurallarını (transaction) içerir.
*   `borrowBook(Long bookId, String username)`: `@Transactional` ile işaretlenmiştir (işlem yarıda kesilirse veritabanı geri alınır). Kitabın stokta olup olmadığını kontrol eder. Stok varsa 1 azaltır ve `BorrowLog` tablosuna "BORROWED" durumunda yeni bir kayıt atar.
*   `getUserBorrowLogs(String username)`: Belirtilen kullanıcının geçmişten bugüne tüm ödünç alma/iade kayıtlarını tarihe göre azalan şekilde (en yeni en üstte) listeler.
*   `returnBook(Long logId, String username)`: `@Transactional` içerir. Kaydın ilgili kullanıcıya ait olup olmadığını ve zaten iade edilip edilmediğini kontrol eder. İade gerçekleşirse kitabın stoğunu 1 artırır ve log kaydının durumunu "RETURNED" yapıp iade tarihini atar.

**`CustomUserDetailsService.java`**
*   `loadUserByUsername(String username)`: Spring Security'nin login olurken çağırdığı fonksiyondur. Veritabanında (`UserRepository`) o kullanıcı adını arar. Bulursa Spring Security'nin anlayacağı `UserDetails` nesnesine dönüştürür.

### 8.3. Repository (Veri Erişim) Katmanı
Spring Data JPA kullanıldığı için bu sınıflar `JpaRepository`'den türetilmiş arayüzlerdir (Interface), gövdeleri ve SQL sorguları (büyük oranda) Spring tarafından otomatik doldurulur.

**`BookRepository.java`**
*   `search(String keyword)`: `@Query` anotasyonuyla özel JPQL yazılmıştır. Kitap isminde, yazarda veya kategoride aranan kelime geçen kitapları getirir.
*   `sumStock()`: Veritabanındaki tüm kitapların toplam stok adedini hesaplar (`SUM(b.stock)`).

**`BorrowLogRepository.java`**
*   `findByUserUsernameOrderByBorrowDateDesc(String username)`: Sadece belli bir kullanıcıya ait kayıtları getirir.
*   `countByStatus(String status)`: Belli bir statüdeki ("BORROWED" vb.) toplam kayıt sayısını döner (Admin paneli istatistiği için).
*   `findAllByOrderByBorrowDateDesc()`: Tüm kayıtları kronolojik (en yeni en üstte) sıralı getirir.

**`UserRepository.java`**
*   `findByUsername(String username)`: Login işlemi sırasında kullanıcı adıyla veritabanından kullanıcı nesnesini çeker.

### 8.4. Config (Yapılandırma) Katmanı

**`SecurityConfig.java`**
*   `passwordEncoder()`: Şifrelerin açık metin kalmaması için `BCryptPasswordEncoder` nesnesi üretir.
*   `authenticationProvider()`: Veritabanı kimlik doğrulaması (`CustomUserDetailsService` ve `PasswordEncoder`) konfigürasyonunu sisteme tanıtır.
*   `filterChain(HttpSecurity http)`: Sistemin kalbidir. Hangi URL'lere kimlerin girebileceğini belirler, login formunun yönlendirmelerini yapar, CSRF ve FrameOptions gibi güvenlik kurallarını ayarlar.

**`DataInitializer.java`**
*   `initData(...)`: Spring Boot başlarken (`CommandLineRunner`) otomatik çalışır. Veritabanı boşsa varsayılan admin (`admin`/`admin123`), user (`user`/`user123`) hesaplarını ve 8 adet örnek kitabı veritabanına kaydeder.
