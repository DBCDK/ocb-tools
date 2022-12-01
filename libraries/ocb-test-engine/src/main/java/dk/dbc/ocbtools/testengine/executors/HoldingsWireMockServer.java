package dk.dbc.ocbtools.testengine.executors;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import dk.dbc.common.records.MarcRecord;
import dk.dbc.common.records.MarcRecordReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class HoldingsWireMockServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(HoldingsWireMockServer.class);
    private static final String HOLDINGS_PATH = "/api/agencies-with-holdings";
    private WireMockServer wireMockServer;

    public HoldingsWireMockServer(Properties settings) {
        int port = Integer.parseInt(settings.getProperty("holdings.port"));
        wireMockServer = makeWireMockServer(port);
    }

    public void addRecord(MarcRecord record, List<Integer> agencies) {
        String recordId = new MarcRecordReader(record).getRecordId();
        addMock(recordId, agencies);
    }

    public void addMock(String recordId, List<Integer> agencies) {
        String request = HOLDINGS_PATH + "/" + recordId;
        String agencyString = agencies == null ? "" : agencies.stream().map(Object::toString).collect(Collectors.joining(","));
        String body = "{\"agencies\":[" + agencyString + "],\"trackingId\":\"" + UUID.randomUUID() + "\"}";
        wireMockServer.stubFor(WireMock.get(request).willReturn(
                ResponseDefinitionBuilder.responseDefinition().withStatus(200).withHeader("content-type", "application/json").withBody(body))
        );
    }

    public void clearMocks() {
        if(wireMockServer != null) {
            wireMockServer.resetMappings();
            addEmptyStub(wireMockServer);
        }
    }

    private static void addEmptyStub(WireMockServer wireMockServer) {
        String emptyResponse = "{\"agencies\":[],\"trackingId\":\"" + UUID.randomUUID() + "\"}";
        wireMockServer.stubFor(WireMock.get(urlPathMatching(HOLDINGS_PATH + "/.*")).willReturn(
                ResponseDefinitionBuilder.responseDefinition()
                        .withStatus(200)
                        .withHeader("content-type", "application/json")
                        .withBody(emptyResponse)).atPriority(Integer.MAX_VALUE));
    }

    private static WireMockServer makeWireMockServer(int port) {
        LOGGER.info("Configuring holdings wiremock server on port " + port);
        WireMockServer server = new WireMockServer(options().port(port));
        server.start();
        configureFor("localhost", server.port());
        return server;
    }
}
