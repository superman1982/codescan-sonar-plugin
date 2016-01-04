/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.sangfor.codescan.sonarqube.rules;

import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.profiles.XMLProfileParser;
import org.sonar.api.utils.ValidationMessages;

/**
 * {@inheritDoc}
 */
public class SFCodeScanDefaultProfile extends ProfileDefinition {
  private XMLProfileParser xmlProfileParser;

  /**
   * {@inheritDoc}
   */
  public SFCodeScanDefaultProfile(XMLProfileParser xmlProfileParser) {
    this.xmlProfileParser = xmlProfileParser;
  }

  @Override
  public RulesProfile createProfile(ValidationMessages messages) {
    RulesProfile profile = xmlProfileParser.parseResource(getClass().getClassLoader(),
                                                          "BugDetective-Standard.xml", messages); 

    profile.setDefaultProfile(true);
    return profile;
  }
}
