//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.ocbtest;

//-----------------------------------------------------------------------------

import dk.dbc.iscrum.utils.logback.filters.BusinessLoggerFilter;
import dk.dbc.ocbtools.commons.api.SubcommandExecutor;
import dk.dbc.ocbtools.commons.cli.CliException;
import dk.dbc.ocbtools.commons.filesystem.OCBFileSystem;
import dk.dbc.ocbtools.commons.type.ApplicationType;
import dk.dbc.ocbtools.scripter.Distribution;
import dk.dbc.ocbtools.scripter.ServiceScripter;
import dk.dbc.ocbtools.testengine.executors.BuildRecordExecutor;
import dk.dbc.ocbtools.testengine.executors.CheckTemplateExecutor;
import dk.dbc.ocbtools.testengine.executors.RemoteBuildExecutor;
import dk.dbc.ocbtools.testengine.executors.RemoteUpdateExecutor;
import dk.dbc.ocbtools.testengine.executors.RemoteValidateExecutor;
import dk.dbc.ocbtools.testengine.executors.TestExecutor;
import dk.dbc.ocbtools.testengine.executors.ValidateRecordExecutor;
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
import dk.dbc.ocbtools.testengine.testcases.ValidationResult;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

//-----------------------------------------------------------------------------

/**
 * Executes the 'run' subcommand.
 */
public class RunExecutor implements SubcommandExecutor {
    private static final XLogger output = XLoggerFactory.getXLogger(BusinessLoggerFilter.LOGGER_NAME);
    private static final XLogger logger = XLoggerFactory.getXLogger(RunExecutor.class);
    private static final String SERVICE_NAME = "ocb-test";

    private File baseDir;

    private boolean useRemote;
    private String configName;
    private boolean printDemoInfo;
    private List<String> tcNames;
    private ApplicationType applicationType;

    private List<TestReport> reports;

    public RunExecutor(File baseDir) {
        this.baseDir = baseDir;
        this.useRemote = false;
        this.configName = "servers";
        this.printDemoInfo = false;
        this.tcNames = null;
        this.reports = null;
        this.applicationType = null;
    }

