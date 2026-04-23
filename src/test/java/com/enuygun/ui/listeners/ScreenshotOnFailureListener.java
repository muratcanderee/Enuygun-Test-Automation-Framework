package com.enuygun.ui.listeners;

import com.enuygun.ui.tests.BaseUiTest;
import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ScreenshotOnFailureListener implements ITestListener {
  private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

  @Override
  public void onTestFailure(ITestResult result) {
    Object instance = result.getInstance();
    if (!(instance instanceof BaseUiTest)) {
      return;
    }

    WebDriver driver = ((BaseUiTest) instance).getDriver();
    if (driver == null) {
      return;
    }

    try {
      byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
      Allure.addAttachment("failure-screenshot", "image/png", new ByteArrayInputStream(png), ".png");

      Path dir = Paths.get("artifacts", "screenshots");
      Files.createDirectories(dir);
      String testName = result.getMethod().getMethodName().replaceAll("[^a-zA-Z0-9._-]", "_");
      Path out = dir.resolve(testName + "_" + LocalDateTime.now().format(TS) + ".png");
      Files.write(out, png);
    } catch (Exception ignored) {
    }
  }
}
