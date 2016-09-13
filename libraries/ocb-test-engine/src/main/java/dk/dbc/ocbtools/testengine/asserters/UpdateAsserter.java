package dk.dbc.ocbtools.testengine.asserters;

import dk.dbc.iscrum.utils.ResourceBundles;
import dk.dbc.iscrum.utils.json.Json;
import dk.dbc.updateservice.service.api.Entry;
import dk.dbc.updateservice.service.api.Messages;
import dk.dbc.updateservice.service.api.Param;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Helper class to assert validation results for equality.
 */
public class UpdateAsserter {
    private static final XLogger logger = XLoggerFactory.getXLogger(UpdateAsserter.class);
    public static final String VALIDATION_PREFIX_KEY = "validation";
    public static final String UPDATE_PREFIX_KEY = "update";

    public static void assertValidation(String bundleKeyPrefix, List<Entry> expected, List<Entry> actual) throws IOException {
        logger.entry(expected, actual);
        try {
            ResourceBundle bundle = ResourceBundles.getBundle("messages");
            String errorKey = "assert." + bundleKeyPrefix + ".error";
            if (expected == null && actual == null) {
            } else if (expected == null ^ actual == null) {
                throw new AssertionError(String.format(bundle.getString(errorKey), Json.encodePretty(expected), Json.encodePretty(actual)));
            } else if (!expected.equals(actual)) {
                if (expected.size() != actual.size()) {
                    throw new AssertionError(String.format(bundle.getString(errorKey), Json.encodePretty(expected), Json.encodePretty(actual)));
                }
                if (!compareEntryObjectList(expected, actual)) {
                    throw new AssertionError(String.format(bundle.getString(errorKey), Json.encodePretty(expected), Json.encodePretty(actual)));
                }
            }
        } catch (RuntimeException e) {
            logger.catching(e);
            throw e;
        } finally {
            logger.exit();
        }
    }

    private static boolean compareEntryObjectList(List<Entry> lhs, List<Entry> rhs) {
        if (lhs == null && rhs == null) {
            return true;
        }
        if (lhs == null ^ rhs == null) {
            return false;
        }
        if (lhs.size() != rhs.size()) {
            return false;
        }
        for (Entry e : lhs) {
            if (!isEntryPresentInEntryList(e, rhs)) {
                return false;
            }
        }
        for (Entry e : rhs) {
            if (!isEntryPresentInEntryList(e, lhs)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isEntryPresentInEntryList(Entry entry, List<Entry> entryList) {
        for (Entry e : entryList) {
            if (compareEntryObjects(entry, e)) {
                return true;
            }
        }
        return false;
    }

    private static boolean compareEntryObjects(Entry lhs, Entry rhs) {
        if (lhs == null && rhs == null) {
            return true;
        }
        if (lhs == null ^ rhs == null) {
            return true;
        }
        if (lhs.getType() != rhs.getType()) {
            return false;
        }
        if (!StringUtils.equals(lhs.getCode(), rhs.getCode())) {
            return false;
        }
        if (lhs.getParams() != null && rhs.getParams() != null) {
            if (lhs.getParams().getParam().size() == rhs.getParams().getParam().size()) {
                for (Param p : lhs.getParams().getParam()) {
                    if (!isParamPresentInParamList(p, rhs.getParams().getParam())) {
                        return false;
                    }
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private static boolean isParamPresentInParamList(Param param, List<Param> paramList) {
        for (Param p : paramList) {
            if (p.equals(param)) {
                return true;
            }
        }
        return false;
    }


    public static void assertValidation(String bundleKeyPrefix, List<Entry> expected, Messages actual) throws IOException {
        logger.entry(bundleKeyPrefix, expected, actual);
        try {
            List<Entry> actualEntries = new ArrayList<>();
            if (actual != null) {
                actualEntries.addAll(actual.getEntry());
            }
            assertValidation(bundleKeyPrefix, expected, actualEntries);
        } finally {
            logger.exit();
        }
    }

    public static void assertValidation(String bundleKeyPrefix, List<Entry> expected, Entry actual) throws IOException {
        logger.entry(bundleKeyPrefix, expected, actual);
        try {
            assertValidation(bundleKeyPrefix, expected, convertValidateInstanceToValidationResults(actual));
        } finally {
            logger.exit();
        }
    }

    private static List<Entry> convertValidateInstanceToValidationResults(Entry instance) {
        logger.entry(instance);
        List<Entry> result = new ArrayList<>();
        try {
            if (instance == null) {
                return result;
            }
            result.add(instance);
            return result;
        } finally {
            logger.exit(result);
        }
    }
}
