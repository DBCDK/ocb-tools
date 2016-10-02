package dk.dbc.ocbtools.ocbtest;

import dk.dbc.ocbtools.commons.api.SubcommandExecutor;
import dk.dbc.ocbtools.commons.cli.CliException;
import dk.dbc.ocbtools.scripter.Distribution;
import dk.dbc.ocbtools.scripter.ScripterException;
import dk.dbc.ocbtools.scripter.ServiceScripter;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class JsTestsExecutor implements SubcommandExecutor {
    private static final XLogger logger = XLoggerFactory.getXLogger(JsTestsExecutor.class);

    private File baseDir;
    private List<String> modules;

    JsTestsExecutor(File baseDir, List<String> modules) {
        this.baseDir = baseDir;
        this.modules = modules;
    }

    @Override
    public void actionPerformed() throws CliException {
        logger.entry();

        try {
            logger.debug("Modules: {}", modules);

            ServiceScripter scripter = new ServiceScripter();
            scripter.setBaseDir(baseDir.getCanonicalPath());
            scripter.setModulesKey("unittest.modules.search.path");

            String distributionsDirName = baseDir.getCanonicalPath() + "/distributions";

            ArrayList<Distribution> distributions = new ArrayList<>();
            distributions.add(new Distribution("ocbtools", "ocb-tools"));
            for (String dirName : new File(distributionsDirName).list()) {
                if (!dirName.equals("common")) {
                    if (new File(distributionsDirName + "/" + dirName).isDirectory()) {
                        distributions.add(new Distribution(dirName, "distributions/" + dirName));
                    }
                }
            }
            scripter.setDistributions(distributions);
            scripter.setServiceName("ocb-test");

            List<String> modulesToTest = modules;
            if (modulesToTest.isEmpty()) {
                for (String modulePath : scripter.getModulePaths()) {
                    File dir = new File(modulePath);
                    String[] fileList = dir.list((dir1, name) -> name.endsWith(".use.js"));

                    for (String fileName : fileList) {
                        modulesToTest.add(fileName.replace(".use.js", ""));
                    }
                }
            }
            scripter.callMethod("JsTests.use.js", "actionPerformed", modulesToTest, "TEST-JsTests.xml", true);
        } catch (IOException | ScripterException ex) {
            throw new CliException(ex.getMessage(), ex);
        } finally {
            logger.exit();
        }
    }
}
