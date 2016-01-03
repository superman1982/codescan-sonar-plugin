package com.sangfor.codescan.sonarqube.rules;

import com.sangfor.codescan.sonarqube.CxxLanguage;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;

public final class CppTestRulesDefinition implements RulesDefinition {

  public static final String KEY = "cpptest";
  public static final String NAME = "CppTest";

  protected String rulesDefinitionFilePath() {
    return "/cpptest.xml";
  }

  private void defineRulesForLanguage(Context context, String repositoryKey, String repositoryName, String languageKey) {
    NewRepository repository = context.createRepository(repositoryKey, languageKey).setName(repositoryName);

    InputStream rulesXml = this.getClass().getResourceAsStream(rulesDefinitionFilePath());
    if (rulesXml != null) {
      RulesDefinitionXmlLoader rulesLoader = new RulesDefinitionXmlLoader();
      rulesLoader.load(repository, rulesXml, StandardCharsets.UTF_8.name());
    }

    repository.done();
  }

  @Override
  public void define(Context context) {
    //String repositoryKey = CppTestRulesDefinition.getRepositoryKeyForLanguage(CxxLanguage.KEY);
 //   String repositoryName = CppTestRulesDefinition.getRepositoryNameForLanguage(CxxLanguage.KEY);
    defineRulesForLanguage(context, KEY, NAME, CxxLanguage.KEY);
  }

  public static String getRepositoryKeyForLanguage(String languageKey) {
    return languageKey.toLowerCase() + "-" + KEY;
  }

  public static String getRepositoryNameForLanguage(String languageKey) {
    return languageKey.toUpperCase() + " " + NAME;
  }

}
