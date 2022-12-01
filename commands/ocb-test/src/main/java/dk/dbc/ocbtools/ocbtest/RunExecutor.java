package dk.dbc.ocbtools.ocbtest;

import dk.dbc.ocbtools.commons.api.SubcommandExecutor;
import dk.dbc.ocbtools.commons.cli.CliException;
import dk.dbc.ocbtools.commons.filesystem.OCBFileSystem;
import dk.dbc.ocbtools.commons.type.ApplicationType;
import dk.dbc.ocbtools.testengine.executors.RemoteBuildExecutor;
import dk.dbc.ocbtools.testengine.executors.RemoteUpdateExecutor;
import dk.dbc.ocbtools.testengine.executors.RemoteValidateExecutor;
import dk.dbc.ocbtools.testengine.executors.TestExecutor;
import dk.dbc.ocbtools.testengine.reports.TestReport;
import dk.dbc.ocbtools.testengine.runners.BuildTestRunner;
import dk.dbc.ocbtools.testengine.runners.BuildTestRunnerItem;
import dk.dbc.ocbtools.testengine.runners.TestResult;
import dk.dbc.ocbtools.testengine.runners.UpdateTestRunner;
import dk.dbc.ocbtools.testengine.runners.UpdateTestRunnerItem;
import dk.dbc.ocbtools.testengine.testcases.BuildTestcase;
import dk.dbc.ocbtools.testengine.testcases.BuildTestcaseRepository;
import dk.dbc.ocbtools.testengine.testcases.BuildTestcaseRepositoryFactory;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcase;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcaseRepository;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcaseRepositoryFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Executes the 'run' subcommand.
 */
class RunExecutor implements SubcommandExecutor {
    private static final XLogger logger = XLoggerFactory.getXLogger(RunExecutor.class);

    private String configName;
    private List<String> tcNames;
    private List<TestReport> reports;
    private BuildTestcaseRepository buildRepo;
    private Properties buildSettings;
    private UpdateTestcaseRepository updateRepo;
    private Properties updateSettings;

    RunExecutor() {
        this.configName = ""; // If someone someday makes a testcase for RunExecutor, then it might be reasonable to set it to "servers" (make an intellij search for it)
        this.tcNames = null;
        this.reports = null;
    }

    void setConfigName(String configName) {
        this.configName = configName;
    }

    void setTcNames(List<String> tcNames) {
        this.tcNames = tcNames;
    }

    void setReports(List<TestReport> reports) {
        this.reports = reports;
    }

    /*
    This voluminous function prevents quibbling about missing testcases when the program are called with a specific
    case. It also fails heavily if no testcase is found.
     */
    private void preCheck() throws CliException {
        try {
            OCBFileSystem fs = new OCBFileSystem(ApplicationType.BUILD);
            buildRepo = BuildTestcaseRepositoryFactory.newInstanceWithTestcases(fs);
            buildSettings = fs.loadSettings(configName);

            if (buildSettings == null) {
                logger.error("Unable to load config '{}'", configName);
                return;
            }

            fs = new OCBFileSystem(ApplicationType.UPDATE);
            updateRepo = UpdateTestcaseRepositoryFactory.newInstanceWithTestcases(fs);
            updateSettings = fs.loadSettings(configName);

            if (updateSettings == null) {
                logger.error("Unable to load config '{}'", configName);
                return;
            }

            List<String> misses = new ArrayList<>();
            for (String testName : tcNames) {
                if (!buildRepo.findAllTestcaseNames().contains(testName)) {
                    misses.add(testName);
                }
            }
            for (String testName : tcNames) {
                if (updateRepo.findAllTestcaseNames().contains(testName)) {
                    misses.remove(testName);
                }
            }

            if (misses.size() > 0) throw new CliException("Testcase(s):\n" + misses.toString() + "\ndoes not exist");

        } catch (IOException ex) {
            throw new CliException(ex.getMessage(), ex);
        }
    }

    @Override
    public void actionPerformed() throws CliException {
        logger.entry();
        try {
            preCheck();
            actionPerformedUpdate();
            actionPerformedBuild();
        } finally {
            logger.exit();
        }
    }

    private void actionPerformedUpdate() throws CliException {
        logger.entry();
        try {
            logger.info("--- Running UPDATE tests ---");
            logger.info("");
            logger.info("Using updateservice url: {}", updateSettings.getProperty("updateservice.url"));
            logger.info("Using rawrepo database: {}", updateSettings.getProperty("rawrepo.jdbc.conn.url"));
            logger.info("Using holdings items service: {}", updateSettings.getProperty("holdings.url"));
            logger.info("");

            final List<UpdateTestRunnerItem> items = new ArrayList<>();
            for (UpdateTestcase tc : updateRepo.findAllTestcases()) {
                if (!matchAnyNames(tc, tcNames)) {
                    continue;
                }
                final List<TestExecutor> executors = new ArrayList<>();
                if (tc.getExpected().getValidation() != null) {
                    executors.add(new RemoteValidateExecutor(tc, updateSettings));
                }
                if (tc.getExpected().getUpdate() != null) {
                    executors.add(new RemoteUpdateExecutor(tc, updateSettings));
                }
                items.add(new UpdateTestRunnerItem(tc, executors));
            }

            final UpdateTestRunner runner = new UpdateTestRunner(items);
            final TestResult testResult = runner.run();

            for (TestReport report : reports) {
                report.produce(testResult);
            }

            if (testResult.hasError()) {
                logger.error("");
                throw new CliException("Errors where found in system-tests.");
            }
        } finally {
            logger.exit();
        }
    }

    private void actionPerformedBuild() throws CliException {
        logger.entry();
        try {
            logger.info("--- Running BUILD tests ---");
            logger.info("");
            logger.info("Using buildservice url: {}", buildSettings.getProperty("buildservice.url"));
            logger.info("");

            final List<BuildTestRunnerItem> items = new ArrayList<>();
            for (BuildTestcase buildTestcase : buildRepo.findAllTestcases()) {
                if (!matchAnyNames(buildTestcase, tcNames)) {
                    continue;
                }
                final List<TestExecutor> executors = new ArrayList<>();
                final RemoteBuildExecutor remoteBuildExecutor = new RemoteBuildExecutor(buildTestcase, buildSettings);
                executors.add(remoteBuildExecutor);
                items.add(new BuildTestRunnerItem(buildTestcase, executors));
            }

            final BuildTestRunner runner = new BuildTestRunner(items);
            final TestResult testResult = runner.run();

            for (TestReport report : reports) {
                report.produce(testResult);
            }
            if (testResult.hasError()) {
                logger.error("");
                throw new CliException("Errors where found in system-tests.");
            }
        } finally {
            logger.exit();
        }
    }

    private boolean matchAnyNames(UpdateTestcase tc, List<String> names) {
        logger.entry(tc, names);
        try {
            return names.isEmpty() || names.contains(tc.getName());
        } finally {
            logger.exit();
        }
    }

    private boolean matchAnyNames(BuildTestcase tc, List<String> names) {
        logger.entry(tc, names);
        try {
            return names.isEmpty() || names.contains(tc.getName());
        } finally {
            logger.exit();
        }
    }
}
