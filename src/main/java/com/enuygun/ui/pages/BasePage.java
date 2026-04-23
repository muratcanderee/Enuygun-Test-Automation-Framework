package com.enuygun.ui.pages;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class BasePage {
  protected final WebDriver driver;
  protected final WebDriverWait wait;

  protected BasePage(WebDriver driver) {
    this.driver = driver;
    this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
  }

  protected WebElement visible(By by) {
    return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
  }

  protected WebElement clickable(By by) {
    return wait.until(ExpectedConditions.elementToBeClickable(by));
  }

  protected List<WebElement> presentAll(By by) {
    return driver.findElements(by);
  }

  protected void click(By by) {
    clickable(by).click();
  }

  protected void type(By by, String text) {
    WebElement el = visible(by);
    try {
      el.sendKeys(Keys.chord(Keys.COMMAND, "a"));
      el.sendKeys(Keys.BACK_SPACE);
      el.sendKeys(text);
    } catch (InvalidElementStateException e) {
      jsSetValue(el, text);
    }
  }

  protected void jsSetValue(WebElement el, String value) {
    ((JavascriptExecutor) driver)
        .executeScript(
            "arguments[0].value = arguments[1];"
                + "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));"
                + "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));",
            el,
            value);
  }

  protected void jsClick(WebElement el) {
    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
  }

  protected boolean tryClick(By by) {
    try {
      driver.findElement(by).click();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  protected void waitForUrlContains(String part) {
    wait.until(ExpectedConditions.urlContains(part));
  }

  protected void waitForDocumentReady() {
    try {
      new WebDriverWait(driver, Duration.ofSeconds(30))
          .until(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
    } catch (TimeoutException ignored) {
    }
  }

  protected void switchToLatestWindowIfOpened(Duration timeout) {
    String original = driver.getWindowHandle();
    WebDriverWait w = new WebDriverWait(driver, timeout);
    boolean opened =
        w.until(
            d -> {
              try {
                return d.getWindowHandles().size() > 1;
              } catch (Exception e) {
                return false;
              }
            });

    if (!opened) return;

    List<String> handles = new ArrayList<>(driver.getWindowHandles());
    for (int i = handles.size() - 1; i >= 0; i--) {
      String h = handles.get(i);
      if (!h.equals(original)) {
        driver.switchTo().window(h);
        waitForDocumentReady();
        return;
      }
    }
  }
}

