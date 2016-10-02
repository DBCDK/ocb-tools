package dk.dbc.ocbtools.testengine.executors;

import dk.dbc.commons.jdbc.util.JDBCUtil;
import dk.dbc.holdingsitems.HoldingsItemsDAO;
import dk.dbc.holdingsitems.HoldingsItemsException;
import dk.dbc.holdingsitems.Record;
import dk.dbc.holdingsitems.RecordCollection;
import dk.dbc.iscrum.records.MarcRecord;
import dk.dbc.rawrepo.RecordId;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Class to setup holdings in the holdingsitems database.
 * <p/>
 * It is the responsibility of the caller to ensure commits/rollbacks on the
 * connection.
 */
class Holdings {

    private static final XLogger logger = XLoggerFactory.getXLogger(Holdings.class);

    private static final String JDBC_DRIVER_KEY = "holdings.jdbc.driver";
    private static final String JDBC_URL_KEY = "holdings.jdbc.conn.url";
    private static final String JDBC_USER_KEY = "holdings.jdbc.conn.user";
    private static final String JDBC_PASSWORD_KEY = "holdings.jdbc.conn.passwd";

    private static final String PROVIDER_NAME = "holdings-items-update";
    private static final String OCBTEST_WORKER_NAME = "ocb-test";
    private static final String[] WORKER_NAMES = {OCBTEST_WORKER_NAME, "solr-sync", "lokreg-sync"};

    /**
     * Creates an number of holdings for the record id in a record.
     * <p/>
     * This method commits/rollbacks on the connection.
     *
     * @param conn     DB connection.
     * @param record   The record to extract the record id from.
     * @param agencies List of agency ids to create holdings for.
     * @throws SQLException           Database errors.
     * @throws IOException            Thrown from HoldingsItemsDAO
     * @throws ClassNotFoundException Thrown from HoldingsItemsDAO
     * @throws HoldingsItemsException Thrown from HoldingsItemsDAO
     */
    static void saveHoldings(Connection conn, MarcRecord record, List<Integer> agencies) {
        logger.entry(conn, record, agencies);

        try {
            HoldingsItemsDAO dao = HoldingsItemsDAO.newInstance(conn);
            RecordId recordId = RawRepo.getRecordId(record);

            if (recordId != null) {
                for (Integer agencyId : agencies) {
                    RecordCollection collection = new RecordCollection(recordId.getBibliographicRecordId(), agencyId, "issue", "trackingId", dao);
                    collection.setComplete(false);
                    Record rec = collection.findRecord("fakeId");
                    rec.setStatus("OnOrder");
                    Date accdate = new Date();
                    rec.setAccessionDate(accdate);
                    collection.save();
                }

                conn.commit();
            }
        } catch (Throwable ex) {
            logger.error("saveHoldings ERROR : ", ex);
            try {
                conn.rollback();
            } catch (SQLException sqlex) {
                logger.error("WHAT !", sqlex);
            }
            throw new IllegalStateException("saveholdings error", ex);
        } finally {
            logger.exit();
        }
    }

    static Set<Integer> loadHoldingsForRecord(Connection conn, MarcRecord record) throws HoldingsItemsException {
        logger.entry();

        Set<Integer> result = null;
        try {
            HoldingsItemsDAO dao = HoldingsItemsDAO.newInstance(conn);
            RecordId recordId = RawRepo.getRecordId(record);

            logger.debug("searcher id {}:{} on collection", recordId.getBibliographicRecordId());
            if (recordId != null) {
                return result = dao.getAgenciesThatHasHoldingsFor(recordId.getBibliographicRecordId());
            }

            return new HashSet<>();
        } finally {
            logger.debug("found id {} on collection", result);
            logger.exit(result);
        }
    }

    static void setupDatabase(Properties settings) throws SQLException, IOException, ClassNotFoundException {
        logger.entry(settings);

        try (Connection conn = getConnection(settings)) {
            try {
                logger.debug("Setup holdings queue workers and rules");
                for (String name : WORKER_NAMES) {
                    logger.debug("Setup queue worker: {}", name);
                    JDBCUtil.update(conn, "INSERT INTO queueworkers(worker) VALUES(?)", name);
                    JDBCUtil.update(conn, "INSERT INTO queuerules(provider, worker) VALUES(?, ?)", PROVIDER_NAME, name);
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
        try (Connection conn = getConnection(settings)) {
            try {
                JDBCUtil.update(conn, "DELETE FROM agencylock");
                JDBCUtil.update(conn, "DELETE FROM holdingsitemsitem");
                JDBCUtil.update(conn, "DELETE FROM holdingsitemscollection");
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

    static Connection getConnection(Properties settings) throws ClassNotFoundException, SQLException, IOException {
        Class.forName(settings.getProperty(JDBC_DRIVER_KEY));

        String url = settings.getProperty(JDBC_URL_KEY);
        String user = settings.getProperty(JDBC_USER_KEY);
        String password = settings.getProperty(JDBC_PASSWORD_KEY);

        Connection conn = DriverManager.getConnection(url, user, password);
        conn.setAutoCommit(false);

        return conn;
    }

}
