//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.ocbtest;

//-----------------------------------------------------------------------------

import dk.dbc.iscrum.utils.logback.filters.BusinessLoggerFilter;
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

//-----------------------------------------------------------------------------

/**
 * Created by stp on 11/03/15.
 */
@Subcommand(name = "run",
        description = "Udfører en, flere eller alle testcases",
        usage = "[options] [testcase1, testcase2, ...]")
public class RunDefinition implements SubcommandDefinition {
    private static final XLogger logger = XLoggerFactory.getXLogger(RunDefinition.class);
    private static final XLogger output = XLoggerFactory.getXLogger(BusinessLoggerFilter.LOGGER_NAME);

    @Override
    public List<Option> createOptions() throws CliException {
        List<Option> options = new ArrayList<>();

        Option option;
        option = new Option("r", "remote", false, "Udfør de enkelte testcases imod en remote installation af UpdateService");
        options.add(option);
        option = new Option("c", "config", true, "Navn på konfiguration som skal anvendes sammen med --remote");
        options.add(option);
        option = new Option("s", "summary", false, "Udskriver en opsummering af testen efter den er udført.");
        options.add(option);
        option = new Option("dm", "demo", false, "Udskriver ekstra oplysninger om råpostrepo før/efter testcasen.");
        options.add(option);
        option = new Option("a", "application", true, "Hvilken applikation der skal kaldes, U/Update for Updateservice eller B/Build for Buildservice.");
        option.setRequired(true);
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

            RunExecutor runExecutor = new RunExecutor(baseDir);
            runExecutor.setUseRemote(line.hasOption("r"));
            if (line.hasOption("c")) {
                runExecutor.setConfigName(line.getOptionValue("c"));
            }
            runExecutor.setPrintDemoInfo(line.hasOption("dm"));
            runExecutor.setTcNames(line.getArgList());
            runExecutor.setReports(reports);
            runExecutor.setApplicationType(CommonMethods.parseApplicationType(line));

            return runExecutor;
        } catch (IOException ex) {
            output.error("Unable to execute command 'run': " + ex.getMessage(), ex);
            return null;
        } finally {
            logger.exit();
        }
    }
}
