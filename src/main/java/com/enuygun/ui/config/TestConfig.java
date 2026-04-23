package com.enuygun.ui.config;

import com.enuygun.utils.ConfigReader;
import java.time.LocalDate;
import java.util.Locale;

public final class TestConfig {
  private TestConfig() {}

  public static String baseUrl() {
    return ConfigReader.getRequired("baseUrl");
  }

  public static String browser() {
    return ConfigReader.getRequired("browser").toLowerCase(Locale.ROOT).trim();
  }

  public static boolean headless() {
    return Boolean.parseBoolean(ConfigReader.getRequired("headless"));
  }

  public static String fromCity() {
    return ConfigReader.getRequired("fromCity");
  }

  public static String toCity() {
    return ConfigReader.getRequired("toCity");
  }

  public static LocalDate departDate() {
    return LocalDate.parse(get("departDate", LocalDate.now().plusDays(14).toString()));
  }

  public static LocalDate returnDate() {
    return LocalDate.parse(get("returnDate", LocalDate.now().plusDays(17).toString()));
  }

  private static String get(String key, String def) {
    String v = ConfigReader.getOptional(key);
    return v == null ? def : v;
  }
}

