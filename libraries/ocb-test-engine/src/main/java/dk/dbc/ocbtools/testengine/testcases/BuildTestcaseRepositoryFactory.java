package dk.dbc.ocbtools.testengine.testcases;

import dk.dbc.ocbtools.commons.filesystem.OCBFileSystem;
import dk.dbc.ocbtools.commons.filesystem.SystemTest;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.IOException;

public class BuildTestcaseRepositoryFactory {

    private static final XLogger logger = XLoggerFactory.getXLogger(BuildTestcaseRepositoryFactory.class);

    public static BuildTestcaseRepository newInstanceWithTestcases(OCBFileSystem fs) throws IOException {
        logger.entry();

        BuildTestcaseRepository result = new BuildTestcaseRepository();
        try {
            for (SystemTest systemTest : fs.findSystemtests()) {
                result.addAll(BuildTestcaseFactory.newInstances(systemTest));
            }
            return result;
        } finally {
            logger.exit();
        }
    }
}
