package com.enuygun.api.config;

import com.enuygun.utils.ConfigReader;

public final class ApiConfig {
  private ApiConfig() {}

  public static String petstoreBaseUrl() {
    String v = ConfigReader.getRequired("petstoreBaseUrl");
    return v.endsWith("/") ? v.substring(0, v.length() - 1) : v;
  }
}

