/**
 * Copyright (C) 2013-2015 all@code-story.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package net.codestory.simplelenium.driver;

public enum Configuration {
  // Active browser name
  BROWSER("browser", "PHANTOM_JS", false),

  // ChromeDriver specific configuration
  CHROMEDRIVER_URL("chromedriver.url", null, false),
  CHROMEDRIVER_EXE("chromedriver.exe", null, false),
  CHROMEDRIVER_PORT("chromedriver.port", "0", false),

  // PhantomJs specific configuration
  PHANTOMJS_URL("phantomjs.url", null, false),
  PHANTOMJS_EXE("phantomjs.exe", null, false),

  // Firefox specific configuration
  GECKODRIVER_URL("geckodriver.url", null, false),
  GECKODRIVER_EXE("geckodriver.exe", null, false),
  FIREFOXDRIVER_PORT("geckodriver.port", "0", false),

  // Standard system properties
  USER_HOME("user.home", null, true),
  OS_NAME("os.name", null, true);

  private final String key;
  private final String defaultValue;
  private final boolean required;

  Configuration(String key, String defaultValue, boolean required) {
    this.key = key;
    this.defaultValue = defaultValue;
    this.required = required;
  }

  public int getInt() {
    return Integer.parseInt(get());
  }

  public String get() {
    String value = System.getProperty(key);

    if ((value == null) || value.trim().isEmpty()) {
      if (required) {
        throw new IllegalArgumentException("System property [" + key + "] cannot be null nor empty");
      }
      return defaultValue;
    }

    return value;
  }

  public static boolean isWindows() {
    return Configuration.OS_NAME.get().startsWith("Windows");
  }

  public static boolean isMac() {
    return Configuration.OS_NAME.get().startsWith("Mac OS X");
  }

  public static boolean isLinux32() {
    return Configuration.OS_NAME.get().contains("x86");
  }
}
