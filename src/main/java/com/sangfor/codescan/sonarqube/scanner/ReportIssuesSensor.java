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

import com.google.common.collect.Lists;
import com.sangfor.codescan.cpptest.CppTestError;
import com.sangfor.codescan.sonarqube.CxxLanguage;
import com.sangfor.codescan.sonarqube.rules.CppTestRulesDefinition;
import com.sangfor.codescan.utils.CodeScanUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sonar.api.batch.bootstrap.ProjectReactor;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;

public class ReportIssuesSensor implements Sensor {

    public static final String RULE_KEY = "BD-RES-INVFREE";
    private Set<String> notFoundFiles = new HashSet<String>();

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor
                .name("Multiline Issues Test")
                .onlyOnLanguages(CxxLanguage.KEY)
                .createIssuesForRuleRepositories(CppTestRulesDefinition.KEY);
    }

    @Override
    public void execute(SensorContext context) {
        Project project;
        FileSystem fs = context.fileSystem();
        FilePredicates p = fs.predicates();

        ArrayList<CppTestError> list = new ArrayList();

        for (CppTestError error : list) {
            saveViolation(project, context, reactor, error);

        }

    }

    private void saveViolation(Project project, SensorContext context, ProjectReactor reactor, CppTestError error) {

        FileSystem fs = context.fileSystem();
        // handles file="" situation -- file level
        if ((error.getFile() != null) && (error.getFile().length() > 0)) {
            String root = reactor.getRoot().getBaseDir().getAbsolutePath();
            String normalPath = CodeScanUtils.normalizePathFull(error.getFile(), root);
            if (normalPath != null && !notFoundFiles.contains(normalPath)) {
                InputFile inputFile = fs.inputFile(fs.predicates().is(new File(normalPath)));
                if (inputFile != null) {
                    createIssue(context, inputFile, error);
                } else {
                    CodeScanUtils.LOG.warn("Cannot find the file '{}', skipping violations", normalPath);
                    notFoundFiles.add(normalPath);
                }
            }
        } else { // project level
            //issuable = perspectives.as(Issuable.class, (Resource) project);
        }
    }

    private static void createIssue(SensorContext context, InputFile inputFile, CppTestError error) {

        RuleKey ruleKey = RuleKey.of(error.getRuleRepoKey(), error.getRuleid());
        NewIssue newIssue = context.newIssue().forRule(ruleKey);

        TextPointer newPointer = inputFile.newPointer(8, 2);

        NewIssueLocation primaryLocation = newIssue.newLocation()
                .on(inputFile)
                .message(error.getMsg())
                .at(inputFile.newRange(newPointer, newPointer));

        newIssue.at(primaryLocation);

        List<NewIssueLocation> flowLocations = Lists.newArrayList();
        Map flows = error.getFlows();

        List<Integer> flowNums = Lists.newArrayList(flows.keySet());
        for (Integer flowNum : flowNums) {
            Map flow = (Map) flows.get(flowNum);
            TextPointer start = (TextPointer) flow.get("start");
            TextPointer end = (TextPointer) flow.get("end");
            NewIssueLocation newLocation = newIssue.newLocation()
                    .on(inputFile)
                    .at(inputFile.newRange(start, end))
                    .message((String) flow.get("info"));
            flowLocations.add(newLocation);
        }
        if (flowLocations.size() == 1) {
            newIssue.addLocation(flowLocations.get(0));
        } else {
            newIssue.addFlow(flowLocations);
        }

        newIssue.save();
    }
}
