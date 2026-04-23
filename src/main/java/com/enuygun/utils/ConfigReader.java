package com.enuygun.utils;

import java.io.InputStream;
import java.util.Properties;

public final class ConfigReader {
  private ConfigReader() {}

  private static final Properties PROPS = loadProps();

  public static String getOptional(String key) {
    String sys = System.getProperty(key);
    if (sys != null && !sys.trim().isEmpty()) return sys;

    String env = System.getenv(key);
    if (env != null && !env.trim().isEmpty()) return env;

    String file = PROPS.getProperty(key);
    if (file != null && !file.trim().isEmpty()) return file;

    return null;
  }

  public static String getRequired(String key) {
    String v = getOptional(key);
    if (v == null || v.trim().isEmpty()) {
      throw new IllegalStateException(
          "Missing required config key '" + key + "' in src/main/resources/config.properties (or -D" + key + "=...)");
    }
    return v;
  }

  private static Properties loadProps() {
    Properties p = new Properties();
    try (InputStream in =
        ConfigReader.class.getClassLoader().getResourceAsStream("config.properties")) {
      if (in != null) {
        p.load(in);
      }
    } catch (Exception ignored) {
    }
    return p;
  }
}

