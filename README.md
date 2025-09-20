# TT Expertise - Araç Ekspertiz Yönetim Sistemi

Mobil uygulamalar için geliştirilmiş, araç ekspertiz süreçlerini yöneten Spring Boot tabanlı bir backend servisidir. Kurumsal seviyede güvenlik, performans ve ölçeklenebilirlik özellikleri ile donatılmıştır.

## Özellikler

- **RESTful API** - Mobil uygulamalar için optimize edilmiş API
- **Idempotency** - Aynı isteğin tekrar gönderilmesini engelleyen güvenlik katmanı
- **Optimistic Locking** - Eşzamanlı veri erişiminde veri tutarlılığı
- **Kapsamlı Test** - Unit, Controller ve Integration testleri
- **API Dokümantasyonu** - Swagger UI ile interaktif dokümantasyon
- **Docker Desteği** - Kolay deployment ve geliştirme ortamı
- **Production Ready** - Canlı ortam için hazır konfigürasyon
- **Temiz Mimari** - SOLID prensipleri ile geliştirilmiş

## Proje Yapısı

Proje, katmanlı mimari prensiplerine uygun olarak tasarlanmıştır:

```
src/
├── main/java/com/ttexpertise/
│   ├── business/           # İş mantığı katmanı
│   ├── controller/         # REST API kontrolcüleri
│   ├── model/             # Veri modelleri ve DTO'lar
│   ├── repository/        # Veri erişim katmanı
│   └── service/           # Servis implementasyonları
└── test/                  # Test dosyaları
```

## Kullanılan Teknolojiler

- **Java 21** - Modern Java özellikleri ile
- **Spring Boot 3.5.6** - Hızlı geliştirme framework'ü
- **PostgreSQL** - Güvenilir veritabanı sistemi
- **Redis** - Yüksek performanslı cache sistemi
- **Docker** - Konteyner tabanlı deployment
- **JUnit 5** - Test framework'ü
- **Swagger UI** - API dokümantasyonu

## Hızlı Başlangıç

### Gereksinimler

- Java 21 veya üzeri
- Maven 3.9 veya üzeri
- Docker ve Docker Compose

### Uygulamayı Çalıştırma

1. **Projeyi klonlayın**
   ```bash
   git clone https://github.com/berkefiratto/tt-expertise.git
   cd tt-expertise
   ```

2. **PostgreSQL'i Docker ile başlatın**
   ```bash
   docker-compose up -d
   ```

3. **Uygulamayı çalıştırın**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **API'ye erişim**
   - API Adresi: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - Sağlık Kontrolü: `http://localhost:8080/actuator/health`

## API Endpoints

### Ekspertiz Yönetimi

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| `GET` | `/api/v1/expertises/{carId}` | Belirli bir araç için son ekspertizi getir |
| `POST` | `/api/v1/expertises` | Yeni ekspertiz oluştur |

### Örnek İstekler

**Ekspertiz Getir:**
```bash
curl -X GET "http://localhost:8080/api/v1/expertises/CAR123" \
  -H "Accept: application/json"
```

**Ekspertiz Oluştur:**
```bash
curl -X POST "http://localhost:8080/api/v1/expertises" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: benzersiz-anahtar-123" \
  -d '{
    "carId": "CAR123",
    "answers": [
      {
        "questionId": 1,
        "value": true,
        "description": "Ön tamponda küçük çizik",
        "photoUrls": ["https://example.com/photo1.jpg", "https://example.com/photo2.jpg"]
      }
    ]
  }'
```

## Test Süreci

### Tüm Testleri Çalıştır
```bash
./mvnw test
```

### Test Türlerine Göre Çalıştır
```bash
# Sadece unit testler
./mvnw test -Dtest=*Test

# Sadece integration testler
./mvnw test -Dtest=*IntegrationTest

# Sadece controller testler
./mvnw test -Dtest=*ControllerTest
```

### Test Kapsamı
- **Unit Testler**: 6/6 ✅
- **Controller Testler**: 4/4 ✅
- **Integration Testler**: 3/3 ✅

## Konfigürasyon

### Ortam Değişkenleri

| Değişken | Varsayılan | Açıklama |
|----------|------------|----------|
| `DB_URL` | `jdbc:postgresql://localhost:5433/tt_expertise` | Veritabanı adresi |
| `DB_USER` | `postgres` | Veritabanı kullanıcı adı |
| `DB_PASS` | `postgres` | Veritabanı şifresi |
| `REDIS_HOST` | `localhost` | Redis sunucu adresi |
| `REDIS_PORT` | `6379` | Redis port numarası |
| `IDEMPOTENCY_TYPE` | `memory` | Idempotency depolama türü (`memory` veya `redis`) |

### Uygulama Profilleri

- **Development**: Geliştirme ortamı için in-memory idempotency
- **Production**: Canlı ortam için Redis tabanlı idempotency

## Canlı Ortam Deployment

### Docker Compose ile Production

```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - DB_URL=jdbc:postgresql://postgres:5432/tt_expertise
      - REDIS_HOST=redis
      - IDEMPOTENCY_TYPE=redis
    depends_on:
      - postgres
      - redis

  postgres:
    image: postgres:16.10
    environment:
      POSTGRES_DB: tt_expertise
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
```

## Kurumsal Özellikler

### Idempotency (Tekrarlama Koruması)
- **Geliştirme**: Bellek tabanlı depolama
- **Canlı Ortam**: Redis tabanlı dağıtık depolama
- **TTL**: Yapılandırılabilir süre (varsayılan: 1 saat)

### Optimistic Locking (İyimser Kilitleme)
- **Versiyon Alanı**: `@Version` ile otomatik versiyonlama
- **Eşzamanlı Güncellemeler**: Aynı anda yapılan değişiklikleri yönetir
- **Hata Yönetimi**: `ObjectOptimisticLockingFailureException` ile

### API Dokümantasyonu
- **Swagger UI**: Etkileşimli API dokümantasyonu
- **OpenAPI 3.0**: Makine tarafından okunabilir API spesifikasyonu
- **Örnekler**: Kapsamlı istek/yanıt örnekleri

## İzleme ve Sağlık Kontrolleri

- **Sağlık Endpoint**: `/actuator/health`
- **Metrikler**: `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`

## Katkıda Bulunma

1. Repository'yi fork edin
2. Feature branch oluşturun (`git checkout -b feature/yeni-ozellik`)
3. Değişikliklerinizi commit edin (`git commit -m 'Yeni özellik eklendi'`)
4. Branch'i push edin (`git push origin feature/yeni-ozellik`)
5. Pull Request açın

## Lisans

Bu proje MIT Lisansı altında lisanslanmıştır - detaylar için [LICENSE](LICENSE) dosyasına bakın.

## Geliştirici

**Berke Fırat Yıldırım**
- GitHub: [@berkefiratto](https://github.com/berkefiratto)
- LinkedIn: [Berke Fırat Yıldırım](https://linkedin.com/in/berkefiratyildirim)

## Proje Durumu

- ✅ **Temel Özellikler**: Tamamlandı
- ✅ **Test Kapsamı**: Kapsamlı test coverage
- ✅ **Dokümantasyon**: Swagger ile API dokümantasyonu
- ✅ **Production Ready**: Kurumsal pattern'ler implement edildi
- ✅ **Docker Desteği**: Konteyner tabanlı deployment

---

**Spring Boot ve modern Java pratikleri ile geliştirilmiştir**
