package com.sangfor.codescan.sonarqube.scanner;

import com.google.common.collect.Lists;
import com.sangfor.codescan.cpptest.CppTestAnalysisResultsParser;
import com.sangfor.codescan.cpptest.CppTestError;
import com.sangfor.codescan.sonarqube.CxxLanguage;
import com.sangfor.codescan.utils.CodeScanUtils;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import static java.lang.String.format;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.jdom.JDOMException;
import org.sonar.api.batch.bootstrap.ProjectReactor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;

/**
 * The goal of this Sensor is to load the results of an analysis performed by a
 * fictive external tool named: FooLint Results are provided as an xml file and
 * are corresponding to the rules defined in 'rules.xml'. To be very abstract,
 * these rules are applied on source files made with the fictive language Foo.
 */
public class IssuesLoaderSensor implements Sensor {

    private static final Logger LOGGER = Loggers.get(IssuesLoaderSensor.class);

    protected static final String REPORT_PATH_KEY = "sonar.codescan.cpptest.reportpath";

    protected final Settings settings;
    protected final FileSystem fileSystem;
    protected final ProjectReactor reactor;
    private Set<String> notFoundFiles = new HashSet<String>();

    /**
     * Use of IoC to get Settings, FileSystem, RuleFinder and
     * ResourcePerspectives
     */
    public IssuesLoaderSensor(final Settings settings, final FileSystem fileSystem, ProjectReactor reactor) {
        this.settings = settings;
        this.fileSystem = fileSystem;
        this.reactor = reactor;
    }

    @Override
    public boolean shouldExecuteOnProject(final Project project) {
        return !StringUtils.isEmpty(getReportPath())
                && fileSystem.hasFiles(fileSystem.predicates().hasLanguage(CxxLanguage.KEY));
    }

    protected String reportPathKey() {
        return REPORT_PATH_KEY;
    }

    protected String getReportPath() {
        String reportPath = settings.getString(reportPathKey());
        if (!StringUtils.isEmpty(reportPath)) {
            return reportPath;
        } else {
            return null;
        }
    }

    @Override
    public void analyse(final Project project, final SensorContext context) {
        String reportPath = getReportPath();
        File analysisResultsFile = new File(reportPath);
        try {
            parseAndSaveResults(context, analysisResultsFile);

        } catch (XMLStreamException e) {
            throw new IllegalStateException("Unable to parse the provided Report file(report.xml)", e);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(IssuesLoaderSensor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JDOMException ex) {
            java.util.logging.Logger.getLogger(IssuesLoaderSensor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void parseAndSaveResults(SensorContext context, final File file) throws XMLStreamException, IOException, JDOMException {
        LOGGER.info("Parsing 'CppTest Report' Analysis Results");
        CppTestAnalysisResultsParser parser = new CppTestAnalysisResultsParser(reactor.getRoot().getBaseDir().getCanonicalPath());
        List<CppTestError> errors = parser.parse(file);
        int i = 0;
        for (CppTestError error : errors) {
            if (saveViolation(context, error)) { // if (getResourceAndSaveIssue(error)) {
                i++;
            }
        }
        LOGGER.info("Total:{}     Add Success: {}", errors.size(), i);
    }

    private boolean saveViolation(SensorContext context, CppTestError error) {
        // handles file="" situation -- file level
        if ((error.getLocFile() != null) && (error.getLocFile().length() > 0)) {
            String root = reactor.getRoot().getBaseDir().getAbsolutePath();
            String normalPath = CodeScanUtils.normalizePathFull(error.getLocFile(), root);
            if (normalPath != null && !notFoundFiles.contains(normalPath)) {
                InputFile inputFile = fileSystem.inputFile(fileSystem.predicates().is(new File(normalPath)));
                if (inputFile != null) {
                    return createIssue(context.newIssue(), inputFile, error);
                } else {
                    CodeScanUtils.LOG.warn("Cannot find the file '{}', skipping violations", normalPath);
                    notFoundFiles.add(normalPath);
                }
            }
        } else { // project level
            //issuable = perspectives.as(Issuable.class, (Resource) project);
        }
        return false;
    }

    private boolean createIssue(NewIssue newIssue, InputFile inputFile, CppTestError error) {

        newIssue.forRule(RuleKey.of(error.getRuleRepoKey(), error.getRuleid()));
        NewIssueLocation primaryLocation = newIssue.newLocation()
                .on(inputFile)
                .message(error.getMsg())
                .at(inputFile.newRange(error.getLn(),
                                        0,
                                        error.getLn(),
                                        0)
                    );
        newIssue.at(primaryLocation);

        Map flows = error.getFlows();
        List<NewIssueLocation> flowLocations = Lists.newArrayList();
        List<Integer> flowNums = Lists.newArrayList(flows.keySet());
         Collections.sort(flowNums);
        for (Integer flowNum : flowNums) {
            Map flow = (Map) flows.get(flowNum);
            int line = Integer.parseInt(flow.get("ln").toString());
            NewIssueLocation newLocation = newIssue.newLocation()
                    .on(inputFile)
                    .at(inputFile.newRange(line,0, line,0))
                    .message(flow.get("props").toString());
            flowLocations.add(newLocation);
        }
        if (flowLocations.size() == 1) {
            newIssue.addLocation(flowLocations.get(0));
        } else {
            newIssue.addFlow(flowLocations);
        }
        try {
            newIssue.save();
        } catch (org.sonar.api.utils.MessageException me) {
            LOGGER.error(format("Can't add issue on file %s at line %d.", error.getLocFile(), error.getLn()), me);
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CppTest Static Analysis Report Sensor";
    }

}
