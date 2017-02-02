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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Executes the 'run' subcommand.
 */
class RunExecutor implements SubcommandExecutor {
    private static final XLogger logger = XLoggerFactory.getXLogger(RunExecutor.class);
    private static final String SERVICE_NAME = "ocb-test";

    private File baseDir;
    private String configName;
    private boolean printDemoInfo;
    private List<String> tcNames;
    private ApplicationType applicationType;
    private List<TestReport> reports;

    RunExecutor(File baseDir) {
        this.baseDir = baseDir;
        this.configName = "servers";
        this.printDemoInfo = false;
        this.tcNames = null;
        this.reports = null;
        this.applicationType = null;
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

    void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    @Override
    public void actionPerformed() throws CliException {
        logger.entry();
        logger.info("Service tested: {}", applicationType);
        try {
            if (applicationType == ApplicationType.UPDATE) {
                actionPerformedUpdate();
            } else {
                actionPerformedBuild();
            }
        } finally {
            logger.exit();
        }
    }

    @SuppressWarnings("Duplicates")
    private void checkForNonExistantTestcases(UpdateTestcaseRepository updateTestcaseRepository) throws CliException {
        logger.entry(updateTestcaseRepository);
        try {
            for (String testName : tcNames) {
                if (!updateTestcaseRepository.findAllTestcaseNames().contains(testName)) {
                    throw new CliException("Testcase : <<<< " + testName + " >>>> does not exists");
                }
            }
        } finally {
            logger.exit();
        }
    }

    @SuppressWarnings("Duplicates")
    private void checkForNonExistantTestcases(BuildTestcaseRepository buildTestcaseRepository) throws CliException {
        logger.entry(buildTestcaseRepository);
        try {
            for (String testName : tcNames) {
                if (!buildTestcaseRepository.findAllTestcaseNames().contains(testName)) {
                    throw new CliException("Testcase : <<<< " + testName + " >>>> does not exists");
                }
            }
        } finally {
            logger.exit();
        }
    }

    private void actionPerformedUpdate() throws CliException {
        logger.entry();
        try {
            OCBFileSystem fs = new OCBFileSystem(ApplicationType.UPDATE);
            UpdateTestcaseRepository repo = UpdateTestcaseRepositoryFactory.newInstanceWithTestcases(fs);

            Properties settings = fs.loadSettings(configName);
            if (settings == null) {
                logger.error("Unable to load config '{}'", configName);
                return;
            }

            logger.info("Using updateservice url: {}", settings.getProperty("updateservice.url"));
            logger.info("Using rawrepo database: {}", settings.getProperty("rawrepo.jdbc.conn.url"));
            logger.info("Using holding items database: {}", settings.getProperty("holdings.jdbc.conn.url"));
            logger.info("");

            List<UpdateTestRunnerItem> items = new ArrayList<>();
            checkForNonExistantTestcases(repo);
            for (UpdateTestcase tc : repo.findAllTestcases()) {
                if (!matchAnyNames(tc, tcNames)) {
                    continue;
                }
                List<TestExecutor> executors = new ArrayList<>();
                if (tc.getExpected().getValidation() != null) {
                    executors.add(new RemoteValidateExecutor(tc, settings, printDemoInfo));
                }
                if (tc.getExpected().getUpdate() != null) {
                    executors.add(new RemoteUpdateExecutor(tc, settings, printDemoInfo));
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
        } catch (IOException ex) {
            throw new CliException(ex.getMessage(), ex);
        } finally {
            logger.exit();
        }
    }

    private void actionPerformedBuild() throws CliException {
        logger.entry();
        try {
            OCBFileSystem fs = new OCBFileSystem(ApplicationType.BUILD);
            BuildTestcaseRepository repo = BuildTestcaseRepositoryFactory.newInstanceWithTestcases(fs);
            Properties settings = fs.loadSettings(configName);

            if (settings == null) {
                logger.error("Unable to load config '{}'", configName);
                return;
            }

            logger.info("Using url: {}", settings.getProperty("buildservice.url"));
            logger.info("");

            List<BuildTestRunnerItem> items = new ArrayList<>();
            checkForNonExistantTestcases(repo);
            for (BuildTestcase buildTestcase : repo.findAllTestcases()) {
                if (!matchAnyNames(buildTestcase, tcNames)) {
                    continue;
                }
                List<TestExecutor> executors = new ArrayList<>();
                RemoteBuildExecutor remoteBuildExecutor = new RemoteBuildExecutor(buildTestcase, settings, printDemoInfo);
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
        } catch (IOException e) {
            throw new CliException(e.getMessage(), e);
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
