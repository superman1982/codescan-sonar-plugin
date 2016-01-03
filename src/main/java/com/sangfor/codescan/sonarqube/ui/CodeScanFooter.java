package com.sangfor.codescan.sonarqube.ui;

import org.sonar.api.web.Footer;

public final class CodeScanFooter implements Footer {

  @Override
  public String getHtml() {
    return "<p>深信服集团 - <em>测试中心质量管理平台 @2016</em></p>";
  }
}
