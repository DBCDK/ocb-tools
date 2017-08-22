package dk.dbc.ocbtools.testengine.asserters;

import dk.dbc.common.records.MarcConverter;
import dk.dbc.common.records.MarcField;
import dk.dbc.common.records.MarcRecord;
import dk.dbc.common.records.MarcSubField;
import dk.dbc.ocbtools.commons.filesystem.OCBFileSystem;
import dk.dbc.ocbtools.commons.type.ApplicationType;
import dk.dbc.ocbtools.testengine.executors.QueuedJob;
import dk.dbc.ocbtools.testengine.executors.RawRepo;
import dk.dbc.ocbtools.testengine.executors.RawRepoRelationType;
import dk.dbc.ocbtools.testengine.testcases.TestcaseMimeType;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcaseRecord;
import dk.dbc.rawrepo.Record;
import dk.dbc.rawrepo.RecordId;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Asserter class to check records against rawrepo.
 */
public class RawRepoAsserter {
    private static final XLogger logger = XLoggerFactory.getXLogger(RawRepoAsserter.class);

    private static final String FORMATTED_RECORD = "{%s - %s:%s}";
    private static final String FORMATTED_RECORD_ID = "{%s:%s}";

    public static void assertRecordListEquals(List<UpdateTestcaseRecord> expected, List<Record> actual, boolean check001cd) {
        logger.entry(expected, actual);

        try {
            OCBFileSystem fs = new OCBFileSystem(ApplicationType.UPDATE);
            if (expected.size() != actual.size()) {
                StringBuilder message = new StringBuilder();
                message.append("Number of records with RawRepo differ\n");

                message.append("Expected: [");
                for (UpdateTestcaseRecord expectedRecord : expected) {
                    message.append("\n");
                    message.append(fs.loadRecord(expectedRecord.getRecordFile().getParentFile(), expectedRecord.getRecord()).toString());
                }
                message.append("],\n");

                message.append("Actual: [");
                for (Record actualRecord : actual) {
                    message.append("\n");
                    message.append(RawRepo.decodeRecord(actualRecord.getContent()).toString());
                }
                message.append("]");

                throw new AssertionError(message);
            }

            for (UpdateTestcaseRecord testRecord : expected) {
                MarcRecord marcExpected = fs.loadRecord(testRecord.getRecordFile().getParentFile(), testRecord.getRecord());
                Record actualRecord = findRawRepoRecord(marcExpected, actual);

                if (actualRecord == null) {
                    throw new AssertionError(String.format("assertRecordListEquals: Record %s does not exist in rawrepo", RawRepo.getRecordId(marcExpected)));
                }
                assertRecordEqual(testRecord, actualRecord, check001cd);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            logger.exit();
        }
    }

    private static void assertRecordEqual(UpdateTestcaseRecord expected, Record actual, boolean check001cd) throws IOException {
        logger.entry(expected, actual);

        try {
            String formattedRecordId = String.format(FORMATTED_RECORD, expected.getRecord(), actual.getId().getBibliographicRecordId(), actual.getId().getAgencyId());

            assertEquals(String.format("Wrong mimetype of record %s", formattedRecordId), expected.getType(), TestcaseMimeType.fromValue(actual.getMimeType()));
            assertEquals(String.format("Wrong deletion mark of record %s", formattedRecordId), expected.isDeleted(), actual.isDeleted());

            OCBFileSystem fs = new OCBFileSystem(ApplicationType.UPDATE);

            MarcRecord marcExpected = fs.loadRecord(expected.getRecordFile().getParentFile(), expected.getRecord());
            MarcRecord marcActual = MarcConverter.convertFromMarcXChange(new String(actual.getContent(), "UTF-8"));
            if (marcExpected.getFields().size() != marcActual.getFields().size()) {
                String message = String.format("Number of fields differ. Expected record:\n%s\nActual record:\n%s",
                        marcExpected.toString(), marcActual.toString());
                fail(message);
            }

            for (int i = 0; i < marcExpected.getFields().size(); i++) {
                MarcField expectedField = marcExpected.getFields().get(i);
                MarcField actualField = marcActual.getFields().get(i);

                if (expectedField.getName().equals("001")) {
                    assertEquals("Compare field name of 001", expectedField.getName(), actualField.getName());
                    assertEquals("Compare indicator of 001", expectedField.getIndicator(), actualField.getIndicator());

                    for (int k = 0; k < expectedField.getSubfields().size(); k++) {
                        MarcSubField expectedSubField = expectedField.getSubfields().get(k);

                        if (expectedSubField.getName().equals("c") && !check001cd) {
                            continue;
                        }
                        if (expectedSubField.getName().equals("d") && !check001cd) {
                            continue;
                        }

                        MarcSubField actualSubField = actualField.getSubfields().get(k);
                        assertEquals("Compare 001" + expectedSubField.getName(), expectedSubField.toString(), actualSubField.toString());
                    }
                } else {
                    assertEquals("Compare field " + expectedField.getName() + "\ntestfile :" + expected.getRecord(), expectedField.toString(), actualField.toString());
                }
            }
        } finally {
            logger.exit();
        }

    }

    public static void assertQueueRecords(List<UpdateTestcaseRecord> expected, List<QueuedJob> QueuedJobs) throws IOException {
        logger.entry(expected, QueuedJobs);

        try {
            OCBFileSystem fs = new OCBFileSystem(ApplicationType.UPDATE);

            for (UpdateTestcaseRecord testRecord : expected) {
                MarcRecord record = fs.loadRecord(testRecord.getRecordFile().getParentFile(), testRecord.getRecord());
                RecordId recordId = RawRepo.getRecordId(record);

                List<String> expectedQueuedJobs = testRecord.getQueueWorkers();

                String formatedRecordId = String.format(FORMATTED_RECORD, testRecord.getRecord(), recordId.getBibliographicRecordId(), recordId.getAgencyId());

                if (testRecord.getQueueWorkers() == null) {
                    // This handles the cases where the testcase doesn't define which workers the queue jobs should be for
                    // TODO: Remove once all testcases use queueWorkers
                    boolean actuallyEnqueued = false;
                    for (QueuedJob job : QueuedJobs) {
                        if (job.getRecordId().equals(recordId)) {
                            actuallyEnqueued = true;
                            break;
                        }
                    }

                    if (testRecord.isEnqueued()) {
                        assertTrue(String.format("The record %s was expected in the queue in rawrepo", formatedRecordId), actuallyEnqueued);
                    } else {
                        assertFalse(String.format("The record %s was not expected in the queue in rawrepo", formatedRecordId), actuallyEnqueued);
                    }
                } else {
                    List<String> actualQueuedJobs = new ArrayList<>();

                    for (QueuedJob job : QueuedJobs) {
                        if (job.getRecordId().equals(recordId)) {
                            actualQueuedJobs.add(job.getWorker());
                        }
                    }

                    // We have to sort the lists first, as the objects won't be equal if the order is different.
                    Collections.sort(expectedQueuedJobs);
                    Collections.sort(actualQueuedJobs);

                    assertEquals("The amount of expected and actual queued jobs is not the same - ", expectedQueuedJobs.size(), actualQueuedJobs.size());
                    assertEquals("Unexpected difference between expected and actual queued jobs", expectedQueuedJobs, actualQueuedJobs);
                }
            }
        } finally {
            logger.exit();
        }
    }

    public static void assertRecordRelations(UpdateTestcaseRecord expected, RawRepoRelationType relationType, Set<RecordId> relations) throws IOException {
        logger.entry(expected, relationType, relations);

        try {
            OCBFileSystem fs = new OCBFileSystem(ApplicationType.UPDATE);

            MarcRecord record = fs.loadRecord(expected.getRecordFile().getParentFile(), expected.getRecord());
            RecordId recordId = RawRepo.getRecordId(record);

            String formatedRecordId = String.format(FORMATTED_RECORD, expected.getRecord(), recordId.getBibliographicRecordId(), recordId.getAgencyId());

            int expectedSize = 0;
            if (relationType.getExpectedRelationItems(expected) != null) {
                expectedSize = relationType.getExpectedRelationItems(expected).size();
            }

            if (expectedSize == relations.size()) {
                for (String name : relationType.getExpectedRelationItems(expected)) {
                    MarcRecord relatedRecord = fs.loadRecord(expected.getRecordFile().getParentFile(), name);
                    RecordId relatedRecordId = RawRepo.getRecordId(relatedRecord);

                    String formatedSiblingRecordId = String.format(FORMATTED_RECORD, name, relatedRecordId.getBibliographicRecordId(), relatedRecordId.getAgencyId());
                    String message = String.format(relationType.getExpectedFormatError(), formatedRecordId, formatedSiblingRecordId);
                    assertTrue(message, relations.contains(relatedRecordId));
                }
            } else {
                StringBuilder recordIds = new StringBuilder();
                for (RecordId id : relations) {
                    if (recordIds.length() > 0) {
                        recordIds.append(", ");
                    }

                    recordIds.append(String.format(FORMATTED_RECORD_ID, id.getBibliographicRecordId(), id.getAgencyId()));
                }

                throw new AssertionError(String.format(relationType.getUnexpectedFormatError(), formatedRecordId, recordIds.toString()));
            }
        } finally {
            logger.exit();
        }
    }

    private static Record findRawRepoRecord(MarcRecord record, List<Record> rawRepoRecords) {
        logger.entry(record, rawRepoRecords);

        Record result = null;
        try {
            RecordId recordId = RawRepo.getRecordId(record);
            for (Record rec : rawRepoRecords) {
                if (rec.getId().equals(recordId)) {
                    return result = rec;
                }
            }

            return result;
        } finally {
            logger.exit(result);
        }
    }
}
