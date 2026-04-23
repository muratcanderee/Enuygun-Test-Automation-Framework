package com.enuygun.ui.pages;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HomePage extends BasePage {

  private final By acceptCookiesBtn = By.id("onetrust-accept-btn-handler");
  private final By cookieBanner = By.id("onetrust-banner-sdk");
  private final By roundTripRadio = By.cssSelector("input[data-testid='search-round-trip-input']");

  private final By fromInput =
      By.cssSelector("input[data-testid='endesign-flight-origin-autosuggestion-input']");
  private final By toInput =
      By.cssSelector("input[data-testid='endesign-flight-destination-autosuggestion-input']");

  private final By departDateInput =
      By.cssSelector("input[data-testid='enuygun-homepage-flight-departureDate-datepicker-input']");
  private final By returnDateInput =
      By.cssSelector("input[data-testid='enuygun-homepage-flight-returnDate-datepicker-input']");

  private final By searchBtn =
      By.cssSelector("button[data-testid='enuygun-homepage-flight-submitButton'], button[type='submit']");

  public HomePage(WebDriver driver) {
    super(driver);
  }

  public HomePage navigateTo(String baseUrl) {
    String url = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    driver.get(url);
    waitForDocumentReady();
    acceptCookies();
    return this;
  }

  public void acceptCookies() {
    try {
      WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(10));

      try {
        shortWait.until(ExpectedConditions.visibilityOfElementLocated(cookieBanner));
      } catch (TimeoutException ignored) {
        return;
      }

      try {
        shortWait.until(ExpectedConditions.elementToBeClickable(acceptCookiesBtn)).click();
      } catch (Exception ignored) {
        jsClick(shortWait.until(ExpectedConditions.presenceOfElementLocated(acceptCookiesBtn)));
      }

      try {
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.invisibilityOfElementLocated(cookieBanner));
      } catch (TimeoutException ignored) {
      }
    } catch (Exception ignored) {
    }
  }

  public HomePage setRoute(String fromCity, String toCity) {
    try {
      jsClick(visible(roundTripRadio));
    } catch (Exception ignored) {
    }

    selectFromAutosuggestion(fromInput, fromCity);
    selectFromAutosuggestion(toInput, toCity);
    return this;
  }

  public HomePage setDates(LocalDate depart, LocalDate ret) {
    try {
      jsClick(visible(departDateInput));
    } catch (Exception ignored) {
    }
    type(departDateInput, depart.toString());
    visible(departDateInput).sendKeys(Keys.ENTER);

    try {
      jsClick(visible(returnDateInput));
    } catch (Exception ignored) {
    }
    type(returnDateInput, ret.toString());
    visible(returnDateInput).sendKeys(Keys.ENTER);
    return this;
  }

  public FlightResultsPage search() {
    try {
      jsClick(visible(searchBtn));
    } catch (Exception ignored) {
      tryClick(searchBtn);
    }
    switchToLatestWindowIfOpened(Duration.ofSeconds(20));
    return new FlightResultsPage(driver).waitForResults();
  }

  private void selectFromAutosuggestion(By input, String query) {
    WebElement el = visible(input);
    try {
      jsClick(el);
    } catch (Exception ignored) {
    }
    type(input, query);

    By options = By.xpath("//*[@role='listbox']//*[@role='option' or self::li]");
    wait.until(d -> !d.findElements(options).isEmpty());

    String q = query.toLowerCase();
    for (int attempt = 0; attempt < 3; attempt++) {
      List<WebElement> opts = presentAll(options);
      for (WebElement opt : opts) {
        try {
          String t = opt.getText();
          if (t != null && t.toLowerCase().contains(q)) {
            jsClick(opt);
            return;
          }
        } catch (Exception ignored) {
        }
      }
      try {
        jsClick(opts.get(0));
        return;
      } catch (Exception ignored) {
      }
    }
  }
}

