package dk.dbc.ocbtools.testengine.testcases;

import dk.dbc.ocbtools.commons.filesystem.OCBFileSystem;
import dk.dbc.ocbtools.commons.filesystem.SystemTest;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.IOException;

/**
 * Created by stp on 14/02/15.
 */
public class UpdateTestcaseRepositoryFactory {
    private static final XLogger logger = XLoggerFactory.getXLogger(UpdateTestcaseRepositoryFactory.class);

    public static UpdateTestcaseRepository newInstanceWithTestcases(OCBFileSystem fs) throws IOException {
        logger.entry();
        UpdateTestcaseRepository result = new UpdateTestcaseRepository();
        try {
            for (SystemTest systemTest : fs.findSystemtests()) {
                if (!systemTest.getFile().getAbsolutePath().contains(UpdateTestcase.WIREMOCK_ROOT_DIR)) {
                    result.addAll(UpdateTestcaseFactory.newInstances(systemTest));
                }
            }
            return result;
        } finally {
            logger.exit();
        }
    }
}
