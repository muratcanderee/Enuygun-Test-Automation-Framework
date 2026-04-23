# Enuygun Case Study (UI + API + Load)

Bu repo, Enuygun uçak bileti arama modülü için **UI (Part 1)**, **API (Part 2)** ve **Load (Part 3)** testlerini aynı çatı altında içerir.

## Teknolojiler

- Java 17
- Selenium 4
- TestNG
- WebDriverManager (driver yönetimi)
- Allure (test raporu + screenshot attachment)
- REST Assured (API)
- k6 (Load)

## Kurulum

- Java 17 kurulu olmalı
- Maven 3.9+ önerilir

Bağımlılıkları indir ve derle:

```bash
mvn -q -DskipTests compile test-compile
```

## Konfigürasyon

Varsayılan değerler `src/main/resources/config.properties` içindedir.

İstersen koşum sırasında override edebilirsin:

- System property: `-DbaseUrl=...`, `-DpetstoreBaseUrl=...`, `-Dbrowser=...`, `-Dheadless=...`
- Environment variable: `baseUrl`, `PETSTORE_BASE_URL`

## Part 1: UI Tests

```bash
mvn test
```

UI testlerini tek başına çalıştırma (suite):

```bash
mvn -q test -Dsurefire.suiteXmlFiles=src/test/resources/testng.xml
```

Allure raporu:

```bash
mvn allure:serve
```

## Part 2: API Tests (Petstore)

Pet CRUD (positive + negative + schema + log):

```bash
mvn -q -Dtest=PetstorePetCrudTest test
```

Schema dosyaları:
- `src/main/resources/schemas/petstore/pet.json`
- `src/main/resources/schemas/petstore/api-response.json`

## Part 3: Load Testing (k6)

Senaryo: `load-test/k6/search.js` — önce `BASE_URL` anasayfası (`tags.name: homepage`), kısa bekleme, sonra uçuş arama sonuç URL’si (`flight_search`). k6 raporunda süreler bu iki adım için ayrı metriklerde görünür (`http_req_duration{name:...}`). İki istek arası bekleme süresi (saniye): `THINK_TIME_SEC` (varsayılan `1`).

### k6 Kurulum

macOS (Homebrew):

```bash
brew install k6
```

Windows:
- Chocolatey: `choco install k6`
- Winget: `winget install k6.k6`

1 kullanıcı ile çalıştırma:

```bash
# Proje kökünden çalıştırın (göreli yollar buna göre)
k6 run load-test/k6/search.js --vus 1 --duration 30s
```

Raporlama:
- Konsol çıktısı: **response time** (avg/p95) ve **error rate** (`http_req_failed`)
- `handleSummary`: `load-test/k6/reports/summary_<timestamp>.json` ve `load-test/k6/reports/report_<timestamp>.html`

İstersen URL / davranış override:

```bash
BASE_URL="https://www.enuygun.com" SEARCH_URL="https://www.enuygun.com/ucak-bileti/arama/..." THINK_TIME_SEC=2 k6 run load-test/k6/search.js
```

## Proje Yapısı

- `src/main/java`: framework kodu (UI pages/driver/config, ortak config reader)
- `src/test/java`: test senaryoları (UI + API)
- `src/main/resources`: config + schema
- `load-test/`: load senaryoları

## Notlar

- UI tarafında wait stratejileri `WebDriverWait` ile.
