package com.enuygun.api.tests;

import com.enuygun.api.config.ApiConfig;
import io.restassured.RestAssured;
import org.testng.annotations.BeforeClass;

public abstract class BaseApiTest {
  @BeforeClass
  void configureRestAssured() {
    RestAssured.baseURI = ApiConfig.petstoreBaseUrl();
  }
}

