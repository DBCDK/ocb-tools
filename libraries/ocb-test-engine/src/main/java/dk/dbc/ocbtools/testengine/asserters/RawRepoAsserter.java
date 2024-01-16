package dk.dbc.ocbtools.testengine.asserters;


import dk.dbc.common.records.MarcRecordReader;
import dk.dbc.marc.binding.DataField;
import dk.dbc.marc.binding.MarcRecord;
import dk.dbc.marc.binding.SubField;
import dk.dbc.marc.reader.MarcReaderException;
import dk.dbc.ocbtools.commons.filesystem.OCBFileSystem;
import dk.dbc.ocbtools.commons.type.ApplicationType;
import dk.dbc.ocbtools.testengine.executors.QueuedJob;
import dk.dbc.ocbtools.testengine.executors.RawRepo;
import dk.dbc.ocbtools.testengine.executors.RawRepoRelationType;
import dk.dbc.ocbtools.testengine.rawrepo.MarcConverter;
import dk.dbc.ocbtools.testengine.testcases.TestcaseMimeType;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcase;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcaseRecord;
import dk.dbc.rawrepo.Record;
import dk.dbc.rawrepo.RecordId;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Asserter class to check records against rawrepo.
 */
public class RawRepoAsserter {
    private static final XLogger logger = XLoggerFactory.getXLogger(RawRepoAsserter.class);

    private static final String FORMATTED_RECORD = "{%s - %s:%s}";
    private static final String FORMATTED_RECORD_ID = "{%s:%s}";

