package dk.dbc.ocbtools.testengine.executors;

import dk.dbc.iscrum.records.MarcRecord;
import dk.dbc.iscrum.utils.json.Json;
import dk.dbc.ocbtools.scripter.Distribution;
import dk.dbc.ocbtools.scripter.ScripterException;
import dk.dbc.ocbtools.scripter.ServiceScripter;
import dk.dbc.ocbtools.testengine.asserters.BuildAsserter;
import dk.dbc.ocbtools.testengine.testcases.BuildTestcase;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Executor to test a testcase against the JavaScript logic.
 * <p/>
 * No external web services are used in this executor.
 */
public class BuildRecordExecutor implements TestExecutor {
    private static final XLogger logger = XLoggerFactory.getXLogger(BuildRecordExecutor.class);
    private static final String SCRIPT_FILENAME = "BuildRecordExecutor.use.js";
    private static final String SCRIPT_FUNCTION = "buildRecord";
    private static final String SERVICE_NAME = "ocb-test";
    private static final String FAUST_NBR = "100007134";

    private File baseDir;
    private DemoInfoPrinter demoInfoPrinter;
    private BuildTestcase tc;
    private ServiceScripter scripter;

    public BuildRecordExecutor(File baseDir, BuildTestcase tc, boolean printDemoInfo) {
        this.baseDir = baseDir;
        this.tc = tc;
        this.demoInfoPrinter = null;
        this.scripter = null;

        if (printDemoInfo) {
            this.demoInfoPrinter = new DemoInfoPrinter();
        }
    }

    public ServiceScripter getScripter() {
        return scripter;
    }

    public void setScripter( ServiceScripter scripter ) {
        this.scripter = scripter;
    }

    @Override
    public String name() {
        return "Build record with local JavaScript";
    }

    @Override
    public void setup() {
        logger.entry();
        if (this.demoInfoPrinter != null) {
            demoInfoPrinter.printHeader(this.tc, this);
        }
        logger.exit();
    }

    @Override
    public void teardown() {
        logger.entry();
        if (this.demoInfoPrinter != null) {
            demoInfoPrinter.printFooter();
        }
        logger.exit();
    }

    @Override
    public void executeTests() {
        logger.entry();

        try {
            if (this.demoInfoPrinter != null) {
                demoInfoPrinter.printLocaleRequest(tc);
            }

            MarcRecord record = tc.loadRequestRecord();
            Map<String, String> settings = createSettings();
            String encodedRecord = null;
            if (record != null) {
                encodedRecord = Json.encode(record);
            }
            Object jsResult = scripter.callMethod(SCRIPT_FILENAME, SCRIPT_FUNCTION, tc, encodedRecord, FAUST_NBR, settings);
            String jsResultAsString = (String) jsResult;
            String jsResultAsStringTrimmed = jsResultAsString.trim();
            if (jsResultAsStringTrimmed.startsWith("{")) {
                MarcRecord jsResultAsMarcRecord = Json.decode(jsResultAsStringTrimmed, MarcRecord.class);
                jsResultAsStringTrimmed = jsResultAsMarcRecord.toString().trim();
            }
            BuildAsserter.assertValidation(tc, jsResultAsStringTrimmed);
        } catch (IOException | ScripterException e) {
            throw new AssertionError("Fatal error when checking template for testcase " + tc.getName(), e);
        } finally {
            logger.exit();
        }
    }

    private Map<String, String> createSettings() {
        logger.entry();
        HashMap<String, String> settings = null;
        try {
            settings = new HashMap<>();
            settings.put("javascript.basedir", baseDir.getAbsolutePath());
            settings.put("javascript.install.name", tc.getDistributionName());
            return settings;
        } finally {
            logger.exit(settings);
        }
    }

    private ServiceScripter createScripter() throws IOException {
        logger.entry();
        ServiceScripter scripter = null;
        try {
            scripter = new ServiceScripter();
            scripter.setBaseDir(baseDir.getCanonicalPath());
            scripter.setModulesKey("unittest.modules.search.path");

            ArrayList<Distribution> distributions = new ArrayList<>();
            distributions.add(new Distribution("ocbtools", "ocb-tools"));
            distributions.add(new Distribution(tc.getDistributionName(), "distributions/" + tc.getDistributionName()));
            logger.debug("Using distributions: {}", distributions);

            scripter.setDistributions(distributions);
            scripter.setServiceName(SERVICE_NAME);

            return scripter;
        } finally {
            logger.exit(scripter);
        }
    }

    @Override
    public String toString() {
        return "BuildRecordExecutor{" +
                "baseDir=" + baseDir +
                ", demoInfoPrinter=" + demoInfoPrinter +
                ", tc=" + tc +
                ", scripter=" + scripter +
                '}';
    }
}
