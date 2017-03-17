//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.ocbwait;

//-----------------------------------------------------------------------------
import dk.dbc.ocbtools.commons.api.SubcommandExecutor;
import dk.dbc.ocbtools.commons.cli.CliException;
import org.perf4j.StopWatch;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class WSExecutor implements SubcommandExecutor {

    private static final XLogger logger = XLoggerFactory.getXLogger( WSExecutor.class );

    private URL url;
    private String text;

    private Integer timeout;
    public URL getUrl() {
        return url;
    }

    public void setUrl( URL url ) {
        this.url = url;
    }

    public String getText() {
        return text;
    }

    public void setText( String text ) {
        this.text = text;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout( Integer timeout ) {
        this.timeout = timeout;
    }

    /**
     * Performes the actions for the subcommand.
     */
    @Override
    public void actionPerformed() throws CliException {
        logger.entry();

        try {
            logger.info( "URL: {}", url );
            logger.info( "Success text: {}", text );
            logger.info( "Timeout: {} ms", timeout );
            logger.info( "" );

            StopWatch watchTimeout = new StopWatch();
            while( watchTimeout.getElapsedTime() < timeout ) {
                WebServiceResponse webServiceResponse;
                try {
                    webServiceResponse = callService();
                }
                catch( IOException ex ) {
                    logger.info( "Web service error: {}", ex.getMessage() );
                    Thread.sleep( 5000 );
                    continue;
                }

                if( webServiceResponse == null ) {
                    Thread.sleep( 5000 );
                    continue;
                }

                if( webServiceResponse.getResponseCode() != 200 ) {
                    Thread.sleep( 5000 );
                    logger.info( "Web service response code: {}", webServiceResponse.getResponseCode() );
                    continue;
                }

                if( webServiceResponse.getResponse().equals( text ) ) {
                    logger.info( "Web service returned acceptance text: {}", webServiceResponse.getResponse() );
                    return;
                }

                Thread.sleep( 500 );
                logger.info( "Web service returned non-acceptance text: {}", webServiceResponse.getResponse() );
            }

            throw new CliException( "Timeout has reached for web service: " + url.toString() );
        }
        catch( InterruptedException ex ) {
            throw new CliException( ex.getMessage(), ex );
        }
        finally {
            logger.exit();
        }
    }

    /**
     * Calls the web service specified by <code>url</code> and returns its
     * response as a string.
     *
     * @return The response from the service. The empty string is returned in case of
     *         an error.
     */
    private WebServiceResponse callService() throws IOException {
        logger.entry();

        WebServiceResponse result = null;
        try {
            int responseCode;
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "text/plain" );

            InputStream is;
            responseCode = conn.getResponseCode();

            if( responseCode == 200 ) {
                is = conn.getInputStream();
            }
            else {
                is = conn.getErrorStream();
            }

            result = new WebServiceResponse( responseCode, is );
            conn.disconnect();

            logger.debug( "Web service response {} ==> {}", url.toString(), result );

            return result;
        }
        finally {
            logger.exit( result );
        }
    }

}
