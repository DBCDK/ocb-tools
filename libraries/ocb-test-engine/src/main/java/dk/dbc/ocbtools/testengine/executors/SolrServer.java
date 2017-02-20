package dk.dbc.ocbtools.testengine.executors;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcase;
import org.perf4j.StopWatch;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.util.Properties;

import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Class to access Solr.
 */
class SolrServer {
    private static final XLogger logger = XLoggerFactory.getXLogger(SolrServer.class);

    private static final String SOLR_PORT_KEY = "solr.port";
    private static final String SELECT_REQUEST_MASK = "([^?]*)select(.*)";
    private static final String SELECT_RESPONSE = "{\"response\":{\"numFound\":0,\"start\":0,\"docs\":[]}}";
    private static final String ANALYSIS_REQUEST_MASK = "([^?]*)analysis(.*)";
    private static final String ANALYSIS_RESPONSE = "{\"responseHeader\":{" +
            "\"status\":0}," +
            "\"analysis\":{" +
            "\"field_names\":{" +
            "\"match.004a\":{\"index\":[[{\"text\":\"something\"}]]}," +
            "\"match.008a\":{\"index\":[[{\"text\":\"something\"}]]}," +
            "\"match.009a\":{\"index\":[[{\"text\":\"something\"}]]}," +
            "\"match.009g\":{\"index\":[[{\"text\":\"something\"}]]}," +
            "\"match.014a\":{\"index\":[[{\"text\":\"something\"}]]}," +
            "\"match.021a\":{\"index\":[[{\"text\":\"something\"}]]}," +
            "\"match.021e\":{\"index\":[[{\"text\":\"something\"}]]}," +
            "\"match.022a\":{\"index\":[[{\"text\":\"something\"}]]}," +
            "\"match.023ab\":{\"index\":[[{\"text\":\"something\"}]]}," +
            "\"match.024a\":{\"index\":[[{\"text\":\"something\"}]]}," +
            "\"match.028a\":{\"index\":[[{\"text\":\"something\"}]]}," +
            "\"match.100a\":{\"index\":[[{\"text\":\"something\"}]]}," +
            "\"match.110a\":{\"index\":[[{\"text\":\"something\"}]]}," +
            "\"match.245a\":{\"index\":[[{\"text\":\"something\"}]]}," +
            "\"match.245g\":{\"index\":[[{\"text\":\"something\"}]]}," +
            "\"match.245n\":{\"index\":[[{\"text\":\"something\"}]]}," +
            "\"match.245ø\":{\"index\":[[{\"text\":\"something\"}]]}," +
            "\"match.250a\":{\"index\":[[{\"text\":\"something\"}]]}," +
            "\"match.260b\":{\"index\":[[{\"text\":\"something\"}]]}," +
            "\"match.300e\":{\"index\":[[{\"text\":\"something\"}]]}," +
            "\"match.538g\":{\"index\":[[{\"text\":\"something\"}]]}" +
            "}}}";

    private WireMockServer solrServer;

    SolrServer(UpdateTestcase utc, Properties settings) {
        logger.entry();

        try {
            this.solrServer = null;

            Integer port = Integer.valueOf(settings.getProperty(SOLR_PORT_KEY), 10);
            if (utc.hasSolrMocking()) {
                StopWatch watch = new StopWatch();

                String rootDir = utc.getSolrRootDirectory().getAbsolutePath();

                logger.debug("Starting WireMock on port {} with root directory: {}", port, rootDir);
                WireMockConfiguration wireMockConfiguration = wireMockConfig().port(port).withRootDirectory(rootDir);

                solrServer = new WireMockServer(wireMockConfiguration);
                solrServer.start();

                logger.info("Starting WireMock Solr server in {} ms", watch.getElapsedTime());
            } else {
                logger.debug("Starting fake wiremock for solr");
                MappingBuilder mbAnalyse = new MappingBuilder(RequestMethod.ANY, urlMatching(ANALYSIS_REQUEST_MASK));
                MappingBuilder mbSelect = new MappingBuilder(RequestMethod.ANY, urlMatching(SELECT_REQUEST_MASK));
                solrServer = new WireMockServer(wireMockConfig().port(port));
                solrServer.stubFor(mbSelect.willReturn(new ResponseDefinitionBuilder().withStatus(200).withBody(SELECT_RESPONSE)));
                solrServer.stubFor(mbAnalyse.willReturn(new ResponseDefinitionBuilder().withStatus(200).withBody(ANALYSIS_RESPONSE)));
                solrServer.start();
                logger.debug("Stub settings {}", solrServer.listAllStubMappings().getMappings());
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
