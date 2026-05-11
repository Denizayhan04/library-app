# Kütüphane Yönetim Sistemi (Library Management System)

Bu proje, modern yazılım mimarisi prensipleri gözetilerek geliştirilmiş, tam yığın (full-stack) bir Kütüphane Yönetim Sistemi uygulamasıdır. Kullanıcıların kitapları inceleyip ödünç alabildiği, yöneticilerin ise envanteri ve kullanıcı işlemlerini takip edebildiği güvenli, ölçeklenebilir ve sağlam bir altyapıya sahiptir.

## Teknik Mimari ve Teknolojiler

Proje, Katmanlı Mimari (Layered Architecture) desenine uygun olarak, kodun tekrar edilebilirliğini ve sürdürülebilirliğini artırmak amacıyla tasarlanmıştır.

* **Programlama Dili:** Java 17
* **Çatı (Framework):** Spring Boot 3.2.0
* **Kimlik Doğrulama ve Yetkilendirme:** Spring Security 6 (BCrypt şifreleme ve Role-Based Access Control)
* **Veritabanı ve ORM:** PostgreSQL (Docker ortamında), Spring Data JPA (Hibernate)
* **Önyüz (Frontend):** Thymeleaf Şablon Motoru, HTML5, Vanilla CSS
* **Bağımlılık Yönetimi:** Maven

## Veritabanı Altyapısı

Projenin veri kalıcılığı, izolasyon ve performans gereksinimlerini karşılamak üzere Docker konteyneri üzerinde çalışan bir PostgreSQL sunucusu ile sağlanmaktadır.

![Docker PostgreSQL Sunucusu](images/docker-postgresql-server.png)

### Veritabanı Şeması (ERD)
Sistem temel olarak `users`, `books` ve `borrow_logs` tablolarından oluşmaktadır. Veriler arasındaki ilişkiler ve kısıtlamalar (constraints) Hibernate tarafından dinamik olarak yönetilmektedir.

![Veritabanı Şeması](images/database-schema.png)

*Not: Uygulamaya yüklenen kitap görselleri, dosya dizini yerine doğrudan PostgreSQL veritabanında `BYTEA` veri tipi ile ikili nesne (BLOB) olarak saklanmaktadır.*

## Sistem Özellikleri ve Kullanıcı Arayüzü

### 1. Kimlik Doğrulama ve Erişim Kontrolü
Sistemde yetkisiz erişimleri önlemek adına Spring Security filtresi uygulanmıştır. Ziyaretçiler yalnızca kitap listesini ve arama formunu görebilirken, işlem yapmak için giriş yapmaları gerekmektedir.

![Giriş Yapılmamış Durum](images/unauth-homepage.png)
![Kullanıcı Giriş/Kayıt Sayfası](images/auth-page.png)

### 2. Kitap Kataloğu ve Detay Görünümü
Tüm kitaplar, kullanıcı dostu bir arayüz ile listelenmektedir. Her kitap kartı, dinamik stok durumunu (Mevcut / Stokta Yok) yansıtır.

![Ana Sayfa (Kitap Listesi)](images/homepage.png)
![Kitap Kartı](images/book-card.png)

Detaylı inceleme ekranında kitabın yayın yılı, kategorisi, ISBN numarası ve açıklaması yer almaktadır. Resimler veritabanından doğrudan render edilerek arayüze basılır.

![Kitap Detay Sayfası](images/book-page.png)

### 3. Ödünç Alma ve İade Süreçleri
Oturum açmış kullanıcılar, stokta bulunan kitapları "Ödünç Al" işlemi ile üzerlerine kaydedebilirler. Ödünç alma işlemi başarılı olduğunda stok sayısı atomik olarak düşürülür.

![Ödünç Alma İşlemi](images/borrow-alert.png)

Kullanıcılar "Kitaplarım" sayfası üzerinden daha önce ödünç aldıkları kitapların listesini ve durumlarını görebilir, diledikleri zaman "İade Et" butonu ile kitapları sisteme geri kazandırabilirler.

![Kullanıcı Kitaplarım Sayfası](images/borrow-return.png)
![İade İşlemi](images/borrow-return-alert.png)

### 4. Yönetim Paneli (Admin Dashboard)
Yönetici yetkisine (`ROLE_ADMIN`) sahip kullanıcılar için özel bir gösterge paneli geliştirilmiştir. Bu panelde sistemin genel istatistikleri (toplam kitap, stok durumu, kullanıcı sayısı, aktif ödünç listesi) izlenebilmektedir.

![Yönetim Paneli](images/admin-panel.png)

Yöneticiler sistemdeki kitapların stoklarını ve görsellerini güncelleyebilir veya yeni kitaplar ekleyebilir.

![Yeni Kitap Ekleme](images/book-add.png)
![Kitap Düzenleme](images/book-edit.png)

Tüm ödünç alma ve iade etme geçmişi, silinmeden `BorrowLog` tablosu üzerinde loglanarak yöneticinin takibine sunulmaktadır.

![Ödünç Alma Geçmişi (Loglar)](images/borrow-history.png)

## Kurulum ve Çalıştırma

Projenin yerel ortamda çalıştırılabilmesi için bilgisayarınızda Java 17, Maven ve Docker kurulu olmalıdır.

1. PostgreSQL konteynerini başlatın:
   ```bash
   docker run --name library-postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_USER=postgres -e POSTGRES_DB=postgres -p 5432:5432 -d postgres:15
   ```
2. Proje dizininde terminali açıp uygulamayı derleyin ve çalıştırın:
   ```bash
   mvn clean spring-boot:run
   ```
3. Tarayıcınız üzerinden `http://localhost:8081` adresine giderek uygulamayı görüntüleyebilirsiniz. Sistem boş başlatıldığında, test verileri otomatik olarak eklenecektir.

