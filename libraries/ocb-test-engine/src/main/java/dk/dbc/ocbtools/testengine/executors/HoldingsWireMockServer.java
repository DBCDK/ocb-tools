package dk.dbc.ocbtools.testengine.executors;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import dk.dbc.common.records.MarcRecord;
import dk.dbc.common.records.MarcRecordReader;

import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class HoldingsWireMockServer {
    private static final String HOLDINGS_PATH = "/api/agencies-with-holdings";
    protected WireMockServer wireMockServer;

    public HoldingsWireMockServer(Properties settings) {
        int port = Integer.parseInt(settings.getProperty("holdings.port"));
        makeWireMockServer(port);
    }

    public void addRecord(MarcRecord record, List<Integer> agencies) {
        String recordId = new MarcRecordReader(record).getRecordId();
        addMock(recordId, agencies);
    }

    public void addMock(String recordId, List<Integer> agencies) {
        String request = HOLDINGS_PATH + "/" + recordId;
        String agencyString = agencies.stream().map(Object::toString).collect(Collectors.joining(","));
        String body = "{\"agencies\":[" + agencyString + "],\"trackingId\":\"" + UUID.randomUUID() + "\"}";
        wireMockServer.stubFor(WireMock.get(request).willReturn(
                ResponseDefinitionBuilder.responseDefinition().withStatus(200).withHeader("content-type", "application/json").withBody(body))
        );
    }

    public void clearMocks() {
        wireMockServer.resetMappings();
    }

    private WireMockServer makeWireMockServer(int port) {
        WireMockServer wireMockServer = new WireMockServer(options().port(port));
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
        return wireMockServer;
    }
}
