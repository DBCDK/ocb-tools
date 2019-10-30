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
import dk.dbc.ocbtools.testengine.runners.*;
import dk.dbc.ocbtools.testengine.testcases.*;
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
    private boolean printDemoInfo;
    private List<String> tcNames;
    private List<TestReport> reports;
    private BuildTestcaseRepository buildRepo;
    private Properties buildSettings;
    private UpdateTestcaseRepository updateRepo;
    private Properties updateSettings;

    RunExecutor() {
        this.configName = "servers";
        this.printDemoInfo = false;
        this.tcNames = null;
        this.reports = null;
    }

    void setConfigName(String configName) {
        this.configName = configName;
    }

    void setPrintDemoInfo(boolean printDemoInfo) {
        this.printDemoInfo = printDemoInfo;
    }

    void setTcNames(List<String> tcNames) {
        this.tcNames = tcNames;
    }

    void setReports(List<TestReport> reports) {
        this.reports = reports;
    }

    public void preCheck() throws CliException {
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
        //logger.info("Service tested: {}", applicationType);
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
            logger.info("Using updateservice url: {}", updateSettings.getProperty("updateservice.url"));
            logger.info("Using rawrepo database: {}", updateSettings.getProperty("rawrepo.jdbc.conn.url"));
            logger.info("Using holding items database: {}", updateSettings.getProperty("holdings.jdbc.conn.url"));
            logger.info("");

            List<UpdateTestRunnerItem> items = new ArrayList<>();
            for (UpdateTestcase tc : updateRepo.findAllTestcases()) {
                if (!matchAnyNames(tc, tcNames)) {
                    continue;
                }
                List<TestExecutor> executors = new ArrayList<>();
                if (tc.getExpected().getValidation() != null) {
                    executors.add(new RemoteValidateExecutor(tc, updateSettings, printDemoInfo));
                }
                if (tc.getExpected().getUpdate() != null) {
                    executors.add(new RemoteUpdateExecutor(tc, updateSettings, printDemoInfo));
                }
                items.add(new UpdateTestRunnerItem(tc, executors));
            }

            UpdateTestRunner runner = new UpdateTestRunner(items);
            TestResult testResult = runner.run();

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
            logger.info("Using url: {}", buildSettings.getProperty("buildservice.url"));
            logger.info("");

            List<BuildTestRunnerItem> items = new ArrayList<>();
            for (BuildTestcase buildTestcase : buildRepo.findAllTestcases()) {
                if (!matchAnyNames(buildTestcase, tcNames)) {
                    continue;
                }
                List<TestExecutor> executors = new ArrayList<>();
                RemoteBuildExecutor remoteBuildExecutor = new RemoteBuildExecutor(buildTestcase, buildSettings, printDemoInfo);
                executors.add(remoteBuildExecutor);
                items.add(new BuildTestRunnerItem(buildTestcase, executors));
            }

            BuildTestRunner runner = new BuildTestRunner(items);
            TestResult testResult = runner.run();

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
