package dk.dbc.ocbtools.testengine.executors;

import dk.dbc.common.records.MarcRecordReader;
import dk.dbc.commons.jdbc.util.JDBCUtil;
import dk.dbc.marc.binding.MarcRecord;
import dk.dbc.marc.writer.JsonLineWriter;
import dk.dbc.marc.writer.MarcWriterException;
import dk.dbc.ocbtools.commons.filesystem.OCBFileSystem;
import dk.dbc.ocbtools.commons.type.ApplicationType;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcaseRecord;
import dk.dbc.rawrepo.RawRepoDAO;
import dk.dbc.rawrepo.RawRepoException;
import dk.dbc.rawrepo.Record;
import dk.dbc.rawrepo.RecordId;
import dk.dbc.vipcore.exception.VipCoreException;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Helper Class to interact with the rawrepo database.
 * <p/>
 * It is the responsibility of the caller to ensure commits/rollbacks on the
 * connection.
 */
public class RawRepo {
    private static final XLogger logger = XLoggerFactory.getXLogger(RawRepo.class);

    private static final String JDBC_DRIVER_KEY = "rawrepo.jdbc.driver";
    private static final String JDBC_URL_KEY = "rawrepo.jdbc.conn.url";
    private static final String JDBC_USER_KEY = "rawrepo.jdbc.conn.user";
    private static final String JDBC_PASSWORD_KEY = "rawrepo.jdbc.conn.passwd";

    private static final String RECORD_ID_COL = "bibliographicrecordid";
    private static final String AGENCY_ID_COL = "agencyid";
    private static final String SELECT_RECORDS_SQL = "SELECT bibliographicrecordid, agencyid FROM records";
    private static final String SELECT_QUEUE_JOBS_SQL = "SELECT bibliographicrecordid, agencyid, worker FROM queue";

    private Properties settings;
    private RawRepoDAO dao;

    RawRepo(Properties settings, Connection connection) throws RawRepoException {
        this.settings = settings;
        this.dao = RawRepoDAO.builder(connection).build();
    }

    /**
     * Saves a list of records in rawrepo.
     *
     * @param baseDir Base directory of the testcase, so we can load each record.
     * @param records Records to store in the rawrepo.
     * @throws IOException      I/O errors if we can not load some records.
     * @throws RawRepoException rawrepo errors.
     */
    void saveRecords(File baseDir, List<UpdateTestcaseRecord> records) throws IOException, RawRepoException, MarcWriterException, VipCoreException {
        logger.entry(baseDir, records);

        try {
            for (UpdateTestcaseRecord record : records) {
                saveRecord(baseDir, record);
            }
        } finally {
            logger.exit();
        }
    }

    /**
     * Saves a record in rawrepo.
     *
     * @param baseDir Base directory of the testcase, so we can load each record.
     * @param record  Record to store in the rawrepo.
     * @throws IOException      I/O errors if we can not load some records.
     * @throws RawRepoException rawrepo errors.
     */
    private void saveRecord(File baseDir, UpdateTestcaseRecord record) throws IOException, RawRepoException, MarcWriterException, VipCoreException {
        logger.entry(baseDir, record);

        try {
            OCBFileSystem fs = new OCBFileSystem(ApplicationType.UPDATE);
            MarcRecord marcRecord = fs.loadRecord(baseDir, record.getRecord());
            RecordId recId = getRecordId(marcRecord);

            if (recId != null) {
                Record newRecord = dao.fetchRecord(recId.getBibliographicRecordId(), recId.getAgencyId());
                newRecord.setDeleted(record.isDeleted());
                newRecord.setMimeType(record.getType().value());
                newRecord.setContentJson(encodeRecord(marcRecord));
                dao.saveRecord(newRecord);

                if (record.isEnqueued()) {
                    dao.changedRecord(settings.getProperty("rawrepo.provider.name"), newRecord.getId());
                }
            }
        } finally {
            logger.exit();
        }
    }

    void saveRelation(MarcRecord commonOrParentRecord, MarcRecord enrichmentOrChildRecord) throws RawRepoException {
        logger.entry(commonOrParentRecord, enrichmentOrChildRecord);

        try {
            RecordId recordId = getRecordId(enrichmentOrChildRecord);

            if (recordId != null) {
                final Set<RecordId> references = dao.getRelationsFrom(recordId);
                references.add(getRecordId(commonOrParentRecord));

                dao.setRelationsFrom(recordId, references);
            }
        } finally {
            logger.exit();
        }
    }

    private Record fetchRecord(String recordId, Integer agencyId) throws RawRepoException {
        logger.entry(recordId, agencyId);

        Record result = null;
        try {
            return result = dao.fetchRecord(recordId, agencyId);
        } finally {
            logger.exit(result);
        }
    }

    /**
     * Encodes the record as marcxchange.
     *
     * @param record The record to encode.
     * @return The encoded record as a sequence of bytes.
     * @throws MarcWriterException if the record can not be encoded in marcxchange.
     */
    private byte[] encodeRecord(MarcRecord record) throws MarcWriterException {
        logger.entry(record);
        byte[] result = null;

        try {
            JsonLineWriter writer = new JsonLineWriter();

            return writer.write(record, StandardCharsets.UTF_8);
        } finally {
            logger.exit(result);
        }
    }

    public static RecordId getRecordId(MarcRecord record) {
        logger.entry();

        try {
            MarcRecordReader reader = new MarcRecordReader(record);

            String recordId = reader.getRecordId();
            int agencyId = reader.getAgencyIdAsInt();

            return new RecordId(recordId, agencyId);
        } catch (NumberFormatException ex) {
            return null;
        } finally {
            logger.exit();
        }
    }

