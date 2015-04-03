//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.executors;

//-----------------------------------------------------------------------------
import dk.dbc.iscrum.utils.json.Json;
import dk.dbc.iscrum.utils.logback.filters.BusinessLoggerFilter;
import dk.dbc.ocbtools.commons.filesystem.OCBFileSystem;
import dk.dbc.ocbtools.testengine.testcases.Testcase;
import dk.dbc.ocbtools.testengine.testcases.TestcaseRecord;
import dk.dbc.ocbtools.testengine.testcases.TestcaseRequest;
import dk.dbc.ocbtools.testengine.testcases.TestcaseSetup;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.IOException;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 03/04/15.
 */
public class DemoInfoPrinter {
    public DemoInfoPrinter() {
    }

    public void printHeader( Testcase tc ) {
        output.info( "" );
        output.info( HEADER_LINE );
        output.info( "Testing {}", tc.getName() );
        output.info( SEP_LINE );
        output.info( "" );
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

            if( tc.getSetup().getHoldings() == null || tc.getSetup().getHoldings().isEmpty() ) {
                output.info( "Holdings: Ingen opsætning" );
            }
            else {
                output.info( "Holdings: {}", tc.getSetup().getHoldings() );
            }

            if( tc.getSetup().getSolr() == null || tc.getSetup().getSolr().isEmpty() ) {
                output.info( "Solr: Ingen opsætning" );
            }
            else {
                output.info( "Solr: \n{}", Json.encodePretty( tc.getSetup().getSolr() ) );
            }

            if( tc.getSetup().getRawrepo() == null || tc.getSetup().getRawrepo().isEmpty() ) {
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