    public void setUseRemote(boolean useRemote) {
        this.useRemote = useRemote;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public void setPrintDemoInfo(boolean printDemoInfo) {
        this.printDemoInfo = printDemoInfo;
    }

    public void setTcNames(List<String> tcNames) {
        this.tcNames = tcNames;
    }

    public void setReports(List<TestReport> reports) {
        this.reports = reports;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    @Override
    public void actionPerformed() throws CliException {
        output.entry();
        output.info("Service tested: {}", applicationType);
        try {
            if (applicationType == ApplicationType.UPDATE) {
                actionPerformedUpdate();
            } else {
                actionPerformedBuild();
            }
        } finally {
            output.exit();
        }
    }

    private void actionPerformedUpdate() throws CliException {
        output.entry();

        try {
            OCBFileSystem fs = new OCBFileSystem(ApplicationType.UPDATE);
            UpdateTestcaseRepository repo = UpdateTestcaseRepositoryFactory.newInstanceWithTestcases(fs);

            Properties settings = fs.loadSettings(this.configName);
            if (settings == null) {
                output.error("Unable to load config '{}'", this.configName);
                return;
            }

            if (this.useRemote) {
                output.info("Using dataio url: {}", settings.getProperty("updateservice.dataio.url"));
                output.info("Using fbs url: {}", settings.getProperty("updateservice.fbs.url"));
                output.info("Using rawrepo database: {}", settings.getProperty("rawrepo.jdbc.conn.url"));
                output.info("Using holding items database: {}", settings.getProperty("holdings.jdbc.conn.url"));
                output.info("");
            }

            Map<String, ServiceScripter> scripterCache = new HashMap<>();
            List<UpdateTestRunnerItem> items = new ArrayList<>();
            for (UpdateTestcase tc : repo.findAllTestcases()) {
                if (!matchAnyNames(tc, tcNames)) {
                    continue;
                }

                List<TestExecutor> executors = new ArrayList<>();

                if (!this.useRemote) {
                    if (tc.getExpected().getValidation() != null) {
                        ValidateRecordExecutor validateRecordExecutor = new ValidateRecordExecutor( baseDir, tc, this.printDemoInfo );

                        ServiceScripter scripter = getOrCreateScripter( scripterCache, tc.getDistributionName() );
                        validateRecordExecutor.setScripter( scripter );

                        executors.add( validateRecordExecutor );
                    } else {
                        output.warn( "Using CheckTemplateExecutor: {}", tc.getName() );
                        executors.add(new CheckTemplateExecutor(baseDir, tc));
                    }
                } else {
                    List<ValidationResult> validation = tc.getExpected().getValidation();
                    if (validation != null) {
                        executors.add(new RemoteValidateExecutor(tc, settings, this.printDemoInfo));
                    }

                    if (tc.getExpected().getUpdate() != null) {
                        executors.add(new RemoteUpdateExecutor(tc, settings, this.printDemoInfo));
                    } else if (validation != null && !validation.isEmpty()) {
                        executors.add(new RemoteUpdateExecutor(tc, settings, this.printDemoInfo));
                    }
                }

                items.add(new UpdateTestRunnerItem(tc, executors));
            }

            UpdateTestRunner runner = new UpdateTestRunner(items);
            TestResult testResult = runner.run();

            for (TestReport report : reports) {
                report.produce(testResult);
            }

            if (testResult.hasError()) {
                output.error("");
                throw new CliException("Errors where found in system-tests.");
            }

        } catch (IOException ex) {
            throw new CliException(ex.getMessage(), ex);
        } finally {
            output.exit();
        }
    }

    private void actionPerformedBuild() throws CliException {
        output.entry();
        try {
            OCBFileSystem fs = new OCBFileSystem(ApplicationType.BUILD);
            BuildTestcaseRepository repo = BuildTestcaseRepositoryFactory.newInstanceWithTestcases(fs);
            Properties settings = fs.loadSettings(this.configName);

            if (settings == null) {
                output.error("Unable to load config '{}'", this.configName);
                return;
            }

            if (this.useRemote) {
                output.info("Using dataio url: {}", settings.getProperty("buildservice.dataio.url"));
                output.info("Using fbs url: {}", settings.getProperty("buildservice.fbs.url"));
                output.info("");
            }

            Map<String, ServiceScripter> scripterCache = new HashMap<>();
            List<BuildTestRunnerItem> items = new ArrayList<>();
            for (BuildTestcase buildTestcase : repo.findAllTestcases()) {
                if (!matchAnyNames(buildTestcase, tcNames)) {
                    continue;
                }
                List<TestExecutor> executors = new ArrayList<>();
                if (this.useRemote) {
                    RemoteBuildExecutor remoteBuildExecutor = new RemoteBuildExecutor(buildTestcase, settings, this.printDemoInfo);
                    executors.add(remoteBuildExecutor);
                } else {
                    BuildRecordExecutor buildRecordExecutor = new BuildRecordExecutor(baseDir, buildTestcase, this.printDemoInfo);

                    ServiceScripter scripter = getOrCreateScripter( scripterCache, buildTestcase.getDistributionName() );
                    buildRecordExecutor.setScripter( scripter );

                    executors.add( buildRecordExecutor );
                }

                items.add(new BuildTestRunnerItem(buildTestcase, executors));
            }

            BuildTestRunner runner = new BuildTestRunner(items);
            TestResult testResult = runner.run();

            for (TestReport report : reports) {
                report.produce(testResult);
            }

            if (testResult.hasError()) {
                output.error("");
                throw new CliException("Errors where found in system-tests.");
            }

        } catch (IOException e) {
            throw new CliException(e.getMessage(), e);
        } finally {
            output.exit();
        }
    }

    private boolean matchAnyNames(UpdateTestcase tc, List<String> names) {
        output.entry(tc, names);

        try {
            return names.isEmpty() || names.contains( tc.getName() );

        } finally {
            output.exit();
        }
    }

    private boolean matchAnyNames(BuildTestcase tc, List<String> names) {
        output.entry(tc, names);

        try {
            return names.isEmpty() || names.contains( tc.getName() );

        } finally {
            output.exit();
        }
    }

    /**
     * Gets a ServiceScripter from a cache (Map) of ServiceScripter's.
     * <p>
     *     The cache is indexed by distribution names.
     * </p>
     * <p>
     *     If a ServiceScripter does not exist in the cache, a new one is created and added to the cache.
     * </p>
     *
     * @param cache            The cache of ServiceScripter's. It may be changed by this method.
     * @param distributionName The distribution name to lookup a ServiceScripter in the cache.
     *
     * @return A ServiceScripter from the cache if it exists, otherwise a new ServiceScripter.
     *
     * @throws IOException Any exception of creation a new ServiceScripter.
     */
    private ServiceScripter getOrCreateScripter( Map<String, ServiceScripter> cache, String distributionName ) throws IOException {
        logger.entry();

        ServiceScripter scripter = null;
        try {
            if( !cache.containsKey( distributionName ) ) {
                scripter = createScripter( distributionName );
                cache.put( distributionName, scripter );
            }
            else {
                scripter = cache.get( distributionName );
            }

            return scripter;
        }
        finally {
            logger.exit( scripter );
        }
    }

    private ServiceScripter createScripter( String distributionName ) throws IOException {
        logger.entry();

        ServiceScripter scripter = null;
        try {
            scripter = new ServiceScripter();
            scripter.setBaseDir( baseDir.getCanonicalPath() );
            scripter.setModulesKey( "unittest.modules.search.path" );

            ArrayList<Distribution> distributions = new ArrayList<>();
            distributions.add( new Distribution( "ocbtools", "ocb-tools" ) );
            distributions.add( new Distribution( distributionName, "distributions/" + distributionName ) );
            logger.debug( "Using distributions: {}", distributions );

            scripter.setDistributions( distributions );
            scripter.setServiceName( SERVICE_NAME );

            return scripter;
        }
        finally {
            logger.exit( scripter );
        }
    }
}
