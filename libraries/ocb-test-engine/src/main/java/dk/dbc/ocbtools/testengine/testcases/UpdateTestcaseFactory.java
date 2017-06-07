package dk.dbc.ocbtools.testengine.testcases;

import dk.dbc.iscrum.utils.json.Json;
import dk.dbc.ocbtools.commons.filesystem.SystemTest;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Testcase factory to create Updateservice Testcase instances from json files.
 */
public class UpdateTestcaseFactory {
    private static final XLogger logger = XLoggerFactory.getXLogger(UpdateTestcaseFactory.class);

    public static List<UpdateTestcase> newInstances(SystemTest systemTest) throws IOException {
        logger.entry(systemTest);

        List<UpdateTestcase> result = null;
        try {
            result = Json.decodeArray(systemTest.getFile(), UpdateTestcase.class);
            for (UpdateTestcase tc : result) {
                tc.setDistributionName(systemTest.getDistributionName());
                tc.setFile(systemTest.getFile());
                if (tc.getSetup() != null && tc.getSetup().getRawrepo() != null) {
                    for (UpdateTestcaseRecord updateTestcaseRecord : tc.getSetup().getRawrepo()) {
                        updateTestcaseRecord.setRecordFile(new File(tc.getFile().getParent() + "/" + updateTestcaseRecord.getRecord()));
                    }
                }
                if (tc.getExpected() != null && tc.getExpected().getUpdate() != null && tc.getExpected().getUpdate().getRawrepo() != null) {
                    for (UpdateTestcaseRecord updateTestcaseRecord : tc.getExpected().getUpdate().getRawrepo()) {
                        updateTestcaseRecord.setRecordFile(new File(tc.getFile().getParent() + "/" + updateTestcaseRecord.getRecord()));
                    }
                }
            }
            return result;
        } finally {
            logger.exit(result);
        }
    }
}
