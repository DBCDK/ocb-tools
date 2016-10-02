package dk.dbc.ocbtools.testengine.testcases;

import dk.dbc.iscrum.utils.json.Json;
import dk.dbc.ocbtools.commons.filesystem.SystemTest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Testcase factory to create Buildservice Testcase instances from json files.
 */
class BuildTestcaseFactory {

    private static final XLogger logger = XLoggerFactory.getXLogger(UpdateTestcaseRepositoryFactory.class);

    static List<BuildTestcase> newInstances(SystemTest systemTest) throws IOException {
        logger.entry(systemTest);

        List<BuildTestcase> result = null;
        try {
            result = Json.decodeArray(systemTest.getFile(), BuildTestcase.class);
            for (BuildTestcase tc : result) {
                tc.setDistributionName(systemTest.getDistributionName());
                tc.setFile(systemTest.getFile());

                if (tc.getRequest() != null && StringUtils.isNotEmpty(tc.getRequest().getRecord())) {
                    tc.getRequest().setRecordFile(new File(tc.getFile().getParent() + "/" + tc.getRequest().getRecord()));
                }
                if (tc.getExpected() != null && StringUtils.isNotEmpty(tc.getExpected().getRecord())) {
                    tc.getExpected().setRecordFile(new File(tc.getFile().getParent() + "/" + tc.getExpected().getRecord()));
                }
            }

            return result;
        } finally {
            logger.exit(result);
        }
    }
}