    static Connection getConnection(Properties settings) throws ClassNotFoundException, SQLException, IOException {
        Class.forName(settings.getProperty(JDBC_DRIVER_KEY));

        String url = settings.getProperty(JDBC_URL_KEY);
        String user = settings.getProperty(JDBC_USER_KEY);
        String password = settings.getProperty(JDBC_PASSWORD_KEY);
        logger.debug("rr getConnection {}/{}/{}", url, user, password);

        Connection conn = DriverManager.getConnection(url, user, password);
        conn.setAutoCommit(false);

        return conn;
    }

    static void setupDatabase(Properties settings) throws SQLException, IOException, ClassNotFoundException {
        logger.entry(settings);
        logger.debug("setup RR");

        try (Connection conn = getConnection(settings)) {
            try {
                logger.debug("Setup rawrepo queue workers and rules");

                QueueSetup queueSetup = new QueueSetup();
                List<String> queueInserts = queueSetup.getQueueRulesInserts();
                for (String ins : queueInserts) {
                    JDBCUtil.update(conn, ins);
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                logger.error(ex.getMessage(), ex);

                throw ex;
            }
        } finally {
            logger.exit();
        }
    }

    static void teardownDatabase(Properties settings) throws SQLException, IOException, ClassNotFoundException {
        logger.entry(settings);

        logger.debug("teardown RR");
        Set<String> settingsStr = settings.stringPropertyNames();
        String[] ar = settingsStr.toArray(new String[0]);
        for (String anAr : ar) {
            logger.debug("Prop {} => {}", anAr, settings.getProperty(anAr));
        }
        try (Connection conn = getConnection(settings)) {
            try {
                JDBCUtil.update(conn, "DELETE FROM relations");
                JDBCUtil.update(conn, "DELETE FROM records");
                JDBCUtil.update(conn, "DELETE FROM records_archive");
                JDBCUtil.update(conn, "DELETE FROM records_summary");
                JDBCUtil.update(conn, "DELETE FROM queue");
                JDBCUtil.update(conn, "DELETE FROM jobdiag");

                JDBCUtil.update(conn, "DELETE FROM queuerules");
                JDBCUtil.update(conn, "DELETE FROM queueworkers");

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                logger.error(ex.getMessage(), ex);

                throw ex;
            }
        } finally {
            logger.exit();
        }
    }

    /**
     * Loads all records from rawrepo.
     * <p/>
     * RawRepoDAO does not support is, so we use JDBC in stead.
     *
     * @param settings Settings to connect to the database.
     * @return The loaded records.
     * @throws SQLException           SQL errors.
     * @throws IOException            I/O errors.
     * @throws ClassNotFoundException If we can not initialize RawRepoDAO.
     * @throws RawRepoException       rawrepo errors.
     */
    static List<Record> loadRecords(Properties settings) throws SQLException, IOException, ClassNotFoundException, RawRepoException {
        logger.entry(settings);

        Set<String> settingsStr = settings.stringPropertyNames();
        String[] ar = settingsStr.toArray(new String[0]);
        for (String anAr : ar) {
            logger.debug("Prop {} = value {}", anAr, settings.getProperty(anAr));
        }
        List<Record> records = new ArrayList<>();
        try (Connection conn = getConnection(settings)) {
            RawRepo rawRepo = new RawRepo(settings, conn);
            for (Map<String, Object> entry : JDBCUtil.queryForRowMaps(conn, SELECT_RECORDS_SQL)) {
                String recordId = (String) (entry.get(RECORD_ID_COL));
                BigDecimal agencyId = (BigDecimal) (entry.get(AGENCY_ID_COL));

                records.add(rawRepo.fetchRecord(recordId, agencyId.intValue()));
            }

            return records;
        } catch (SQLException | RawRepoException ex) {
            logger.error(ex.getMessage(), ex);

            throw ex;
        } finally {
            logger.exit(records);
        }
    }

    /**
     * Loads all records from the ocb-test queue.
     *
     * @param settings Settings to connect to the database.
     * @return The loaded records.
     * @throws SQLException           SQL errors.
     * @throws IOException            I/O errors.
     * @throws ClassNotFoundException If we can not initialize RawRepoDAO.
     * @throws RawRepoException       rawrepo errors.
     */
    static List<QueuedJob> loadQueuedRecords(Properties settings) throws RawRepoException, SQLException, IOException, ClassNotFoundException {
        logger.entry();

        List<QueuedJob> result = new ArrayList<>();
        try (Connection conn = getConnection(settings)) {

            for (Map<String, Object> entry : JDBCUtil.queryForRowMaps(conn, SELECT_QUEUE_JOBS_SQL)) {
                result.add(QueuedJob.fromMap(entry));
            }

            return result;
        } finally {
            logger.exit(result);
        }
    }

    /**
     * Loads all relations of the given type for some record id.
     *
     * @param settings Settings to connect to the database.
     * @param recordId Record id.
     * @param type     Relation type.
     * @return The loaded records.
     * @throws SQLException           SQL errors.
     * @throws IOException            I/O errors.
     * @throws ClassNotFoundException If we can not initialize RawRepoDAO.
     * @throws RawRepoException       rawrepo errors.
     */
    static Set<RecordId> loadRelations(Properties settings, RecordId recordId, RawRepoRelationType type) throws RawRepoException, SQLException, IOException, ClassNotFoundException {
        logger.entry();

        Set<RecordId> result = null;
        try (Connection conn = getConnection(settings)) {
            RawRepoDAO dao = RawRepoDAO.builder(conn).build();

            switch (type) {
                case CHILD:
                    return result = dao.getRelationsChildren(recordId);
                case SIBLING:
                    return result = dao.getRelationsSiblingsToMe(recordId);
            }

            return result;
        } finally {
            logger.exit(result);
        }
    }
}
