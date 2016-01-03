package com.sangfor.codescan.sonarqube;

import java.util.Arrays;
import java.util.List;
import org.sonar.api.config.PropertyDefinition;

public class CodeScanProperties {

  public static final String MY_PROPERTY_KEY = "sonar.example.myproperty";

  private CodeScanProperties() {
    // only statics
  }

  public static List<PropertyDefinition> definitions() {
    return Arrays.asList(
      PropertyDefinition.builder(MY_PROPERTY_KEY)
        .name("My Property")
        .description("Description of my property")
        .defaultValue("Hello World!")
        .build(),

      PropertyDefinition.builder(CxxLanguage.FILE_SUFFIXES_PROPERTY_KEY)
        .name("File Suffixes")
        .description("Comma-separated list of suffixes for files to analyze.")
        .defaultValue(CxxLanguage.DEFAULT_FILE_SUFFIXES)
        .build()
      );
  }
}
