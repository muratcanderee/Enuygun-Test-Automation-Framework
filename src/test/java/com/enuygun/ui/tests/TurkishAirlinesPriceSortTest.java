package com.enuygun.ui.tests;

import com.enuygun.ui.config.TestConfig;
import com.enuygun.ui.pages.FlightResultsPage;
import com.enuygun.ui.pages.HomePage;
import java.time.LocalTime;
import org.testng.annotations.Test;

public class TurkishAirlinesPriceSortTest extends BaseUiTest {

  @Test
  void filter_turkishAirlines_and_verify_price_sorting() {
    String from = TestConfig.fromCity();
    String to = TestConfig.toCity();

    FlightResultsPage results =
        new HomePage(driver)
            .navigateTo(TestConfig.baseUrl())
            .setRoute(from, to)
            .setDates(TestConfig.departDate(), TestConfig.returnDate())
            .search();

    results
        .applyDepartureTimeFilter(LocalTime.of(10, 0), LocalTime.of(18, 0))
        .filterByAirline("Türk Hava Yolları")
        .sortByPriceAscending()
        .assertAirlineVisibleInAllCards("Türk Hava Yolları")
        .assertPricesAscending();
  }
}
