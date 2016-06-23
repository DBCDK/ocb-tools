//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.executors;

//-----------------------------------------------------------------------------

import dk.dbc.iscrum.utils.logback.filters.BusinessLoggerFilter;
import dk.dbc.commons.jdbc.util.JDBCUtil;
import dk.dbc.iscrum.records.MarcConverter;
import dk.dbc.iscrum.records.MarcReader;
import dk.dbc.iscrum.records.MarcRecord;
import dk.dbc.iscrum.records.MarcXchangeFactory;
import dk.dbc.iscrum.records.marcxchange.CollectionType;
import dk.dbc.iscrum.records.marcxchange.ObjectFactory;
import dk.dbc.iscrum.records.marcxchange.RecordType;
import dk.dbc.ocbtools.commons.filesystem.OCBFileSystem;
import dk.dbc.ocbtools.commons.type.ApplicationType;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcaseRecord;
import dk.dbc.rawrepo.QueueJob;
import dk.dbc.rawrepo.RawRepoDAO;
import dk.dbc.rawrepo.RawRepoException;
import dk.dbc.rawrepo.Record;
import dk.dbc.rawrepo.RecordId;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

//-----------------------------------------------------------------------------

/**
 * Helper Class to interact with the rawrepo database.
 * <p/>
 * It is the responsibility of the caller to ensure commits/rollbacks on the
 * connection.
 */
public class RawRepo {
    private static final XLogger logger = XLoggerFactory.getXLogger(RawRepo.class);
    private static final XLogger output = XLoggerFactory.getXLogger(BusinessLoggerFilter.LOGGER_NAME);

    private static final String JDBC_DRIVER_KEY = "rawrepo.jdbc.driver";
    private static final String JDBC_URL_KEY = "rawrepo.jdbc.conn.url";
    private static final String JDBC_USER_KEY = "rawrepo.jdbc.conn.user";
    private static final String JDBC_PASSWORD_KEY = "rawrepo.jdbc.conn.passwd";

    private static final String RECORD_ID_COL = "bibliographicrecordid";
    private static final String AGENCY_ID_COL = "agencyid";
    private static final String SELECT_RECORDS_SQL = "SELECT bibliographicrecordid, agencyid FROM records";

    private static final String OCBTEST_WORKER_NAME = "ocb-test";
    private static final String BASIS_WORKER_NAME = "basis-decentral";
    private static final String BASIS_WORKER_MIMETYPE = "text/decentral+marcxchange";
    private static final String[] WORKER_NAMES = {OCBTEST_WORKER_NAME, "fbs-sync", "solr-sync", "broend-sync", BASIS_WORKER_NAME};

    private Properties settings;
    private RawRepoDAO dao;

    public RawRepo(Properties settings, Connection connection) throws RawRepoException {
        this.settings = settings;
        this.dao = RawRepoDAO.builder(connection).build();
        try {
            teardownDatabase(settings);
        } catch (Throwable upe) {
            logger.debug("Init fail", upe);
        }
    }

