/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.sangfor.codescan.sonarqube.scanner;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.sangfor.codescan.sonarqube.CxxLanguage;
import com.sangfor.codescan.sonarqube.rules.CppTestRulesDefinition;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;

public class MultilineIssuesSensor implements Sensor {

  public static final String RULE_KEY = "BD-RES-INVFREE";

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name("Multiline Issues Test")
      .onlyOnLanguages(CxxLanguage.KEY)
      .createIssuesForRuleRepositories(CppTestRulesDefinition.KEY);
  }

  @Override
  public void execute(SensorContext context) {
    FileSystem fs = context.fileSystem();
    FilePredicates p = fs.predicates();
    for (InputFile file : fs.inputFiles(p.and(p.hasLanguages(CxxLanguage.KEY), p.hasType(Type.MAIN)))) {
      createIssues(file, context);
    }
  }

  private static void createIssues(InputFile file, SensorContext context) {
    Map<Integer, TextPointer> startIssuesPositions = Maps.newHashMap();
    Map<Integer, TextPointer> endIssuesPositions = Maps.newHashMap();
    Map<Integer, Table<Integer, Integer, TextPointer>> startFlowsPositions = Maps.newHashMap();
    Map<Integer, Table<Integer, Integer, TextPointer>> endFlowsPositions = Maps.newHashMap();

    parseIssues(file, context, startIssuesPositions, endIssuesPositions);
    parseFlows(file, context, startFlowsPositions, endFlowsPositions);
    createIssues(file, context, startIssuesPositions, endIssuesPositions, startFlowsPositions, endFlowsPositions);
  }

  private static void parseFlows(InputFile file, SensorContext context, Map<Integer, Table<Integer, Integer, TextPointer>> startFlowsPositions,
    Map<Integer, Table<Integer, Integer, TextPointer>> endFlowsPositions) {
      
         Integer issueId = Integer.parseInt("1");
          Integer issueFlowId = Integer.parseInt("1");

          if (!startFlowsPositions.containsKey(issueId)) {
            startFlowsPositions.put(issueId, HashBasedTable.<Integer, Integer, TextPointer>create());
          }
          startFlowsPositions.get(issueId).row(issueFlowId).put(1, file.newPointer(2, 1));  
          startFlowsPositions.get(issueId).row(issueFlowId).put(2, file.newPointer(3, 2));  
          startFlowsPositions.get(issueId).row(issueFlowId).put(3, file.newPointer(4, 3));  
          startFlowsPositions.get(issueId).row(issueFlowId).put(4, file.newPointer(5, 4));  
          startFlowsPositions.get(issueId).row(issueFlowId).put(5, file.newPointer(6, 5));  
          
          if (!endFlowsPositions.containsKey(issueId)) {
            endFlowsPositions.put(issueId, HashBasedTable.<Integer, Integer, TextPointer>create());
          }
          endFlowsPositions.get(issueId).row(issueFlowId).put(1, file.newPointer(2, 6));
          endFlowsPositions.get(issueId).row(issueFlowId).put(2, file.newPointer(3, 7));
          endFlowsPositions.get(issueId).row(issueFlowId).put(3, file.newPointer(4, 8));
          endFlowsPositions.get(issueId).row(issueFlowId).put(4, file.newPointer(5, 9));
          endFlowsPositions.get(issueId).row(issueFlowId).put(5, file.newPointer(6, 10));       
  }

  private static void createIssues(InputFile file, SensorContext context, Map<Integer, TextPointer> startPositions,
    Map<Integer, TextPointer> endPositions, Map<Integer, Table<Integer, Integer, TextPointer>> startFlowsPositions,
    Map<Integer, Table<Integer, Integer, TextPointer>> endFlowsPositions) {  
    RuleKey ruleKey = RuleKey.of(CppTestRulesDefinition.KEY, RULE_KEY);

    for (Map.Entry<Integer, TextPointer> entry : startPositions.entrySet()) {
      NewIssue newIssue = context.newIssue().forRule(ruleKey);
      Integer issueId = entry.getKey();
      NewIssueLocation primaryLocation = newIssue.newLocation()
        .on(file)
        .at(file.newRange(entry.getValue(), endPositions.get(issueId)));
      newIssue.at(primaryLocation.message("Primary location"));
      if (startFlowsPositions.containsKey(issueId)) {
        Table<Integer, Integer, TextPointer> flows = startFlowsPositions.get(issueId);
        for (Map.Entry<Integer, Map<Integer, TextPointer>> flowEntry : flows.rowMap().entrySet()) {
          Integer flowId = flowEntry.getKey();
          List<NewIssueLocation> flowLocations = Lists.newArrayList();
          List<Integer> flowNums = Lists.newArrayList(flowEntry.getValue().keySet());
          Collections.sort(flowNums);
          for (Integer flowNum : flowNums) {
            TextPointer start = flowEntry.getValue().get(flowNum);
            TextPointer end = endFlowsPositions.get(issueId).row(flowId).get(flowNum);
            NewIssueLocation newLocation = newIssue.newLocation()
              .on(file)
              .at(file.newRange(start, end))
              .message("Flow step #" + flowNum);
            flowLocations.add(newLocation);
          }
          if (flowLocations.size() == 1) {
            newIssue.addLocation(flowLocations.get(0));
          } else {
            newIssue.addFlow(flowLocations);
          }
        }
      }
      newIssue.save();
    }
  }

  private static void parseIssues(InputFile file, SensorContext context, Map<Integer, TextPointer> startPositions,
    Map<Integer, TextPointer> endPositions) {
      
      TextPointer newPointer = file.newPointer(8, 2);
      startPositions.put(1, newPointer);
   
      TextPointer newPointer1 = file.newPointer(8, 15);
      endPositions.put(1, newPointer1);
  }

}