    public static void assertRecordListEquals(List<UpdateTestcaseRecord> expected, List<Record> actual, UpdateTestcase tc) {
        logger.entry(expected, actual);
        actual.sort(recordComparator());

        try {
            OCBFileSystem fs = new OCBFileSystem(ApplicationType.UPDATE);
            if (expected.size() != actual.size()) {
                StringBuilder message = new StringBuilder();
                message.append("Number of records with RawRepo differ\n");

                String exp = expected.stream()
                        .map(UpdateTestcaseRecord::getRecordFile)
                        .map(fs::loadRecord)
                        .sorted(marcRecordComparator())
                        .map(Objects::toString)
                        .collect(Collectors.joining("\n"));
                message.append("Expected: [\n").append(exp).append("],\n");

                message.append("Actual: [");
                for (Record actualRecord : actual) {
                    message.append("\n");
                    message.append(MarcConverter.convertFromMarcJson(actualRecord.getContentJson()));
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
                assertRecordEqual(testRecord, actualRecord, tc);
            }
        } catch (IOException | MarcReaderException ex) {
            ex.printStackTrace();
        } finally {
            logger.exit();
        }
    }

    private static Comparator<? super MarcRecord> marcRecordComparator() {
        return Comparator.comparing((MarcRecord m) -> new MarcRecordReader(m).getRecordId()).thenComparing(m -> new MarcRecordReader(m).getAgencyId());
    }

    private static Comparator<? super Record> recordComparator() {
        return Comparator.comparing((Record r) -> r.getId().getBibliographicRecordId()).thenComparing(r -> r.getId().getAgencyId());
    }


    private static void assertRecordEqual(UpdateTestcaseRecord expected, Record actual, UpdateTestcase tc) throws IOException {
        logger.entry(expected, actual);

        try {
            final String formattedRecordId = String.format(FORMATTED_RECORD, expected.getRecord(), actual.getId().getBibliographicRecordId(), actual.getId().getAgencyId());

            assertEquals(String.format("Wrong mimetype of record %s", formattedRecordId), expected.getType(), TestcaseMimeType.fromValue(actual.getMimeType()));
            assertEquals(String.format("Wrong deletion mark of record %s", formattedRecordId), expected.isDeleted(), actual.isDeleted());

            OCBFileSystem fs = new OCBFileSystem(ApplicationType.UPDATE);

            final MarcRecord marcExpected = fs.loadRecord(expected.getRecordFile().getParentFile(), expected.getRecord());
            MarcRecord marcActual = MarcConverter.convertFromMarcJson(actual.getContentJson());
            if (marcExpected.getFields().size() != marcActual.getFields().size()) {
                String message = String.format("Number of fields differ. File : %s\nExpected record:\n%s\nActual record:\n%s",
                        expected.getRecordFile().getName(), marcExpected, marcActual);
                fail(message);
            }

            for (int i = 0; i < marcExpected.getFields().size(); i++) {
                final DataField expectedField = marcExpected.getFields(DataField.class).get(i);
                final DataField actualField = marcActual.getFields(DataField.class).get(i);

                if (expectedField.getTag().equals("001")) {
                    assertEquals("Compare field name of 001", expectedField.getTag(), actualField.getTag());

                    final String expectedIndicators = String.format("%s%s%s", expectedField.getInd1(), expectedField.getInd2(), expectedField.getInd3());
                    final String actualIndicators = String.format("%s%s%s", actualField.getInd1(), actualField.getInd2(), actualField.getInd3());

                    assertEquals("Compare indicator of 001", expectedIndicators, actualIndicators);


                    for (int k = 0; k < expectedField.getSubFields().size(); k++) {
                        SubField expectedSubField = expectedField.getSubFields().get(k);

                        if (expectedSubField.getCode() == 'c' && !tc.getRequest().isCheck001c()) {
                            continue;
                        }
                        if (expectedSubField.getCode() == 'd' && !tc.getRequest().isCheck001d()) {
                            continue;
                        }

                        SubField actualSubField = actualField.getSubFields().get(k);
                        assertEquals("Compare 001" + expectedSubField.getCode(), expectedSubField.toString(), actualSubField.toString());
                    }
                } else {
                    // This is a bit messy. It is done because subject records from metakompas requests has a weekcode in field d09 - could be useful in other cases
                    if (tc.getRequest().getIgnoreFieldsInMatch().contains(expectedField.getTag())) {
                        continue;
                    }
                    assertEquals("Compare field " + expectedField.getTag() + "\ntestfile :" + expected.getRecord(), expectedField.toString(), actualField.toString());
                }
            }
        } catch (MarcReaderException e) {
            throw new RuntimeException(e);
        } finally {
            logger.exit();
        }

    }

    public static void assertQueueRecords(List<UpdateTestcaseRecord> expected, List<QueuedJob> queuedJobs) throws IOException {
        logger.entry(expected, queuedJobs);

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
                    for (QueuedJob queuedJob : queuedJobs) {
                        if (queuedJob.getRecordId().equals(recordId)) {
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
                    for (QueuedJob queuedJob : queuedJobs) {
                        if (queuedJob.getRecordId().equals(recordId)) {
                            actualQueuedJobs.add(queuedJob.getWorker());
                        }
                    }

                    // We have to sort the lists first, as the objects won't be equal if the order is different.
                    Collections.sort(expectedQueuedJobs);
                    Collections.sort(actualQueuedJobs);

                    assertEquals("The amount of expected and actual queued jobs is not the same for " + formatedRecordId + " -", expectedQueuedJobs.size(), actualQueuedJobs.size());
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
                if (relationType.getExpectedRelationItems(expected) != null) {
                    for (String name : relationType.getExpectedRelationItems(expected)) {
                        MarcRecord relatedRecord = fs.loadRecord(expected.getRecordFile().getParentFile(), name);
                        RecordId relatedRecordId = RawRepo.getRecordId(relatedRecord);

                        String formatedSiblingRecordId = String.format(FORMATTED_RECORD, name, relatedRecordId.getBibliographicRecordId(), relatedRecordId.getAgencyId());
                        String message = String.format(relationType.getExpectedFormatError(), formatedRecordId, formatedSiblingRecordId);
                        assertTrue(message, relations.contains(relatedRecordId));
                    }
                }
            } else {
                StringBuilder recordIds = new StringBuilder();
                for (RecordId id : relations) {
                    if (recordIds.length() > 0) {
                        recordIds.append(", ");
                    }

                    recordIds.append(String.format(FORMATTED_RECORD_ID, id.getBibliographicRecordId(), id.getAgencyId()));
                }

                if (relationType.getExpectedRelationItems(expected) != null) {
                    throw new AssertionError(String.format(relationType.getUnexpectedFormatError(), formatedRecordId, relationType.getExpectedRelationItems(expected), recordIds.toString()));
                } else {
                    throw new AssertionError(String.format(relationType.getUnexpectedFormatError(), formatedRecordId, "EMPTY", recordIds.toString()));
                }
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
