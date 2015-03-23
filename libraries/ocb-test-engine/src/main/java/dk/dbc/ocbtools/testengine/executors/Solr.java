//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.executors;

//-----------------------------------------------------------------------------

import dk.dbc.iscrum.utils.logback.filters.BusinessLoggerFilter;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Class to access Solr.
 */
public class Solr {
    public static void waitForIndex( Properties settings ) {
        logger.entry();

        try {
            int milliSecconds = Integer.valueOf( settings.getProperty( "solr.delay" ), 10 );
            logger.debug( "Wait for {} second(s), to give solr a chance to get up-to-date with the rawrepo records.", milliSecconds / 1000 );
            Thread.sleep( milliSecconds );
        }
        catch( InterruptedException ex ) {
            Thread.currentThread().interrupt();

            output.error( "Unable to wait for solr index: {}", ex.getMessage() );
            logger.debug( "Stacktrace:", ex );

            throw new RuntimeException( ex );
        }
        finally {
            logger.exit();
        }
    }

    public static void clearIndex( Properties settings ) {
        logger.entry( settings );

        String url = "";
        try {
            url = settings.getProperty( "solr.url" );
            SolrServer solr = new CommonsHttpSolrServer( url );
            solr.deleteByQuery("*:*");
        }
        catch( IOException | SolrServerException ex ) {
            output.error( "Unable to clear the solr index at {}: {}", url, ex.getMessage() );
            logger.debug( "Stacktrace:", ex );

            throw new RuntimeException( ex );
        }
        finally {
            logger.exit();
        }

    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger output = XLoggerFactory.getXLogger( BusinessLoggerFilter.LOGGER_NAME );
    private static final XLogger logger = XLoggerFactory.getXLogger( RemoteValidateExecutor.class );
}
