package com.enuygun.ui.tests;

import com.enuygun.ui.driver.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class BaseUiTest {
  protected WebDriver driver;

  @BeforeMethod
  void setUp() {
    driver = DriverFactory.createDriver();
  }

  @AfterMethod(alwaysRun = true)
  void tearDown() {
    if (driver != null) {
      driver.quit();
    }
  }

  public WebDriver getDriver() {
    return driver;
  }
}
