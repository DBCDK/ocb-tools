package dk.dbc.ocbtools.testengine.executors;

import dk.dbc.common.records.MarcRecord;
import dk.dbc.commons.jdbc.util.JDBCUtil;
import dk.dbc.holdingsitems.DatabaseMigrator;
import dk.dbc.holdingsitems.HoldingsItemsDAO;
import dk.dbc.holdingsitems.HoldingsItemsException;
import dk.dbc.holdingsitems.Record;
import dk.dbc.holdingsitems.RecordCollection;
import dk.dbc.rawrepo.RecordId;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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

            logger.debug("searcher id {}:{} on collection", recordId != null ? recordId.getBibliographicRecordId() : "null");
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

    static void teardownDatabase(Properties settings) throws SQLException, ClassNotFoundException {
        logger.entry(settings);
        try (Connection conn = getConnection(settings)) {
            DatabaseMigrator.migrate(getDataSource(settings));

            try {
                JDBCUtil.update(conn, "DELETE FROM holdingsitemsitem");
                JDBCUtil.update(conn, "DELETE FROM holdingsitemscollection");
                JDBCUtil.update(conn, "DELETE FROM queue");
                JDBCUtil.update(conn, "DELETE FROM queue_error");

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

    static DataSource getDataSource(Properties properties) throws SQLException {
        final PGSimpleDataSource dataSource = new PGSimpleDataSource();

        String url = properties.getProperty(JDBC_URL_KEY);
        String user = properties.getProperty(JDBC_USER_KEY);
        String password = properties.getProperty(JDBC_PASSWORD_KEY);

        dataSource.setUrl(url);
        dataSource.setUser(user);
        dataSource.setPassword(password);

        return dataSource;
    }

    static Connection getConnection(Properties properties) throws ClassNotFoundException, SQLException {
        Class.forName(properties.getProperty(JDBC_DRIVER_KEY));

        String url = properties.getProperty(JDBC_URL_KEY);
        String user = properties.getProperty(JDBC_USER_KEY);
        String password = properties.getProperty(JDBC_PASSWORD_KEY);

        Connection conn = DriverManager.getConnection(url, user, password);
        conn.setAutoCommit(false);

        return conn;
    }

}
