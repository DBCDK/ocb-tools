//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.executors;

//-----------------------------------------------------------------------------
import dk.dbc.holdingsitems.HoldingsItemsException;
import dk.dbc.iscrum.records.MarcRecord;
import dk.dbc.iscrum.utils.json.Json;
import dk.dbc.iscrum.utils.logback.filters.BusinessLoggerFilter;
import dk.dbc.ocbtools.commons.filesystem.OCBFileSystem;
import dk.dbc.ocbtools.testengine.testcases.*;
import dk.dbc.rawrepo.RawRepoException;
import dk.dbc.rawrepo.Record;
import dk.dbc.rawrepo.RecordId;
import dk.dbc.updateservice.service.api.UpdateRecordRequest;
import dk.dbc.updateservice.service.api.UpdateRecordResponse;
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
import java.util.Set;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 03/04/15.
 */
public class DemoInfoPrinter {
    public DemoInfoPrinter() {
    }

    public void printHeader( Testcase tc, TestExecutor executor ) {
        output.info( "" );
        output.info( HEADER_LINE );
        output.info( "Testing {}", tc.getName() );
        output.info( SEP_LINE );
        output.info( "" );
        output.info( "Executor: {}", executor.name() );
        output.info( "Distribution: {}", tc.getDistributionName() );
        output.info( "File: {}", tc.getFile().getAbsolutePath() );
        output.info( "Description: {}", tc.getDescription() );
    }

    public void printFooter() {
        output.info( HEADER_LINE );
        output.info( "" );
    }

    public void printSetup( Testcase tc ) {
        try {
            output.info( SEP_LINE );
            output.info( "" );

            if( tc.getSetup() == null || tc.getSetup().getHoldings() == null || tc.getSetup().getHoldings().isEmpty() ) {
                output.info( "Holdings: Ingen opsætning" );
            }
            else {
                output.info( "Holdings: {}", tc.getSetup().getHoldings() );
            }

            if( tc.getSetup() == null || tc.getSetup().getSolr() == null || tc.getSetup().getSolr().isEmpty() ) {
                output.info( "Solr: Ingen opsætning" );
            }
            else {
                output.info( "Solr: \n{}", Json.encodePretty( tc.getSetup().getSolr() ) );
            }

            if( tc.getSetup() == null || tc.getSetup().getRawrepo() == null || tc.getSetup().getRawrepo().isEmpty() ) {
                output.info( "Rawrepo: Ingen opsætning" );
            }
            else {
                output.info( "Rawrepo:" );
                output.info( SEP_LINE );
                OCBFileSystem fs = new OCBFileSystem();
                File baseDir = tc.getFile().getParentFile();

                for( TestcaseRecord testRecord : tc.getSetup().getRawrepo() ) {
                    output.info( "File: {}", testRecord.getRecord() );
                    output.info( "Content:\n{}", fs.loadRecord( baseDir, testRecord.getRecord() ).toString() );
                }
            }

            output.info( SEP_LINE );
            output.info( "" );
        }
        catch( IOException ex ) {
            output.error( "Failed to print setup: {}", ex.getMessage() );
            logger.debug( "Stacktrace: ", ex );
        }
    }

    public void printLocaleRequest( Testcase tc ) {
        try {
            output.info( SEP_LINE );
            output.info( "Validating request against JavaScript" );
            output.info( SEP_LINE );
            output.info( "" );
            output.info( "Template name: {}", tc.getRequest().getTemplateName() );
            output.info( "Record: {}", tc.getRequest().getRecord() );
            output.info( "{}\n{}", SEP_LINE, tc.loadRecord().toString() );
        }
        catch( Exception ex ) {
            output.error( "Failed to print setup: {}", ex.getMessage() );
            logger.debug( "Stacktrace: ", ex );
        }
    }

    public void printRemoteDatabases( Testcase tc, Properties settings ) throws SQLException, ClassNotFoundException, HoldingsItemsException, RawRepoException {
        try {
            output.info( SEP_LINE );
            output.info( "" );

            try( Connection conn = Holdings.getConnection( settings ) ) {
                output.info( "Holdings: {}", Holdings.loadHoldingsForRecord( conn, tc.loadRecord() ) );
            }

            List<Record> rawRepoRecords = RawRepo.loadRecords( settings );
            if( rawRepoRecords == null || rawRepoRecords.isEmpty() ) {
                output.info( "Rawrepo: Empty" );
            }
            else {
                output.info( "Rawrepo:" );
                output.info( SEP_LINE );

                for( Record rawRepoRecord : rawRepoRecords ) {
                    output.info( "Id: [{}:{}]", rawRepoRecord.getId().getBibliographicRecordId(), rawRepoRecord.getId().getAgencyId() );
                    output.info( "Mimetype: {}", TestcaseRecordType.fromValue( rawRepoRecord.getMimeType() ) );
                    output.info( "Deleted: {}", rawRepoRecord.isDeleted() );
                    output.info( "TrackingID: {}", rawRepoRecord.getTrackingId() );
                    output.info( "" );
                    output.info( "Children: {}", formatRecordIds( RawRepo.loadRelations( settings, rawRepoRecord.getId(), RawRepoRelationType.CHILD ) ) );
                    output.info( "Siblings: {}", formatRecordIds( RawRepo.loadRelations( settings, rawRepoRecord.getId(), RawRepoRelationType.SIBLING ) ) );
                    output.info( "" );
                    output.info( "Content:\n{}", RawRepo.decodeRecord( rawRepoRecord.getContent() ) );
                }

                output.info( "Queued records: {}", formatRecordIds( RawRepo.loadQueuedRecords( settings ) ) );
            }

            output.info( SEP_LINE );
            output.info( "" );
        }
        catch( IOException ex ) {
            output.error( "Failed to print setup: {}", ex.getMessage() );
            logger.debug( "Stacktrace: ", ex );
        }
    }

    public void printRequest( UpdateRecordRequest request, MarcRecord record ) throws IOException {
        logger.entry();

        try {
            output.info( "Request record:\n{}", record );
            output.info( "Webservice Request: {}", Json.encodePretty( request ) );
        }
        finally {
            logger.exit();
        }
    }

    public void printResponse( UpdateRecordResult response ) throws IOException {
        logger.entry();

        try {
            output.info( "Response: {}", Json.encodePretty( response ) );
        }
        finally {
            logger.exit();
        }
    }

    public String formatRecordIds( Iterable<RecordId> ids ) {
        logger.entry();

        String result = "";
        try {
            StringBuilder sb = new StringBuilder();

            String sep = "";
            sb.append( '[' );
            for( RecordId recordId : ids ) {
                sb.append( sep );
                sb.append( '{' );
                sb.append( recordId.getBibliographicRecordId() );
                sb.append( ':' );
                sb.append( recordId.getAgencyId() );
                sb.append( '}' );

                sep = ", ";
            }
            sb.append( ']' );

            return result = sb.toString();
        }
        finally {
            logger.exit( result );
        }
    }

    private static String makeString( String ch, int length ) {
        String str = "";
        for( int i = 0; i < length; i++ ) {
            str += ch;
        }

        return str;
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger logger = XLoggerFactory.getXLogger( CheckTemplateExecutor.class );
    private static final XLogger output = XLoggerFactory.getXLogger( BusinessLoggerFilter.LOGGER_NAME );

    private static final int WIDTH = 72;
    private static final String HEADER_LINE = makeString( "#", WIDTH );
    private static final String SEP_LINE = makeString( "-", WIDTH );
}
