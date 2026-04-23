# Enuygun Case Study (UI + API + Load)

Bu proje; Enuygun uçak bileti arama modülü için kurgulanmış, **UI**, **API** ve **performans** katmanlarını birleştiren bir test otomasyon iskeletidir.

## Teknolojiler

- **UI:** Java 17, Selenium 4, TestNG, WebDriverManager
- **API:** REST Assured (CRUD ve JSON Schema doğrulama)
- **Performans:** k6
- **Raporlama:** Allure (UI/API), k6 HTML reporter

## Proje yapısı

- `src/main/java` — sayfa nesneleri, driver factory, `ConfigReader`
- `src/test/java` — UI ve API testleri
- `src/main/resources` — `config.properties`, JSON şemaları
- `src/test/resources` — TestNG suite dosyaları (`testng-all.xml`, `testng-ui.xml`, `testng-api.xml`)
- `load-test/k6/` — yük senaryosu ve rapor çıktıları

## Kurulum

Java 17 ve Maven gerekir.

```bash
mvn clean install
```

## Koşum

Varsayılan: `mvn test` → `testng-all.xml` ile UI + API.

| Kapsam | Komut |
|--------|--------|
| Tam suite | `mvn test` |
| Yalnızca UI | `mvn test -Dsurefire.suiteXmlFiles=src/test/resources/testng-ui.xml` |
| Yalnızca API | `mvn test -Dsurefire.suiteXmlFiles=src/test/resources/testng-api.xml` |
| Tek test sınıfı | `mvn test -Dtest=SınıfAdı` (gerekirse yukarıdaki `-Dsurefire.suiteXmlFiles=...` ile birlikte) |

## Raporlama

**Allure (UI/API):** koşumdan sonra örneğin `mvn allure:serve`. Statik rapor için `allure:report`; rapor klasöründe gerçek arayüz için **`index.html`** dosyasını açın.

Not: Hata durumlarında UI testleri için ekran görüntüsü Allure raporuna eklenir.

**k6:** raporlar `load-test/k6/reports/` altında üretilir.

```bash
k6 run load-test/k6/search.js --vus 1 --duration 30s
```

İsteğe bağlı ortam değişkenleri: `BASE_URL`, `SEARCH_URL`, `THINK_TIME_SEC`.

## Konfigürasyon

`src/main/resources/config.properties` veya JVM parametreleri (`-Danahtar=değer`).

| Parametre | Örnek / açıklama |
|-----------|------------------|
| `browser` | `chrome`, `firefox`, … |
| `headless` | `true` / `false` |
| `baseUrl` | Örn. `https://www.enuygun.com` |
| `fromCity` / `toCity` | Örn. `Istanbul`, `Ankara` |
| `departDate` / `returnDate` | `YYYY-MM-DD` |
| `departureTimeFilterStart` / `departureTimeFilterEnd` | Kalkış saat filtresi, `HH:mm` (örn. `10:00`, `18:00`) |

Örnek:

```bash
mvn test -Dsurefire.suiteXmlFiles=src/test/resources/testng-ui.xml \
  -DdepartureTimeFilterStart=09:00 \
  -DdepartureTimeFilterEnd=15:00
```

## Öne çıkanlar

- Ortak konfigürasyon (`ConfigReader` + `TestConfig`)
- API yanıtları için JSON Schema doğrulama
- k6 ve UI senaryolarında dinamik tarih / URL kullanımı
- UI’da senkronizasyon için `WebDriverWait` kullanımı
