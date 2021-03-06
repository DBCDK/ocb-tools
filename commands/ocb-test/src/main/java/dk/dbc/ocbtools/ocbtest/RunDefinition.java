package dk.dbc.ocbtools.ocbtest;

import dk.dbc.ocbtools.commons.api.Subcommand;
import dk.dbc.ocbtools.commons.api.SubcommandDefinition;
import dk.dbc.ocbtools.commons.api.SubcommandExecutor;
import dk.dbc.ocbtools.commons.cli.CliException;
import dk.dbc.ocbtools.testengine.reports.JUnitReport;
import dk.dbc.ocbtools.testengine.reports.TestReport;
import dk.dbc.ocbtools.testengine.reports.TextReport;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Subcommand(name = "run",
        description = "Udfører en, flere eller alle testcases",
        usage = "[options] [testcase1, testcase2, ...]")
public class RunDefinition implements SubcommandDefinition {
    private static final XLogger logger = XLoggerFactory.getXLogger(RunDefinition.class);

    @Override
    public List<Option> createOptions() throws CliException {
        List<Option> options = new ArrayList<>();
        Option option;
        option = new Option("c", "config", true, "Navn på konfiguration som skal anvendes.");
        option.setRequired(true);
        options.add(option);
        option = new Option("s", "summary", false, "Udskriver en opsummering af testen efter den er udført.");
        options.add(option);
        return options;
    }

    @Override
    public SubcommandExecutor createExecutor(File baseDir, CommandLine line) throws CliException {
        logger.entry(baseDir, line);
        try {
            List<TestReport> reports = new ArrayList<>();

            logger.debug("hasOption -s: {}", line.hasOption("s"));
            logger.debug("Args: {}", line.getArgList());

            TextReport textReport = new TextReport();
            textReport.setPrintSummary(line.hasOption("s"));
            reports.add(textReport);

            JUnitReport junitReport = new JUnitReport(new File(baseDir.getCanonicalPath() + "/target/surefire-reports"));
            reports.add(junitReport);

            RunExecutor runExecutor = new RunExecutor();
            if (line.hasOption("c")) {
                runExecutor.setConfigName(line.getOptionValue("c"));
            }
            runExecutor.setTcNames(line.getArgList());
            runExecutor.setReports(reports);
            return runExecutor;
        } catch (IOException ex) {
            logger.error("Unable to execute command 'run': " + ex.getMessage(), ex);
            return null;
        } finally {
            logger.exit();
        }
    }
}
