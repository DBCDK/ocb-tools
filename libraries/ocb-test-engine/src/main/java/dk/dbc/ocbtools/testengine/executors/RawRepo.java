//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.executors;

//-----------------------------------------------------------------------------
import dk.dbc.commons.jdbc.util.JDBCUtil;
import dk.dbc.iscrum.records.MarcReader;
import dk.dbc.iscrum.records.MarcRecord;
import dk.dbc.iscrum.records.MarcXchangeFactory;
import dk.dbc.iscrum.records.marcxchange.CollectionType;
import dk.dbc.iscrum.records.marcxchange.ObjectFactory;
import dk.dbc.iscrum.records.marcxchange.RecordType;
import dk.dbc.ocbtools.commons.filesystem.OCBFileSystem;
import dk.dbc.ocbtools.testengine.testcases.TestcaseRecord;
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
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 20/03/15.
 */
public class RawRepo {
    public RawRepo( Connection connection ) throws RawRepoException {
        this.dao = RawRepoDAO.newInstance( connection );
    }

    public void saveRecords( File baseDir, List<TestcaseRecord> records ) throws IOException, RawRepoException, JAXBException {
        logger.entry();

        try {
            for( TestcaseRecord record : records ) {
                saveRecord( baseDir, record );
            }
        }
        finally {
            logger.exit();
        }
    }

    public void saveRecord( File baseDir, TestcaseRecord record ) throws IOException, RawRepoException, JAXBException {
        logger.entry();

        try {
            OCBFileSystem fs = new OCBFileSystem();
            MarcRecord marcRecord = fs.loadRecord( baseDir, record.getRecord() );
            RecordId recId = getRecordId( marcRecord );

            Record newRecord = dao.fetchRecord( recId.getBibliographicRecordId(), recId.getAgencyId() );
            newRecord.setDeleted( record.isDeleted() );
            newRecord.setMimeType( record.getType().value() );
            newRecord.setContent( encodeRecord( marcRecord ) );
            dao.saveRecord( newRecord );
            dao.changedRecord( "opencataloging-update", newRecord.getId(), newRecord.getMimeType() );
        }
        finally {
            logger.exit();
        }
    }

    /**
     * Encodes the record as marcxchange.
     *
     * @param record The record to encode.
     *
     * @return The encoded record as a sequence of bytes.
     *
     * @throws javax.xml.bind.JAXBException if the record can not be encoded in marcxchange.
     * @throws java.io.UnsupportedEncodingException if the record can not be encoded in UTF-8
     */
    private byte[] encodeRecord( MarcRecord record ) throws JAXBException, UnsupportedEncodingException {
        logger.entry( record );
        byte[] result = null;

        try {

            if( record.getFields().isEmpty() ) {
                return null;
            }

            dk.dbc.iscrum.records.marcxchange.RecordType marcXchangeType = MarcXchangeFactory.createMarcXchangeFromMarc( record );

            ObjectFactory objectFactory = new ObjectFactory();
            JAXBElement<RecordType> jAXBElement = objectFactory.createRecord( marcXchangeType );

            JAXBContext jc = JAXBContext.newInstance( CollectionType.class );
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty( Marshaller.JAXB_SCHEMA_LOCATION, "http://www.loc.gov/standards/iso25577/marcxchange-1-1.xsd" );

            StringWriter recData = new StringWriter();
            marshaller.marshal( jAXBElement, recData );

            logger.info( "Marshelled record: {}", recData.toString() );
            result = recData.toString().getBytes( "UTF-8" );

            return result;
        }
        finally {
            logger.exit( result );
        }
    }

    private RecordId getRecordId( MarcRecord record ) {
        logger.entry();

        try {
            String recId = MarcReader.getRecordValue( record, "001", "a" );
            int agencyId = Integer.valueOf( MarcReader.getRecordValue( record, "001", "b" ), 10 );

            return new RecordId( recId, agencyId );
        }
        finally {
            logger.exit();
        }
    }

    public static Connection getConnection( Properties settings ) throws ClassNotFoundException, SQLException, IOException {
        Class.forName( settings.getProperty( JDBC_DRIVER_KEY ) );

        String url = settings.getProperty( JDBC_URL_KEY );
        String user = settings.getProperty( JDBC_USER_KEY );
        String password = settings.getProperty( JDBC_PASSWORD_KEY );

        Connection conn = DriverManager.getConnection( url, user, password );
        conn.setAutoCommit( false );

        return conn;
    }

    public static void setupDatabase( Properties settings ) throws SQLException, IOException, ClassNotFoundException {
        logger.entry( settings );

        try( Connection conn = getConnection( settings ) ) {
            try {
                JDBCUtil.update( conn, "INSERT INTO queueworkers(worker) VALUES(?)", "fbs-sync" );
                JDBCUtil.update( conn, "INSERT INTO queueworkers(worker) VALUES(?)", "solr-sync" );
                JDBCUtil.update( conn, "INSERT INTO queueworkers(worker) VALUES(?)", "broend-sync" );
                JDBCUtil.update( conn, "INSERT INTO queueworkers(worker) VALUES(?)", "basis-decentral" );

                JDBCUtil.update( conn, "INSERT INTO queuerules(provider, worker, changed, leaf) VALUES(?, ?, ?, ?)", "opencataloging-update", "fbs-sync", "Y", "A" );
                JDBCUtil.update( conn, "INSERT INTO queuerules(provider, worker, changed, leaf) VALUES(?, ?, ?, ?)", "opencataloging-update", "solr-sync", "Y", "A" );
                JDBCUtil.update( conn, "INSERT INTO queuerules(provider, worker, changed, leaf) VALUES(?, ?, ?, ?)", "opencataloging-update", "broend-sync", "Y", "A" );
                JDBCUtil.update( conn, "INSERT INTO queuerules(provider, worker, mimetype, changed, leaf) VALUES(?, ?, ?, ?, ?)", "opencataloging-update", "basis-decentral", "text/decentral+marcxchange", "Y", "A" );

                conn.commit();
            }
            catch( SQLException ex ) {
                conn.rollback();
                logger.error( ex.getMessage(), ex );

                throw ex;
            }
        }
        finally {
            logger.exit();
        }
    }

    public static void teardownDatabase( Properties settings ) throws SQLException, IOException, ClassNotFoundException {
        logger.entry( settings );

        try( Connection conn = getConnection( settings ) ) {
            try {
                JDBCUtil.update( conn, "DELETE FROM relations" );
                JDBCUtil.update( conn, "DELETE FROM records" );
                JDBCUtil.update( conn, "DELETE FROM queue" );

                JDBCUtil.update( conn, "DELETE FROM queuerules" );
                JDBCUtil.update( conn, "DELETE FROM queueworkers" );

                conn.commit();
            }
            catch( SQLException ex ) {
                conn.rollback();
                logger.error( ex.getMessage(), ex );

                throw ex;
            }
        }
        finally {
            logger.exit();
        }

    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger logger = XLoggerFactory.getXLogger( RawRepo.class );

    private static final String JDBC_DRIVER_KEY = "rawrepo.jdbc.driver";
    private static final String JDBC_URL_KEY = "rawrepo.jdbc.conn.url";
    private static final String JDBC_USER_KEY = "rawrepo.jdbc.conn.user";
    private static final String JDBC_PASSWORD_KEY = "rawrepo.jdbc.conn.passwd";

    private RawRepoDAO dao;
}
