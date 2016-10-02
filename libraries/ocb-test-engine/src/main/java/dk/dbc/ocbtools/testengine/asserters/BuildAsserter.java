package dk.dbc.ocbtools.testengine.asserters;

import dk.dbc.iscrum.records.MarcRecord;
import dk.dbc.iscrum.utils.ResourceBundles;
import dk.dbc.ocbtools.testengine.testcases.BuildTestcase;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Helper class to assert build results for equality.
 */
public class BuildAsserter {
    private static final XLogger logger = XLoggerFactory.getXLogger(BuildAsserter.class);
    private static final String BUILD_ERROR_RESOURCE_STRING = "assert.build.error";

    public static void assertValidation(BuildTestcase buildTestcase, String actual) {
        logger.entry(buildTestcase, actual);
        try {
            ResourceBundle bundle = ResourceBundles.getBundle("messages");
            String actualResult;
            String expectedResult;
            if (StringUtils.isNotEmpty(buildTestcase.getExpected().getError())) {
                actualResult = actual.trim();
                expectedResult = buildTestcase.getExpected().getError().trim();
            } else {
                String actualTrimmed = actual.trim();
                actualResult = removeFaustFromMarcRecordString(actualTrimmed);
                MarcRecord expectedRecord = buildTestcase.loadResultRecord();
                String expectedTrimmed = expectedRecord.toString().trim();
                expectedResult = removeFaustFromMarcRecordString(expectedTrimmed);
            }
            if (!expectedResult.equals(actualResult)) {
                throw new AssertionError(String.format(bundle.getString(BUILD_ERROR_RESOURCE_STRING), expectedResult, actualResult));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            logger.exit();
        }
    }

    private static String removeFaustFromMarcRecordString(String record) {
        logger.entry(record);
        String res = null;
        try {
            if (record != null) {
                int idx = record.indexOf("001 00");
                if (idx > -1) {
                    idx = record.indexOf("*a");
                    if (idx > -1) {
                        idx = record.indexOf(" ", idx);
                        res = record.substring(0, idx);
                        idx = record.indexOf(" ", idx + 1);
                        res += record.substring(idx);
                    }
                }
            }
            return res;
        } finally {
            logger.exit(res);
        }
    }
}