    /**
     * Saves a list of records in rawrepo.
     *
     * @param baseDir Base directory of the testcase, so we can load each record.
     * @param records Records to store in the rawrepo.
     * @throws IOException      I/O errors if we can not load some records.
     * @throws RawRepoException rawrepo errors.
     * @throws JAXBException    XML errors.
     */
    public void saveRecords(File baseDir, List<UpdateTestcaseRecord> records) throws IOException, RawRepoException, JAXBException {
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
     * @throws JAXBException    XML errors.
     */
    public void saveRecord(File baseDir, UpdateTestcaseRecord record) throws IOException, RawRepoException, JAXBException {
        logger.entry(baseDir, record);

        try {
            OCBFileSystem fs = new OCBFileSystem(ApplicationType.UPDATE);
            MarcRecord marcRecord = fs.loadRecord(baseDir, record.getRecord());
            RecordId recId = getRecordId(marcRecord);

            if (recId != null) {
                Record newRecord = dao.fetchRecord(recId.getBibliographicRecordId(), recId.getAgencyId());
                newRecord.setDeleted(record.isDeleted());
                newRecord.setMimeType(record.getType().value());
                newRecord.setContent(encodeRecord(marcRecord));
                dao.saveRecord(newRecord);

                if (record.isEnqueued()) {
                    dao.changedRecord( settings.getProperty( "rawrepo.provider.name" ), newRecord.getId(), newRecord.getMimeType());
                }
            }
        } finally {
            logger.exit();
        }
    }

    public void saveRelation(MarcRecord commonOrParentRecord, MarcRecord enrichmentOrChildRecord) throws RawRepoException {
        logger.entry(commonOrParentRecord, enrichmentOrChildRecord);

        try {
            RecordId recordId = getRecordId(enrichmentOrChildRecord);

            if (recordId != null) {
                final HashSet<RecordId> references = new HashSet<>();
                references.add(getRecordId(commonOrParentRecord));

                dao.setRelationsFrom(recordId, references);
            }
        } finally {
            logger.exit();
        }
    }

    public Record fetchRecord(String recordId, Integer agencyId) throws RawRepoException {
        logger.entry(recordId, agencyId);

        Record result = null;
        try {
            return result = dao.fetchRecord(recordId, agencyId);
        } finally {
            logger.exit(result);
        }
    }

    public static MarcRecord decodeRecord(byte[] bytes) throws UnsupportedEncodingException {
        return MarcConverter.convertFromMarcXChange(new String(bytes, "UTF-8"));
    }

    /**
     * Encodes the record as marcxchange.
     *
     * @param record The record to encode.
     * @return The encoded record as a sequence of bytes.
     * @throws javax.xml.bind.JAXBException         if the record can not be encoded in marcxchange.
     * @throws java.io.UnsupportedEncodingException if the record can not be encoded in UTF-8
     */
    private byte[] encodeRecord(MarcRecord record) throws JAXBException, UnsupportedEncodingException {
        logger.entry(record);
        byte[] result = null;

        try {

            if (record.getFields().isEmpty()) {
                return null;
            }

            dk.dbc.iscrum.records.marcxchange.RecordType marcXchangeType = MarcXchangeFactory.createMarcXchangeFromMarc(record);

            ObjectFactory objectFactory = new ObjectFactory();
            JAXBElement<RecordType> jAXBElement = objectFactory.createRecord(marcXchangeType);

            JAXBContext jc = JAXBContext.newInstance(CollectionType.class);
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://www.loc.gov/standards/iso25577/marcxchange-1-1.xsd");

            StringWriter recData = new StringWriter();
            marshaller.marshal(jAXBElement, recData);

            logger.info("Marshelled record: {}", recData.toString());
            result = recData.toString().getBytes("UTF-8");

            return result;
        } finally {
            logger.exit(result);
        }
    }

    public static RecordId getRecordId(MarcRecord record) {
        logger.entry();

        try {
            String recId = MarcReader.getRecordValue(record, "001", "a");
            int agencyId = Integer.valueOf(MarcReader.getRecordValue(record, "001", "b"), 10);

            return new RecordId(recId, agencyId);
        } catch (NumberFormatException ex) {
            return null;
        } finally {
            logger.exit();
        }
    }

    public static Connection getConnection(Properties settings) throws ClassNotFoundException, SQLException, IOException {
        Class.forName(settings.getProperty(JDBC_DRIVER_KEY));

        String url = settings.getProperty(JDBC_URL_KEY);
        String user = settings.getProperty(JDBC_USER_KEY);
        String password = settings.getProperty(JDBC_PASSWORD_KEY);
        logger.info("rr getConnection {}/{}/{}", url, user, password);

        Connection conn = DriverManager.getConnection(url, user, password);
        conn.setAutoCommit(false);

        return conn;
    }

    public static void setupDatabase(Properties settings) throws SQLException, IOException, ClassNotFoundException {
        logger.entry(settings);
        logger.info("setup RR");

        try (Connection conn = getConnection(settings)) {
            try {
                output.debug("Setup rawrepo queue workers and rules");
                for (String name : WORKER_NAMES) {
                    output.debug("Setup queue worker: {}", name);
                    JDBCUtil.update(conn, "INSERT INTO queueworkers(worker) VALUES(?)", name);

                    output.debug("Setup queue rule for worker: {}", name);
                    if (name.equals(BASIS_WORKER_NAME)) {
                        JDBCUtil.update(conn, "INSERT INTO queuerules(provider, worker, mimetype, changed, leaf) VALUES(?, ?, ?, ?, ?)", settings.getProperty( "rawrepo.provider.name" ), name, BASIS_WORKER_MIMETYPE, "Y", "A");
                    } else {
                        JDBCUtil.update(conn, "INSERT INTO queuerules(provider, worker, changed, leaf) VALUES(?, ?, ?, ?)", settings.getProperty( "rawrepo.provider.name" ), name, "Y", "A");
                    }
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

    public static void teardownDatabase(Properties settings) throws SQLException, IOException, ClassNotFoundException {
        logger.entry(settings);

        logger.info("teardown RR");
        Set<String> settingsStr = settings.stringPropertyNames();
        String[] ar = settingsStr.toArray(new String[0]);
        for (int ix = 0; ix < ar.length;ix++) {
            logger.error("Prop {} = value {}", ar[ix], settings.getProperty(ar[ix]));
        }
        try (Connection conn = getConnection(settings)) {
            try {
                JDBCUtil.update(conn, "DELETE FROM relations");
                JDBCUtil.update(conn, "DELETE FROM records");
                JDBCUtil.update(conn, "DELETE FROM records_archive");
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
    public static List<Record> loadRecords(Properties settings) throws SQLException, IOException, ClassNotFoundException, RawRepoException {
        logger.entry(settings);

        Set<String> settingsStr = settings.stringPropertyNames();
        String[] ar = settingsStr.toArray(new String[0]);
        for (int ix = 0; ix < ar.length;ix++) {
            logger.error("Prop {} = value {}", ar[ix], settings.getProperty(ar[ix]));
        }
        List<Record> records = new ArrayList<>();
        try (Connection conn = getConnection(settings)) {
            RawRepo rawRepo = new RawRepo( settings, conn);
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
    public static List<RecordId> loadQueuedRecords(Properties settings) throws RawRepoException, SQLException, IOException, ClassNotFoundException {
        logger.entry();

        List<RecordId> result = new ArrayList<>();
        try (Connection conn = getConnection(settings)) {
            RawRepoDAO dao = RawRepoDAO.builder(conn).build();

            final int COUNT_SIZE = 10;
            List<QueueJob> jobs;
            do {
                jobs = dao.dequeue(OCBTEST_WORKER_NAME, COUNT_SIZE);
                for (QueueJob job : jobs) {
                    result.add(job.getJob());
                }
            }
            while (jobs != null && !jobs.isEmpty());

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
    public static Set<RecordId> loadRelations(Properties settings, RecordId recordId, RawRepoRelationType type) throws RawRepoException, SQLException, IOException, ClassNotFoundException {
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
