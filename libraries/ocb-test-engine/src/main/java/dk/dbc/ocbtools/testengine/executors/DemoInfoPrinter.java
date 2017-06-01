package dk.dbc.ocbtools.testengine.executors;

import dk.dbc.buildservice.service.api.BuildRequest;
import dk.dbc.buildservice.service.api.BuildResult;
import dk.dbc.holdingsitems.HoldingsItemsException;
import dk.dbc.iscrum.records.MarcRecord;
import dk.dbc.iscrum.utils.json.Json;
import dk.dbc.ocbtools.commons.filesystem.OCBFileSystem;
import dk.dbc.ocbtools.commons.type.ApplicationType;
import dk.dbc.ocbtools.testengine.testcases.*;
import dk.dbc.rawrepo.RawRepoException;
import dk.dbc.rawrepo.Record;
import dk.dbc.rawrepo.RecordId;
import dk.dbc.updateservice.service.api.UpdateRecordRequest;
import dk.dbc.updateservice.service.api.UpdateRecordResult;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

class DemoInfoPrinter {
    private static final XLogger logger = XLoggerFactory.getXLogger(DemoInfoPrinter.class);

    private static final int WIDTH = 72;
    private static final String HEADER_LINE = makeString("#", WIDTH);
    private static final String SEP_LINE = makeString("-", WIDTH);

    void printHeader(BaseTestcase tc, TestExecutor executor) {
        logger.info("");
        logger.info(HEADER_LINE);
        logger.info("Testing {}", tc.getName());
        logger.info(SEP_LINE);
        logger.info("");
        logger.info("Executor: {}", executor.name());
        logger.info("Distribution: {}", tc.getDistributionName());
        logger.info("File: {}", tc.getFile().getAbsolutePath());
        logger.info("Description: {}", tc.getDescription());
    }

    void printFooter() {
        logger.info(HEADER_LINE);
        logger.info("");
    }

    void printSetup(UpdateTestcase tc) {
        try {
            logger.info(SEP_LINE);
            logger.info("");

            if (tc.getSetup() == null || tc.getSetup().getHoldings() == null || tc.getSetup().getHoldings().isEmpty()) {
                logger.info("Holdings: Ingen opsætning");
            } else {
                logger.info("Holdings: {}", tc.getSetup().getHoldings());
            }

            if (tc.getSetup() == null || tc.getSetup().getSolr() == null || tc.getSetup().getSolr().isEmpty()) {
                logger.info("Solr: Ingen opsætning");
            } else {
                logger.info("Solr: \n{}", Json.encodePretty(tc.getSetup().getSolr()));
            }

            if (tc.getSetup() == null || tc.getSetup().getRawrepo() == null || tc.getSetup().getRawrepo().isEmpty()) {
                logger.info("Rawrepo: Ingen opsætning");
            } else {
                logger.info("Rawrepo:");
                logger.info(SEP_LINE);
                OCBFileSystem fs = new OCBFileSystem(ApplicationType.UPDATE);
                File baseDir = tc.getFile().getParentFile();

                for (UpdateTestcaseRecord testRecord : tc.getSetup().getRawrepo()) {
                    logger.info("File: {}", testRecord.getRecord());
                    logger.info("Content:\n{}", fs.loadRecord(baseDir, testRecord.getRecord()).toString());
                }
            }

            logger.info(SEP_LINE);
            logger.info("");
        } catch (IOException ex) {
            logger.error("Failed to print setup: {}", ex.getMessage());
            logger.debug("Stacktrace: ", ex);
        }
    }

    void printLocaleRequest(UpdateTestcase tc) {
        try {
            logger.info(SEP_LINE);
            logger.info("Validating request against JavaScript");
            logger.info(SEP_LINE);
            logger.info("");
            logger.info("Template name: {}", tc.getRequest().getTemplateName());
            logger.info("Record: {}", tc.getRequest().getRecord());
            logger.info("{}\n{}", SEP_LINE, tc.loadRecord().toString());
        } catch (Exception ex) {
            logger.error("Failed to print setup: {}", ex.getMessage());
            logger.debug("Stacktrace: ", ex);
        }
    }

