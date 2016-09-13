package dk.dbc.ocbtools.testengine.executors;

import dk.dbc.iscrum.records.MarcRecord;
import dk.dbc.iscrum.utils.json.Json;
import dk.dbc.ocbtools.scripter.ScripterException;
import dk.dbc.ocbtools.scripter.ServiceScripter;
import dk.dbc.ocbtools.testengine.asserters.UpdateAsserter;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcase;
import dk.dbc.updateservice.service.api.Entry;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Executor to test a testcase against the JavaScript logic.
 * <p/>
 * No external web services are used in this executor.
 */
public class ValidateRecordExecutor implements TestExecutor {
    private static final XLogger logger = XLoggerFactory.getXLogger(ValidateRecordExecutor.class);
    private static final String SCRIPT_FILENAME = "ValidateRecordExecutor.use.js";
    private static final String SCRIPT_FUNCTION = "validateRecord";
    private static final String SERVICE_NAME = "ocb-test";

    private File baseDir;
    private DemoInfoPrinter demoInfoPrinter;
    private UpdateTestcase tc;
    private ServiceScripter scripter;

    public ValidateRecordExecutor(File baseDir, UpdateTestcase tc, boolean printDemoInfo) {
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

    public void setScripter(ServiceScripter scripter) {
        this.scripter = scripter;
    }

    @Override
    public String name() {
        return "Validate record with locale JavaScript";
    }

    @Override
    public boolean setup() {
        if (this.demoInfoPrinter != null) {
            demoInfoPrinter.printHeader(this.tc, this);
            demoInfoPrinter.printSetup(this.tc);
        }
        return true;
    }

    @Override
    public void teardown() {
        if (this.demoInfoPrinter != null) {
            demoInfoPrinter.printFooter();
        }
    }

    @Override
    public void executeTests() {
        logger.entry();
        try {
            if (this.demoInfoPrinter != null) {
                demoInfoPrinter.printLocaleRequest(tc);
            }

            MarcRecord record = tc.loadRecord();
            Map<String, String> settings = createSettings();

            Object jsResult = scripter.callMethod(SCRIPT_FILENAME, SCRIPT_FUNCTION, tc, Json.encode(record), settings);
            List<Entry> valErrors = Json.decodeArray(jsResult.toString(), Entry.class);
            UpdateAsserter.assertValidation(UpdateAsserter.VALIDATION_PREFIX_KEY, tc.getExpected().getValidation(), valErrors);
        } catch (IOException | ScripterException ex) {
            throw new AssertionError(String.format("Fatal error when checking template for testcase %s", tc.getName()), ex);
        } finally {
            logger.exit();
        }
    }

    private Map<String, String> createSettings() {
        HashMap<String, String> settings = new HashMap<>();
        settings.put("javascript.basedir", baseDir.getAbsolutePath());
        settings.put("javascript.install.name", tc.getDistributionName());
        settings.put("solr.url", "");
        return settings;
    }
}
