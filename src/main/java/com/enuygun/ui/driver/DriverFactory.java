package com.enuygun.ui.driver;

import com.enuygun.ui.config.TestConfig;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public final class DriverFactory {
  private DriverFactory() {}

  public static WebDriver createDriver() {
    String browser = TestConfig.browser();
    boolean headless = TestConfig.headless();

    WebDriver driver;
    switch (browser) {
      case "firefox":
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions ff = new FirefoxOptions();
        if (headless) ff.addArguments("-headless");
        driver = new FirefoxDriver(ff);
        break;
      case "chrome":
      default:
        WebDriverManager.chromedriver().setup();
        ChromeOptions ch = new ChromeOptions();
        if (headless) ch.addArguments("--headless=new");
        ch.addArguments("--window-size=1440,900");
        ch.addArguments("--disable-gpu");
        ch.addArguments("--no-sandbox");
        driver = new ChromeDriver(ch);
        break;
    }

    driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
    driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
    driver.manage().window().maximize();
    return driver;
  }
}

