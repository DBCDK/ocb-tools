package dk.dbc.ocbtools.testengine.executors;

import dk.dbc.ocbtools.scripter.Distribution;
import dk.dbc.ocbtools.scripter.ScripterException;
import dk.dbc.ocbtools.scripter.ServiceScripter;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcase;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;

/**
 * Test executor that checks that a template exists and can be loaded.
 */
public class CheckTemplateExecutor implements TestExecutor {
    private static final XLogger logger = XLoggerFactory.getXLogger(CheckTemplateExecutor.class);
    private static final String SCRIPT_FILENAME = "validator.js";
    private static final String SCRIPT_FUNCTION = "checkTemplate";
    private static final String SERVICE_NAME = "update";

    private File baseDir;
    private UpdateTestcase tc;

    public CheckTemplateExecutor(File baseDir, UpdateTestcase tc) {
        this.baseDir = baseDir;
        this.tc = tc;
    }

    @Override
    public String name() {
        return "Check Template";
    }

    @Override
    public boolean setup() {
        return true;
    }

    @Override
    public void teardown() {
    }

    @Override
    public void executeTests() {
        logger.entry();

        try {
            ServiceScripter scripter = new ServiceScripter();
            scripter.setBaseDir(baseDir.getCanonicalPath());
            scripter.setModulesKey("unittest.modules.search.path");

            ArrayList<Distribution> distributions = new ArrayList<>();
            distributions.add(new Distribution(tc.getDistributionName(), "distributions/" + tc.getDistributionName()));
            logger.debug("Using distributions: {}", distributions);

            scripter.setDistributions(distributions);
            scripter.setServiceName(SERVICE_NAME);

            HashMap<String, String> settings = new HashMap<>();
            settings.put("javascript.basedir", baseDir.getAbsolutePath());
            settings.put("javascript.install.name", tc.getDistributionName());

            String message = String.format("The template '%s' does not exist in testcase %s", tc.getRequest().getTemplateName(), tc.getName());
            assertTrue(message, (Boolean) scripter.callMethod(SCRIPT_FILENAME, SCRIPT_FUNCTION, tc.getRequest().getTemplateName(), settings));
        } catch (IOException | ScripterException ex) {
            throw new AssertionError(String.format("Fatal error when checking template for testcase %s", tc.getName()), ex);
        } finally {
            logger.exit();
        }
    }
}
