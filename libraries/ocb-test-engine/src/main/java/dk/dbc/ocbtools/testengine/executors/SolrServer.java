package dk.dbc.ocbtools.testengine.executors;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcase;
import org.perf4j.StopWatch;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.util.Properties;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Class to access Solr.
 */
class SolrServer {
    private static final XLogger logger = XLoggerFactory.getXLogger(SolrServer.class);

    private static final String SOLR_PORT_KEY = "solr.port";

    private WireMockServer solrServer;

    SolrServer(UpdateTestcase utc, Properties settings) {
        logger.entry();

        try {
            this.solrServer = null;

            if (utc.hasSolrMocking()) {
                StopWatch watch = new StopWatch();

                Integer port = Integer.valueOf(settings.getProperty(SOLR_PORT_KEY), 10);
                String rootDir = utc.getSolrRootDirectory().getAbsolutePath();

                logger.debug("Starting WireMock on port {} with root directory: {}", port, rootDir);
                WireMockConfiguration wireMockConfiguration = wireMockConfig().port(port).withRootDirectory(rootDir);

                solrServer = new WireMockServer(wireMockConfiguration);
                solrServer.start();

                logger.info("Starting WireMock Solr server in {} ms", watch.getElapsedTime());
            }
        } catch (Throwable ex) {
            logger.error("solrServer mocking ERROR : ", ex);
            throw new IllegalStateException("solrServer mocking error", ex);
        } finally {
            logger.exit();
        }
    }

    void stop() {
        logger.entry();

        try {
            if (solrServer != null) {
                StopWatch watch = new StopWatch();
                solrServer.stop();
                logger.info("Stopping WireMock Solr server in {} ms", watch.getElapsedTime());
            }
        } finally {
            logger.exit();
        }
    }
}
