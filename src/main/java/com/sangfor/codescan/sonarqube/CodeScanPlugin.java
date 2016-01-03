package com.sangfor.codescan.sonarqube;

import com.sangfor.codescan.sonarqube.scanner.IssuesLoaderSensor;
import com.sangfor.codescan.sonarqube.scanner.IssueSensor;
import com.sangfor.codescan.sonarqube.scanner.RandomMeasureComputer;
import com.sangfor.codescan.sonarqube.rules.CodeScanProfile;
import com.sangfor.codescan.sonarqube.rules.CppTestRulesDefinition;
import com.sangfor.codescan.sonarqube.scanner.MultilineIssuesSensor;
import com.sangfor.codescan.sonarqube.ui.CodeScanFooter;
import com.sangfor.codescan.sonarqube.ui.ExampleRubyWidget;
import java.util.ArrayList;
import java.util.List;
import org.sonar.api.SonarPlugin;

import static java.util.Arrays.asList;

/**
 * This class is the entry point for all extensions. It must be referenced from pom.xml.
 */
public final class CodeScanPlugin extends SonarPlugin {

  /**
   * Returns the list of the extensions to be available at runtime.
   */
  @Override
  public List getExtensions() {
    List extensions = new ArrayList();
    extensions.addAll(CodeScanProperties.definitions());
    extensions.add(CxxLanguage.class);
    extensions.add(CodeScanMetrics.class);

    // Rules, Quality Profile
    extensions.addAll(asList(CppTestRulesDefinition.class, CodeScanProfile.class));

    // Scanner
    extensions.addAll(asList(RandomMeasureComputer.class, MultilineIssuesSensor.class, IssueSensor.class, IssuesLoaderSensor.class));

    // UI
    extensions.addAll(asList(CodeScanFooter.class, ExampleRubyWidget.class));
    return extensions;
  }
}
