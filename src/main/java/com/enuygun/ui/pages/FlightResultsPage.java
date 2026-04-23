package com.enuygun.ui.pages;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class FlightResultsPage extends BasePage {
  private static final Pattern TIME_PATTERN = Pattern.compile("\\b([01]?\\d|2[0-3]):[0-5]\\d\\b");
  private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("H:mm", Locale.ROOT);

  private final By flightCards =
      By.cssSelector(
          ".flight-list-body .flight-item, [data-testid*='flight-card'], [data-testid*='result-card'], [data-testid*='flight-result']");

  private static final By DEPARTURE_TIME_CELL = By.cssSelector("[data-testid='departureTime']");
  private static final By FLIGHT_INFO_PRICE = By.cssSelector("[data-testid='flightInfoPrice'][data-price]");
  private static final By SUMMARY_AIRLINE_MARKETING =
      By.cssSelector(".summary-airline .summary-marketing-airlines");

  private final By departureTimeFilterCardHeader =
      By.cssSelector(".ctx-filter-departure-return-time.card-header");

  private final By departureDepartureTimeSliderRoot =
      By.cssSelector("[data-testid='departureDepartureTimeSlider']");
  private final By rcSliderHandles = By.cssSelector(".rc-slider-handle");

  private final By airlineFilterCardHeader = By.cssSelector(".ctx-filter-airline.card-header");

  private static final By RESULT_SORTING_CHEAPEST =
      By.cssSelector("[data-testid='resultSorting'] [data-testid='sortButtons0']");

  public FlightResultsPage(WebDriver driver) {
    super(driver);
  }

  public FlightResultsPage waitForResults() {
    waitForDocumentReady();
    return this;
  }

  public FlightResultsPage assertRouteInUrl(String fromCity, String toCity) {
    String url = driver.getCurrentUrl().toLowerCase(Locale.ROOT);
    assertThat(url).contains(fromCity.toLowerCase(Locale.ROOT));
    assertThat(url).contains(toCity.toLowerCase(Locale.ROOT));
    return this;
  }

  public FlightResultsPage applyDepartureTimeFilter(LocalTime start, LocalTime end) {
    expandDepartureArrivalTimeFilterIfCollapsed();

    WebElement sliderRoot = visible(departureDepartureTimeSliderRoot);
    List<WebElement> handles = sliderRoot.findElements(rcSliderHandles);
    assertThat(handles)
        .as("Expected 2 rc-slider handles under departureDepartureTimeSlider")
        .hasSizeGreaterThanOrEqualTo(2);

    final int startHour = clampHour(start.getHour());
    int endResolved = clampHour(end.getHour());
    if (endResolved <= startHour) {
      endResolved = Math.min(23, startHour + 1);
    }
    final int endHour = endResolved;

    boolean hoursInAria = readAriaInt(handles.get(0), "aria-valuemax", 1440) <= 24;
    int startTarget = hoursInAria ? startHour : startHour * 60;
    int endTarget = hoursInAria ? endHour : endHour * 60;

    new WebDriverWait(driver, Duration.ofSeconds(35))
        .until(
            d -> {
              try {
                WebElement root = d.findElement(departureDepartureTimeSliderRoot);
                if (departureSliderHoursMatch(root, hoursInAria, startHour, endHour)) {
                  return true;
                }
                dragDepartureHandle(root, 0, startTarget);
                dragDepartureHandle(root, 1, endTarget);
                return false;
              } catch (Exception ex) {
                return false;
              }
            });

    return this;
  }

  public FlightResultsPage filterByAirline(String airlineName) {
    expandAirlineFilterIfCollapsed();
    By airlineCheckbox =
        By.xpath(
            "//div[contains(@class,'ctx-filter-airline')]/following-sibling::div//label[contains(normalize-space(.),\""
                + airlineName
                + "\")]//input[@type='checkbox']");
    WebElement input =
        new WebDriverWait(driver, Duration.ofSeconds(15))
            .until(ExpectedConditions.presenceOfElementLocated(airlineCheckbox));
    jsClick(input);
    waitUntilFlightCardsShowAirline(airlineName.trim());
    waitForDocumentReady();
    return this;
  }

  private void waitUntilFlightCardsShowAirline(String expected) {
    new WebDriverWait(driver, Duration.ofSeconds(30))
        .until(
            ignored -> {
              try {
                return allFlightCardsShowMarketingAirline(expected);
              } catch (Exception e) {
                return false;
              }
            });
  }

  private boolean allFlightCardsShowMarketingAirline(String expected) {
    List<WebElement> cards = displayedCards();
    if (cards.isEmpty()) {
      return false;
    }
    for (WebElement card : cards) {
      String shown = card.findElement(SUMMARY_AIRLINE_MARKETING).getText().trim();
      if (!expected.equals(shown)) {
        return false;
      }
    }
    return true;
  }

  public List<WebElement> displayedCards() {
    List<WebElement> rows = presentAll(flightCards);
    List<WebElement> withDepartureCell = new ArrayList<>();
    for (WebElement row : rows) {
      try {
        if (!row.findElements(DEPARTURE_TIME_CELL).isEmpty()) {
          withDepartureCell.add(row);
        }
      } catch (Exception ignored) {
      }
    }
    if (!withDepartureCell.isEmpty()) {
      return withDepartureCell;
    }
    List<WebElement> legacy = new ArrayList<>();
    for (WebElement el : rows) {
      try {
        String txt = el.getText();
        if (txt != null && txt.length() > 40 && TIME_PATTERN.matcher(txt).find()) {
          legacy.add(el);
        }
      } catch (Exception ignored) {
      }
    }
    return legacy.isEmpty() ? rows : legacy;
  }

  public List<LocalTime> extractDepartureTimes() {
    List<LocalTime> times = new ArrayList<>();
    for (WebElement card : displayedCards()) {
      try {
        List<WebElement> depCells = card.findElements(DEPARTURE_TIME_CELL);
        if (!depCells.isEmpty()) {
          String text = depCells.get(0).getText().trim();
          if (!text.isEmpty()) {
            times.add(LocalTime.parse(text, HH_MM));
          }
          continue;
        }
        String full = card.getText();
        Matcher m = TIME_PATTERN.matcher(full);
        if (m.find()) {
          times.add(LocalTime.parse(m.group(), HH_MM));
        }
      } catch (Exception ignored) {
      }
    }
    return times;
  }

  public FlightResultsPage assertAllDeparturesBetween(LocalTime start, LocalTime end) {
    List<LocalTime> times = extractDepartureTimes();
    assertThat(times)
        .as("No departure times (data-testid=departureTime) could be read from flight rows")
        .isNotEmpty();
    assertThat(times)
        .allSatisfy(
            t ->
                assertThat(!t.isBefore(start) && !t.isAfter(end))
                    .as(
                        "Kalkış %s, %s ile %s aralığında olmalı (karttaki departureTime)",
                        t,
                        start,
                        end)
                    .isTrue());
    return this;
  }

  public FlightResultsPage assertAirlineVisibleInAllCards(String airlineName) {
    List<WebElement> cards = displayedCards();
    assertThat(cards).isNotEmpty();
    String expected = airlineName.trim();
    for (WebElement card : cards) {
      String shown = card.findElement(SUMMARY_AIRLINE_MARKETING).getText().trim();
      assertThat(shown).isEqualTo(expected);
    }
    return this;
  }

  public List<BigDecimal> extractFlightInfoPricesOrdered() {
    List<BigDecimal> prices = new ArrayList<>();
    for (WebElement row : displayedCards()) {
      try {
        List<WebElement> nodes = row.findElements(FLIGHT_INFO_PRICE);
        if (nodes.isEmpty()) {
          continue;
        }
        String raw = nodes.get(0).getAttribute("data-price");
        if (raw == null || raw.trim().isEmpty()) {
          continue;
        }
        prices.add(new BigDecimal(raw.trim().replace(',', '.')));
      } catch (Exception ignored) {
      }
    }
    return prices;
  }

  public FlightResultsPage assertPricesAscending() {
    List<BigDecimal> prices = extractFlightInfoPricesOrdered();
    assertThat(prices).as("flightInfoPrice[data-price] kartlardan okunamadı").isNotEmpty();
    assertThat(prices)
        .as("Her kart fiyatı listede bir sonraki karttan düşük veya eşit olmalı (data-price sırası)")
        .isSortedAccordingTo(Comparator.naturalOrder());
    return this;
  }

  public FlightResultsPage sortByPriceAscending() {
    waitForDocumentReady();
    WebElement cheapest = visible(RESULT_SORTING_CHEAPEST);
    String cls = cheapest.getAttribute("class");
    if (cls == null || !cls.contains("active")) {
      jsClick(cheapest);
      new WebDriverWait(driver, Duration.ofSeconds(15))
          .until(
              d -> {
                try {
                  String c = d.findElement(RESULT_SORTING_CHEAPEST).getAttribute("class");
                  return c != null && c.contains("active");
                } catch (Exception e) {
                  return false;
                }
              });
    }
    assertThat(driver.findElement(RESULT_SORTING_CHEAPEST).getAttribute("class"))
        .as("En ucuz sıralaması aktif olmalı (resultSorting / sortButtons0)")
        .contains("active");
    waitForDocumentReady();
    return this;
  }

  private void expandAirlineFilterIfCollapsed() {
    try {
      WebElement header = visible(airlineFilterCardHeader);
      WebElement card =
          header.findElement(By.xpath("./ancestor::div[contains(@class,'filter-card')]"));
      WebElement collapse = card.findElement(By.cssSelector(".collapse"));
      String cls = collapse.getAttribute("class");
      if (cls == null || !cls.contains("show")) {
        jsClick(header);
      }
    } catch (Exception ignored) {
    }
  }

  private void expandDepartureArrivalTimeFilterIfCollapsed() {
    try {
      WebElement header = driver.findElement(departureTimeFilterCardHeader);
      List<WebElement> collapse = driver.findElements(By.cssSelector(".filter-card.card .collapse"));
      boolean collapsed =
          collapse.stream()
              .anyMatch(
                  el -> {
                    try {
                      String cls = el.getAttribute("class");
                      return cls != null && !cls.contains("show");
                    } catch (Exception e) {
                      return false;
                    }
                  });
      if (collapsed) {
        jsClick(header);
      }
    } catch (Exception ignored) {
    }
  }

  private static int clampHour(int hour) {
    if (hour < 0) return 0;
    if (hour > 23) return 23;
    return hour;
  }

  private void dragDepartureHandle(WebElement sliderRoot, int handleIndex, int targetValue) {
    WebElement rail = sliderRoot.findElement(By.cssSelector(".rc-slider-rail"));
    org.openqa.selenium.Rectangle railRect = rail.getRect();

    List<WebElement> hs = sliderRoot.findElements(rcSliderHandles);
    if (hs.size() <= handleIndex) return;

    WebElement handle = hs.get(handleIndex);
    org.openqa.selenium.Rectangle handleRect = handle.getRect();

    int min = readAriaInt(handle, "aria-valuemin", 0);
    int max = readAriaInt(handle, "aria-valuemax", 1440);
    int clamped = Math.max(min, Math.min(max, targetValue));

    double pct = (clamped - min) / (double) Math.max(1, (max - min));
    int targetAbsX = railRect.getX() + (int) Math.round(pct * Math.max(1, railRect.getWidth()));
    int targetAbsY = railRect.getY() + Math.max(1, railRect.getHeight() / 2);

    int startAbsX = handleRect.getX() + handleRect.getWidth() / 2;
    int startAbsY = handleRect.getY() + handleRect.getHeight() / 2;

    new Actions(driver)
        .moveToLocation(startAbsX, startAbsY)
        .pause(Duration.ofMillis(50))
        .clickAndHold()
        .pause(Duration.ofMillis(50))
        .moveToLocation(targetAbsX, targetAbsY)
        .pause(Duration.ofMillis(50))
        .release()
        .perform();
  }

  private static int readAriaInt(WebElement el, String attr, int def) {
    try {
      String v = el.getAttribute(attr);
      if (v == null) return def;
      return Integer.parseInt(v.trim());
    } catch (Exception e) {
      return def;
    }
  }

  private static int hourFromSliderValue(boolean hoursInAria, int raw) {
    if (raw < 0) return -1;
    return hoursInAria ? raw : raw / 60;
  }

  private boolean departureSliderHoursMatch(
      WebElement sliderRoot, boolean hoursInAria, int startHour, int endHour) {
    List<WebElement> hs = sliderRoot.findElements(rcSliderHandles);
    if (hs.size() < 2) return false;
    int left = readAriaInt(hs.get(0), "aria-valuenow", -1);
    int right = readAriaInt(hs.get(1), "aria-valuenow", -1);
    return hourFromSliderValue(hoursInAria, left) == startHour
        && hourFromSliderValue(hoursInAria, right) == endHour;
  }
}

