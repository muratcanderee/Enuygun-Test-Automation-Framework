package com.enuygun.ui.tests;

import com.enuygun.ui.config.TestConfig;
import com.enuygun.ui.pages.HomePage;
import org.testng.annotations.Test;

public class CriticalPathSmokeTest extends BaseUiTest {

  @Test
  void criticalPath_search_reaches_results() {
    new HomePage(driver)
        .navigateTo(TestConfig.baseUrl())
        .setRoute(TestConfig.fromCity(), TestConfig.toCity())
        .setDates(TestConfig.departDate(), TestConfig.returnDate())
        .search();
  }
}
