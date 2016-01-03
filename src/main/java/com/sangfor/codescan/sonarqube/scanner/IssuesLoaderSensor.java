package com.sangfor.codescan.sonarqube.scanner;

import com.sangfor.codescan.cpptest.CppTestAnalysisResultsParser;
import com.sangfor.codescan.cpptest.CppTestError;
import com.sangfor.codescan.sonarqube.CxxLanguage;
import com.sangfor.codescan.sonarqube.rules.CppTestRulesDefinition;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import static java.lang.String.format;
import java.util.logging.Level;
import org.sonar.api.batch.bootstrap.ProjectReactor;

/**
 * The goal of this Sensor is to load the results of an analysis performed by a
 * fictive external tool named: FooLint Results are provided as an xml file and
 * are corresponding to the rules defined in 'rules.xml'. To be very abstract,
 * these rules are applied on source files made with the fictive language Foo.
 */
public class IssuesLoaderSensor implements Sensor {

    private static final Logger LOGGER = Loggers.get(IssuesLoaderSensor.class);

    protected static final String REPORT_PATH_KEY = "sonar.codescan.cpptest.reportPath";

    protected final Settings settings;
    protected final FileSystem fileSystem;
    protected final RuleFinder ruleFinder;
    protected final ResourcePerspectives perspectives;
    protected final  ProjectReactor reactor;

    /**
     * Use of IoC to get Settings, FileSystem, RuleFinder and
     * ResourcePerspectives
     */
    public IssuesLoaderSensor(final Settings settings, final FileSystem fileSystem, final RuleFinder ruleFinder, final ResourcePerspectives perspectives, ProjectReactor reactor) {
        this.settings = settings;
        this.fileSystem = fileSystem;
        this.ruleFinder = ruleFinder;
        this.perspectives = perspectives;
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
            parseAndSaveResults(analysisResultsFile);

        } catch (XMLStreamException e) {
            throw new IllegalStateException("Unable to parse the provided Report file(report.xml)", e);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(IssuesLoaderSensor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void parseAndSaveResults(final File file) throws XMLStreamException, IOException {
        LOGGER.info("Parsing 'CppTest Report' Analysis Results");
        CppTestAnalysisResultsParser parser = new CppTestAnalysisResultsParser(reactor.getRoot().getBaseDir().getCanonicalPath());
        List<CppTestError> errors = parser.parse(file);
        int i = 0;
        for (CppTestError error : errors) {
            if (getResourceAndSaveIssue(error)) {
                i++;
            }
        }
        LOGGER.info("Total:{}     Add Success: {}", errors.size(), i);
    }

    private boolean getResourceAndSaveIssue(CppTestError error) {
        LOGGER.debug(error.toString());

        InputFile inputFile = fileSystem.inputFile(
                fileSystem.predicates().and(
                        fileSystem.predicates().hasRelativePath(error.getFile()),
                        fileSystem.predicates().hasType(InputFile.Type.MAIN)));

        LOGGER.debug("inputFile null ? " + (inputFile == null));

        if (inputFile != null) {
            return saveIssue(inputFile, error.getLine(), error.getRuleid(), error.getMsg(), error.getFlow().toString());
        } else {
            LOGGER.error("Not able to find a InputFile with " + error.getFile());
            return false;
        }
    }

    private boolean saveIssue(InputFile inputFile, int line, String externalRuleKey, String message, String flow) {
        RuleKey rule = RuleKey.of(CppTestRulesDefinition.KEY, externalRuleKey);

        Issuable issuable = perspectives.as(Issuable.class, inputFile);
        boolean result = false;
        if (issuable != null) {
            LOGGER.debug("Issuable is not null: %s", issuable.toString());
            Issuable.IssueBuilder issueBuilder = issuable.newIssueBuilder()
                    .ruleKey(rule)
                    .message(message);
            if (line > 0) {
                LOGGER.debug("line is > 0");
                issueBuilder = issueBuilder.line(line);
            }
            Issue issue = issueBuilder.build();
            LOGGER.debug("issue == null? " + (issue == null));
            try {
                result = issuable.addIssue(issue);
                LOGGER.debug("after addIssue: result={}", result);
            } catch (org.sonar.api.utils.MessageException me) {
                LOGGER.error(format("Can't add issue on file %s at line %d.", inputFile.absolutePath(), line), me);
            }

        } else {
            LOGGER.debug("Can't find an Issuable corresponding to InputFile:" + inputFile.absolutePath());
        }
        return result;
    }

    @Override
    public String toString() {
        return "CppTest Static Analysis Report Sensor";
    }

}
