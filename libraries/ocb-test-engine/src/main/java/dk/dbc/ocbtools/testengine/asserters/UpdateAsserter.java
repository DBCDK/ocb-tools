package dk.dbc.ocbtools.testengine.asserters;

import dk.dbc.iscrum.utils.ResourceBundles;
import dk.dbc.iscrum.utils.json.Json;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcaseExpectedResult;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcaseExpectedValidateResult;
import dk.dbc.updateservice.service.api.DoubleRecordEntries;
import dk.dbc.updateservice.service.api.DoubleRecordEntry;
import dk.dbc.updateservice.service.api.MessageEntry;
import dk.dbc.updateservice.service.api.Messages;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Helper class to assert validation results for equality.
 */
public class UpdateAsserter {
    private static final XLogger logger = XLoggerFactory.getXLogger(UpdateAsserter.class);
    public static final String VALIDATION_PREFIX_KEY = "validation";
    public static final String UPDATE_PREFIX_KEY = "update";

    public static void assertValidation(String bundleKeyPrefix, List<MessageEntry> expected, List<MessageEntry> actual) throws IOException {
        logger.entry(bundleKeyPrefix, expected, actual);
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

    public static void assertValidationDoubleRecord(String bundleKeyPrefix, List<DoubleRecordEntry> expected, List<DoubleRecordEntry> actual) throws IOException {
        logger.entry(bundleKeyPrefix, expected, actual);
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
                if (!compareDoubleRecordObjectList(expected, actual)) {
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


    private static boolean compareEntryObjectList(List<MessageEntry> lhs, List<MessageEntry> rhs) {
        if (lhs == null && rhs == null) {
            return true;
        }
        if (lhs == null ^ rhs == null) {
            return false;
        }
        if (lhs.size() != rhs.size()) {
            return false;
        }
        for (MessageEntry e : lhs) {
            if (!isEntryPresentInEntryList(e, rhs)) {
                return false;
            }
        }
        for (MessageEntry e : rhs) {
            if (!isEntryPresentInEntryList(e, lhs)) {
                return false;
            }
        }
        return true;
    }

    private static boolean compareDoubleRecordObjectList(List<DoubleRecordEntry> lhs, List<DoubleRecordEntry> rhs) {
        if (lhs == null && rhs == null) {
            return true;
        }
        if (lhs == null ^ rhs == null) {
            return false;
        }
        if (lhs.size() != rhs.size()) {
            return false;
        }
        for (DoubleRecordEntry e : lhs) {
            if (!isDoubleRecordPresentInEntryList(e, rhs)) {
                return false;
            }
        }
        for (DoubleRecordEntry e : rhs) {
            if (!isDoubleRecordPresentInEntryList(e, lhs)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isEntryPresentInEntryList(MessageEntry entry, List<MessageEntry> entryList) {
        for (MessageEntry e : entryList) {
            if (compareEntryObjects(entry, e)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDoubleRecordPresentInEntryList(DoubleRecordEntry entry, List<DoubleRecordEntry> entryList) {
        for (DoubleRecordEntry e : entryList) {
            if (compareDoubleRecordObjects(entry, e)) {
                return true;
            }
        }
        return false;
    }

    private static boolean compareEntryObjects(MessageEntry lhs, MessageEntry rhs) {
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
        if (!StringUtils.equals(lhs.getUrlForDocumentation(), rhs.getUrlForDocumentation())) {
            return false;
        }
        if (!Objects.equals(lhs.getOrdinalPositionOfField(), rhs.getOrdinalPositionOfField())) {
            return false;
        }
        if (!Objects.equals(lhs.getOrdinalPositionOfSubfield(), rhs.getOrdinalPositionOfSubfield())) {
            return false;
        }
        if (!Objects.equals(lhs.getOrdinalPositionInSubfield(), rhs.getOrdinalPositionInSubfield())) {
            return false;
        }
        if (!StringUtils.equals(lhs.getMessage(), rhs.getMessage())) {
            return false;
        }
        return true;
    }

    private static boolean compareDoubleRecordObjects(DoubleRecordEntry lhs, DoubleRecordEntry rhs) {
        if (lhs == null && rhs == null) {
            return true;
        }
        if (lhs == null ^ rhs == null) {
            return true;
        }
        if (!StringUtils.equals(lhs.getPid(), rhs.getPid())) {
            return false;
        }
        if (!StringUtils.equals(lhs.getMessage(), rhs.getMessage())) {
            return false;
        }
        return true;
    }

    public static void assertValidation(String bundleKeyPrefix, UpdateTestcaseExpectedValidateResult expected, Messages actual) throws IOException {
        logger.entry(bundleKeyPrefix, expected, actual);
        try {
            List<MessageEntry> expectedEntries = new ArrayList<>();
            if (expected != null && expected.getErrors() != null) {
                expectedEntries.addAll(expected.getErrors());
            }
            List<MessageEntry> actualEntries = new ArrayList<>();
            if (actual != null && actual.getMessageEntry() != null) {
                actualEntries.addAll(actual.getMessageEntry());
            }
            assertValidation(bundleKeyPrefix, expectedEntries, actualEntries);
        } finally {
            logger.exit();
        }
    }

    public static void assertValidation(String bundleKeyPrefix, List<MessageEntry> expected, Messages actual) throws IOException {
        logger.entry(bundleKeyPrefix, expected, actual);
        try {
            List<MessageEntry> expectedEntries = new ArrayList<>();
            if (expected != null) {
                expectedEntries.addAll(expected);
            }
            List<MessageEntry> actualEntries = new ArrayList<>();
            if (actual != null && actual.getMessageEntry() != null) {
                actualEntries.addAll(actual.getMessageEntry());
            }
            assertValidation(bundleKeyPrefix, expectedEntries, actualEntries);
        } finally {
            logger.exit();
        }
    }

    public static void assertValidation(String bundleKeyPrefix, UpdateTestcaseExpectedResult expected, Messages actual) throws IOException {
        logger.entry(bundleKeyPrefix, expected, actual);
        try {
            List<MessageEntry> expectedEntries = new ArrayList<>();
            if (expected != null && expected.getValidation() != null && expected.getValidation().getErrors() != null) {
                expectedEntries.addAll(expected.getValidation().getErrors());
            }
            List<MessageEntry> actualEntries = new ArrayList<>();
            if (actual != null && actual.getMessageEntry() != null) {
                actualEntries.addAll(actual.getMessageEntry());
            }
            assertValidation(bundleKeyPrefix, expectedEntries, actualEntries);
        } finally {
            logger.exit();
        }
    }

    public static void assertValidation(String bundleKeyPrefix, List<DoubleRecordEntry> expected, DoubleRecordEntries actual) throws IOException {
        logger.entry(bundleKeyPrefix, expected, actual);
        try {
            List<DoubleRecordEntry> expectedEntries = new ArrayList<>();
            if (expected != null) {
                expectedEntries.addAll(expected);
            }
            List<DoubleRecordEntry> actualEntries = new ArrayList<>();
            if (actual != null) {
                actualEntries.addAll(actual.getDoubleRecordEntry());
            }
            assertValidationDoubleRecord(bundleKeyPrefix, expectedEntries, actualEntries);
        } finally {
            logger.exit();
        }
    }
}
