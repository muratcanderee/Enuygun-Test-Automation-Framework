package com.enuygun.ui.tests;

import com.enuygun.ui.config.TestConfig;
import com.enuygun.ui.pages.FlightResultsPage;
import com.enuygun.ui.pages.HomePage;
import java.time.LocalTime;
import org.testng.annotations.Test;

public class FlightSearchTimeFilterTest extends BaseUiTest {

  @Test
  void basicFlightSearch_and_departureTimeFilter() {
    String from = TestConfig.fromCity();
    String to = TestConfig.toCity();

    FlightResultsPage results =
        new HomePage(driver)
            .navigateTo(TestConfig.baseUrl())
            .setRoute(from, to)
            .setDates(TestConfig.departDate(), TestConfig.returnDate())
            .search();

    LocalTime windowStart = TestConfig.departureTimeFilterStart();
    LocalTime windowEnd = TestConfig.departureTimeFilterEnd();

    results
        .assertRouteInUrl(from, to)
        .applyDepartureTimeFilter(windowStart, windowEnd)
        .assertAllDeparturesBetween(windowStart, windowEnd);
  }
}