    void printLocaleRequest(BuildTestcase tc) {
        try {
            logger.info(SEP_LINE);
            logger.info("Building record with JavaScript");
            logger.info(SEP_LINE);
            logger.info("");
            logger.info("Template name: {}", tc.getRequest().getTemplateName());
            logger.info("Record: {}", tc.getRequest().getRecord());
            logger.info(SEP_LINE);
            logger.info(tc.loadRequestRecord().toString());
        } catch (Exception ex) {
            logger.error("Failed to print setup: {}", ex.getMessage());
            logger.debug("Stacktrace: ", ex);
        }
    }

    void printRemoteDatabases(UpdateTestcase tc, Properties settings) throws SQLException, ClassNotFoundException, HoldingsItemsException, RawRepoException {
        try {
            logger.info(SEP_LINE);
            logger.info("");

            try (Connection conn = Holdings.getConnection(settings)) {
                logger.info("Holdings: {}", Holdings.loadHoldingsForRecord(conn, tc.loadRecord()));
            }

            List<Record> rawRepoRecords = RawRepo.loadRecords(settings);
            if (rawRepoRecords == null || rawRepoRecords.isEmpty()) {
                logger.info("Rawrepo: Empty");
            } else {
                logger.info("Rawrepo:");
                logger.info(SEP_LINE);

                for (Record rawRepoRecord : rawRepoRecords) {
                    logger.info("Id: [{}:{}]", rawRepoRecord.getId().getBibliographicRecordId(), rawRepoRecord.getId().getAgencyId());
                    logger.info("Mimetype: {}", TestcaseMimeType.fromValue(rawRepoRecord.getMimeType()));
                    logger.info("Deleted: {}", rawRepoRecord.isDeleted());
                    logger.info("TrackingID: {}", rawRepoRecord.getTrackingId());
                    logger.info("");
                    logger.info("Children: {}", formatRecordIds(RawRepo.loadRelations(settings, rawRepoRecord.getId(), RawRepoRelationType.CHILD)));
                    logger.info("Siblings: {}", formatRecordIds(RawRepo.loadRelations(settings, rawRepoRecord.getId(), RawRepoRelationType.SIBLING)));
                    logger.info("");
                    logger.info("Content:\n{}", RawRepo.decodeRecord(rawRepoRecord.getContent()));
                }

                logger.info("Queued records: {}", formatQueuedJobs(RawRepo.loadQueuedRecords(settings)));
            }

            logger.info(SEP_LINE);
            logger.info("");
        } catch (IOException ex) {
            logger.error("Failed to print setup: {}", ex.getMessage());
            logger.debug("Stacktrace: ", ex);
        }
    }

    void printRequest(UpdateRecordRequest request, MarcRecord record) throws IOException {
        logger.entry();

        try {
            logger.info("Request record:\n{}", record);
            logger.info("Webservice Request: {}", Json.encodePretty(request));
        } finally {
            logger.exit();
        }
    }

    void printRequest(BuildRequest request, MarcRecord record) throws IOException {
        logger.entry();

        try {
            logger.info("Request record:\n{}", record);
            logger.info("Webservice Request: {}", Json.encodePretty(request));
        } finally {
            logger.exit();
        }
    }

    void printResponse(UpdateRecordResult response) throws IOException {
        logger.entry();

        try {
            logger.info("Response: {}", Json.encodePretty(response));
        } finally {
            logger.exit();
        }
    }

    void printResponse(BuildResult response) throws IOException {
        logger.entry();

        try {
            logger.info("Response: {}", Json.encodePretty(response));
        } finally {
            logger.exit();
        }
    }

    private String formatQueuedJobs(Iterable<QueuedJob> jobs) {
        logger.entry();

        String result = "";
        try {
            List<RecordId> recordIds = new ArrayList<>();
            for (QueuedJob job : jobs) {
                recordIds.add(job.getRecordId());
            }
            return formatRecordIds(recordIds);
        } finally {
            logger.exit(result);
        }
    }

    private String formatRecordIds(Iterable<RecordId> ids) {
        logger.entry();

        String result = "";
        try {
            StringBuilder sb = new StringBuilder();

            String sep = "";
            sb.append('[');
            for (RecordId recordId : ids) {
                sb.append(sep);
                sb.append('{');
                sb.append(recordId.getBibliographicRecordId());
                sb.append(':');
                sb.append(recordId.getAgencyId());
                sb.append('}');

                sep = ", ";
            }
            sb.append(']');

            return result = sb.toString();
        } finally {
            logger.exit(result);
        }
    }

    private static String makeString(String ch, int length) {
        String str = "";
        for (int i = 0; i < length; i++) {
            str += ch;
        }

        return str;
    }
}
