package dk.dbc.ocbtools.ocbrecord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dbc.buildservice.client.BuildService;
import dk.dbc.buildservice.service.api.BibliographicRecord;
import dk.dbc.buildservice.service.api.BuildPortType;
import dk.dbc.buildservice.service.api.BuildRequest;
import dk.dbc.buildservice.service.api.BuildResult;
import dk.dbc.buildservice.service.api.ExtraRecordData;
import dk.dbc.buildservice.service.api.RecordData;
import dk.dbc.iscrum.records.MarcConverter;
import dk.dbc.iscrum.records.MarcRecord;
import dk.dbc.iscrum.records.MarcXchangeFactory;
import dk.dbc.iscrum.records.marcxchange.ObjectFactory;
import dk.dbc.iscrum.records.marcxchange.RecordType;
import dk.dbc.iscrum.utils.json.Json;
import dk.dbc.ocbtools.commons.api.SubcommandExecutor;
import dk.dbc.ocbtools.commons.cli.CliException;
import dk.dbc.ocbtools.ocbrecord.json.MixIns;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;

class NewExecutor implements SubcommandExecutor {

    private static final XLogger logger = XLoggerFactory.getXLogger(NewExecutor.class);

    private OCBRecordData ocbRecordData;

    NewExecutor(OCBRecordData ocbRecordData) {
        logger.entry();
        this.ocbRecordData = ocbRecordData;
        logger.exit();
    }

    @Override
    // Main command entrypoint
    public void actionPerformed() throws CliException {
        logger.entry();
        try {
            ocbRecordData = parseFormatFromInput(ocbRecordData);
            ocbRecordData = readPropertiesFile(ocbRecordData);
            ocbRecordData = getFaustNumberFromOpenNumberRoll(ocbRecordData);
            validateProgramParameters(ocbRecordData);
            printProgramInfo(ocbRecordData);
            ocbRecordData = readInputFileIntoData(ocbRecordData);

            String resultOfRemoteJavascriptCall = callWebService(ocbRecordData);
            String generatedOutput = generateOutput(ocbRecordData, resultOfRemoteJavascriptCall);
            printGeneratedOutput(ocbRecordData, generatedOutput);
        } finally {
            logger.exit();
        }
    }

    // Call the build webservice and return the json result as a string
    private String callWebService(OCBRecordData ocbRecordData) throws CliException {
        logger.entry(ocbRecordData);
        String res = null;
        try {
            String webServiceUrl = ocbRecordData.getBuildWsUrl();
            URL url = new URL(webServiceUrl);
            BuildService buildService = new BuildService(url);
            BuildPortType buildPortType = buildService.createPort();

            BuildRequest buildRequest = createBuildRequest(ocbRecordData);
            BuildResult buildResult = buildPortType.build(buildRequest);
            res = convertBuildResultToJson(buildResult);
            return res;
        } catch (MalformedURLException e) {
            logger.catching(e);
            logger.info("Programmet fejlede med denne besked:");
            logger.info(e.getMessage());
            throw new CliException(e.getMessage(), e);
        } finally {
            logger.exit(res);
        }
    }

    // Convert the buildresult to a json string
    private String convertBuildResultToJson(BuildResult buildResult) throws CliException {
        logger.entry(buildResult);
        String res = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            addJacksonMixInAnnotations(objectMapper);
            MarcRecord record = null;
            if (buildResult != null && buildResult.getBibliographicRecord() != null && buildResult.getBibliographicRecord().getRecordData() != null) {
                List<Object> list = buildResult.getBibliographicRecord().getRecordData().getContent();
                for (Object o : list) {
                    if (o instanceof Node) {
                        record = MarcConverter.createFromMarcXChange(new DOMSource((Node) o));
                        res = objectMapper.writeValueAsString(record);
                        break;
                    }
                }
            }
            return res;
        } catch (JsonProcessingException e) {
            throw new CliException(e.getMessage(), e);
        } finally {
            logger.exit(res);
        }
    }

    // Create buildservice request from ocbRecordData object
    private BuildRequest createBuildRequest(OCBRecordData ocbRecordData) throws CliException {
        logger.entry(ocbRecordData);
        BuildRequest buildRequest = null;
        try {
            buildRequest = new BuildRequest();
            buildRequest.setSchemaName(ocbRecordData.getTemplate());
            buildRequest.setTrackingId(ocbRecordData.getUuid());
            BibliographicRecord bibliographicRecord = new BibliographicRecord();
            bibliographicRecord.setRecordPacking(OCBRecordStatics.RECORD_PACKING);
            bibliographicRecord.setRecordSchema(OCBRecordStatics.RECORD_SCHEMA);
            RecordData recordData = new RecordData();
            bibliographicRecord.setRecordData(recordData);
            ExtraRecordData extraRecordData = new ExtraRecordData();
            bibliographicRecord.setExtraRecordData(extraRecordData);
            buildRequest.setBibliographicRecord(bibliographicRecord);
            return buildRequest;
        } finally {
            logger.exit(buildRequest);
        }
    }

    // Write the formatted buildservice result string to a file
    private void writeResultToFile(OCBRecordData ocbRecordData, String result) throws CliException {
        logger.entry(ocbRecordData, result);
        try {
            File file = new File(ocbRecordData.getOutputFile());
            if (!file.exists()) {
                Path path = Paths.get(ocbRecordData.getOutputFile());
                Files.write(path, result.getBytes(Charset.forName("UTF-8")));
            } else {
                throw new CliException("Fejl, logfilen findes allerede.");
            }
        } catch (IOException ex) {
            throw new CliException(ex.getMessage(), ex);
        } finally {
            logger.exit();
        }
    }

    // Generates output in the request format (JSON, MARC, MarcXchange) and returns it as a string
    private String generateOutput(OCBRecordData ocbRecordData, String result) throws CliException {
        logger.entry(ocbRecordData, result);
        String res = null;
        try {
            switch (ocbRecordData.getFormatType()) {
                case JSON:
                    res = result;
                    break;
                case MARCXCHANGE:
                    res = decodeJsonToMarcXchange(result);
                    break;
                case UNKNOWN:
                case MARC:
                default:
                    res = decodeJsonToMarc(result);
                    break;
            }
            return res;
        } finally {
            logger.exit(res);
        }
    }

    // Convert a json string to a MARC formatted string
    private String decodeJsonToMarc(String result) throws CliException {
        logger.entry(result);
        String res = null;
        try {
            MarcRecord marcRecord = Json.decode(result, MarcRecord.class);
            res = marcRecord.toString();
            return res;
        } catch (IOException ex) {
            throw new CliException(ex.getMessage(), ex);
        } finally {
            logger.exit(res);
        }
    }

    // Convert a json string to a MarcXchange formatted string
    private String decodeJsonToMarcXchange(String result) throws CliException {
        logger.entry(result);
        String res = null;
        try {
            String MARCXCHANGE_1_1_SCHEMA_LOCATION = "http://www.loc.gov/standards/iso25577/marcxchange-1-1.xsd";

            MarcRecord marcRecord = Json.decode(result, MarcRecord.class);
            RecordType marcXhangeType = MarcXchangeFactory.createMarcXchangeFromMarc(marcRecord);
            ObjectFactory objectFactory = new ObjectFactory();
            JAXBElement<RecordType> jAXBElement = objectFactory.createRecord(marcXhangeType);

            JAXBContext jaxbContext = JAXBContext.newInstance(RecordType.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, MARCXCHANGE_1_1_SCHEMA_LOCATION);

            DocumentBuilderFactory dbf;
            dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            marshaller.marshal(jAXBElement, document);

            // Code below has shamelessly been taken from these answers:
            // http://stackoverflow.com/a/10356325
            // http://stackoverflow.com/a/139096
            DOMSource domSource = new DOMSource(document);
            StringWriter stringWriter = new StringWriter();
            StreamResult streamResult = new StreamResult(stringWriter);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(domSource, streamResult);
            res = stringWriter.toString();
            return res;
        } catch (ParserConfigurationException | JAXBException | IOException | TransformerException e) {
            throw new CliException(e.getMessage(), e);
        } finally {
            logger.exit(res);
        }
    }

    // Output the formatted result to either a file or screen
    private void printGeneratedOutput(OCBRecordData ocbRecordData, String result) throws CliException {
        logger.entry(ocbRecordData, result);
        try {
            if (ocbRecordData.getOutputFile() != null) {
                writeResultToFile(ocbRecordData, result);
            } else {
                logger.info("\n-------------- Output start --------------\n" + result + "\n-------------- Output slut ---------------");
            }
        } finally {
            logger.exit();
        }
    }

    // Read the properties file and populate internal data object
    private OCBRecordData readPropertiesFile(OCBRecordData ocbRecordData) throws CliException {
        logger.entry(ocbRecordData);
        try {
            InputStream inputStream = new FileInputStream("ocb-record.settings");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF8");
            Properties properties = new Properties();
            properties.load(inputStreamReader);
            String webserviceUrl;
            if (ocbRecordData.getDistribution().equalsIgnoreCase("fbs")) {
                if (!properties.containsKey(OCBRecordStatics.PROP_BUILD_FBS_URL)) {
                    throw new CliException("Property " + OCBRecordStatics.PROP_BUILD_FBS_URL + " ikke fundet i settings fil.");
                }
                webserviceUrl = properties.getProperty(OCBRecordStatics.PROP_BUILD_FBS_URL);
            } else if (ocbRecordData.getDistribution().equalsIgnoreCase("dataio")) {
                if (!properties.containsKey(OCBRecordStatics.PROP_BUILD_DATAIO_URL)) {
                    throw new CliException("Property " + OCBRecordStatics.PROP_BUILD_DATAIO_URL + " ikke fundet i settings fil.");
                }
                webserviceUrl = properties.getProperty(OCBRecordStatics.PROP_BUILD_DATAIO_URL);
            } else {
                // This shouldn't happen, but probably did anyway, bugger
                throw new CliException("Property " + OCBRecordStatics.PROP_BUILD_DATAIO_URL + " ikke fundet i settings fil.");
            }
            ocbRecordData.setBuildWsUrl(webserviceUrl);

            if (!properties.containsKey(OCBRecordStatics.PROP_OPENNUMBERROLL_URL)) {
                throw new CliException("Property " + OCBRecordStatics.PROP_OPENNUMBERROLL_URL + " ikke fundet i settings fil.");
            }
            String openNumberRollUrl = properties.getProperty(OCBRecordStatics.PROP_OPENNUMBERROLL_URL);
            ocbRecordData.setOpenNumberRollUrl(openNumberRollUrl);
            return ocbRecordData;
        } catch (FileNotFoundException ex) {
            throw new CliException("ocb-record.settings fil ikke fundet.", ex);
        } catch (IOException ex) {
            throw new CliException(ex.getMessage(), ex);
        } finally {
            logger.exit(ocbRecordData);
        }
    }

    // Get faust number from opennumberroll
    private OCBRecordData getFaustNumberFromOpenNumberRoll(OCBRecordData ocbRecordData) throws CliException {
        logger.entry(ocbRecordData);
        OCBRecordData res = ocbRecordData;
        try {
            String openNumberRollUrl = ocbRecordData.getOpenNumberRollUrl();
            Client client = ClientBuilder.newClient();
            WebTarget webTarget = client.target(openNumberRollUrl);
            Response response = webTarget.request().get();
            if (response.getStatus() != 200) {
                throw new CliException("Fik statuskode " + response.getStatus() + " fra OpenNumberRoll");
            }
            String openNumberRollRsponse = response.readEntity(String.class);
            String faustNumber = getFaustNumberFromOpenNumberRollReponse(openNumberRollRsponse);
            res.setFaustNumber(faustNumber);
            return res;
        } finally {
            logger.exit(res);
        }
    }

    // Extract faustnumber from opennumberroll xml response
    private String getFaustNumberFromOpenNumberRollReponse(String openNumberRollResponse) throws CliException {
        logger.entry(openNumberRollResponse);
        String res = null;
        try {
            String tag = "rollNumber>";
            int startIdx = openNumberRollResponse.indexOf(tag);
            startIdx += tag.length();
            if (startIdx < 1) {
                throw new CliException("Kunne ikke finde rollNumber tag i svaret fra OpenNumberRoll");
            }

            int endIdx = openNumberRollResponse.indexOf("<", startIdx);
            res = openNumberRollResponse.substring(startIdx, endIdx);
            return res;
        } finally {
            logger.exit(res);
        }
    }

    // Output program running parameters
    private void printProgramInfo(OCBRecordData ocbRecordData) {
        logger.entry(ocbRecordData);
        try {
            logger.info("ocb-record parametre:");
            logger.info("Distribution.....: " + ocbRecordData.getDistribution());
            if (ocbRecordData.getOutputFile() != null) {
                logger.info("Output filnavn...: " + ocbRecordData.getOutputFile());
            } else {
                logger.info("Output filnavn...: ingen (direkte til skærm)");
            }
            if (ocbRecordData.getInputFile() != null) {
                logger.info("Input filnavn....: " + ocbRecordData.getInputFile());
            } else {
                logger.info("Input filnavn....: ingen (ny post genereres)");
            }
            logger.info("Skabelon.........: " + ocbRecordData.getTemplate());
            logger.info("Format...........: " + ocbRecordData.getFormatType().typeToString());
            logger.info("Faust nummer.....: " + ocbRecordData.getFaustNumber());
            if (ocbRecordData.getInputEncoding() != null && ocbRecordData.getInputFile() != null) {
                logger.info("Filkodning.......: " + ocbRecordData.getInputEncoding());
            } else {
                logger.info("Filkodning.......: ignoreret (ingen inputfil angivet)");
            }
            if (ocbRecordData.isRemote()) {
                logger.info("Remote...........: ja, mod webservice");
            } else {
                logger.info("Remote...........: nej, lokalt");
            }
            if (ocbRecordData.isRemote()) {
                logger.info("Tracking id......: " + ocbRecordData.getUuid());
                logger.info("Build WS url.....: " + ocbRecordData.getBuildWsUrl());
            }
            logger.info("Numberroll url...: " + ocbRecordData.getOpenNumberRollUrl());
        } finally {
            logger.exit();
        }
    }

    // Validate input parameters
    private void validateProgramParameters(OCBRecordData ocbRecordData) throws CliException {
        logger.entry(ocbRecordData);
        try {
            if (!validateProgramParametersBaseDir(ocbRecordData)) {
                throw new CliException("Programmet synes ikke at være startet i den korrekt rodfolder. Programmet skal køres fra den samme opencat-business folder som bin og distributions folderne.");
            }

            if (!validateProgramParametersDistribution(ocbRecordData)) {
                throw new CliException("Kunne ikke finde distributionen: " + ocbRecordData.getDistribution());
            }

            if (!validateProgramParametersOutputFile(ocbRecordData)) {
                throw new CliException("Output filen findes allerede.");
            }

            if (!validateProgramParametersTemplate(ocbRecordData)) {
                throw new CliException("Kunne ikke finde skabelonen: " + ocbRecordData.getTemplate());
            }

            if (!validateProgramParametersFormat(ocbRecordData)) {
                throw new CliException("Format: " + ocbRecordData.getFormat() + " er ikke blandt de tilladte værdier");
            }

            if (ocbRecordData.getInputFile() != null) {
                if (!validateProgramParametersInputFile(ocbRecordData)) {
                    throw new CliException("Kunne ikke finde input fil: " + ocbRecordData.getTemplate());
                }
                if (!validateProgramParametersCharset(ocbRecordData)) {
                    throw new CliException("Ukendst fil koding: " + ocbRecordData.getInputEncoding());
                }
            }
        } finally {
            logger.exit();
        }
    }

    // Validates program parameter basedir is correct
    private Boolean validateProgramParametersBaseDir(OCBRecordData ocbRecordData) throws CliException {
        logger.entry(ocbRecordData);
        Boolean res = false;
        try {
            File baseDir = ocbRecordData.getBaseDir();
            Path path = Paths.get(baseDir.getCanonicalPath());
            if (Files.isDirectory(path)) {
                String distributionsDirName = baseDir.getCanonicalPath().concat("/distributions");
                path = Paths.get(distributionsDirName);
                res = Files.isDirectory(path);
            }
            return res;
        } catch (IOException ex) {
            throw new CliException(ex.getMessage(), ex);
        } finally {
            logger.exit(res);
        }
    }

    // Validates program distribution is correct
    private Boolean validateProgramParametersDistribution(OCBRecordData ocbRecordData) throws CliException {
        logger.entry(ocbRecordData);
        Boolean res = false;
        try {
            if (ocbRecordData.getDistribution().equalsIgnoreCase("fbs") || ocbRecordData.getDistribution().equalsIgnoreCase("dataio")) {
                File baseDir = ocbRecordData.getBaseDir();
                String distribution = ocbRecordData.getDistribution();
                String distributionsDirName = baseDir.getCanonicalPath().concat("/distributions/").concat(distribution);
                Path path = Paths.get(distributionsDirName);
                res = Files.isDirectory(path);
            }
            return res;
        } catch (IOException ex) {
            throw new CliException(ex.getMessage(), ex);
        } finally {
            logger.exit(res);
        }
    }

    // Validates program parameter output is correct
    private Boolean validateProgramParametersOutputFile(OCBRecordData ocbRecordData) throws CliException {
        logger.entry(ocbRecordData);
        Boolean res = true;
        try {
            if (ocbRecordData.getOutputFile() != null) {
                File file = new File(ocbRecordData.getOutputFile());
                res = !file.exists();
            }
            return res;
        } finally {
            logger.exit(res);
        }
    }

    // Validates program parameter template (skabelon) is correct
    private Boolean validateProgramParametersTemplate(OCBRecordData ocbRecordData) throws CliException {
        logger.entry(ocbRecordData);
        Boolean res = false;
        try {
            File baseDir = ocbRecordData.getBaseDir();
            String distribution = ocbRecordData.getDistribution();
            String template = ocbRecordData.getTemplate();
            String distributionsDirName = baseDir.getCanonicalPath().concat("/distributions");
            String completeTemplatePath = distributionsDirName.concat("/").concat(distribution).concat("/templates/").concat(template).concat(".json");
            Path path = Paths.get(completeTemplatePath);
            res = Files.exists(path);
            return res;
        } catch (IOException ex) {
            throw new CliException(ex.getMessage(), ex);
        } finally {
            logger.exit(res);
        }
    }

    // Validates program parameter format is correct
    private Boolean validateProgramParametersFormat(OCBRecordData ocbRecordData) {
        logger.entry(ocbRecordData);
        Boolean res = true;
        try {
            if (ocbRecordData.getFormat() != null) {
                String format = ocbRecordData.getFormat();
                if (!("MARC".equalsIgnoreCase(format) || "MARCXCHANGE".equalsIgnoreCase(format) || "JSON".equalsIgnoreCase(format))) {
                    res = false;
                }
            }
            return res;
        } finally {
            logger.exit(res);
        }
    }

    // Validates program parameter input file is correct
    private Boolean validateProgramParametersInputFile(OCBRecordData ocbRecordData) {
        logger.entry(ocbRecordData);
        Boolean res = false;
        try {
            if (ocbRecordData.getInputFile() != null) {
                File file = new File(ocbRecordData.getInputFile());
                res = file.exists();
            }
            return res;
        } finally {
            logger.exit(res);
        }
    }

    // Validates program parameter input file charset is correct
    private Boolean validateProgramParametersCharset(OCBRecordData ocbRecordData) {
        logger.entry(ocbRecordData);
        Boolean res = true;
        try {
            if (ocbRecordData.getInputEncoding() != null) {
                if (!("UTF-8".equalsIgnoreCase(ocbRecordData.getInputEncoding())
                        || "UTF8".equalsIgnoreCase(ocbRecordData.getInputEncoding())
                        || "LATIN-1".equalsIgnoreCase(ocbRecordData.getInputEncoding())
                        || "LATIN1".equalsIgnoreCase(ocbRecordData.getInputEncoding()))) {
                    res = false;
                }
            }
            return res;
        } finally {
            logger.exit(res);
        }
    }

    // Read javascript properties file
    private Properties getPropertiesForJavascript(OCBRecordData ocbRecordData) throws CliException {
        logger.entry(ocbRecordData);
        Properties properties = null;
        try {
            properties = new Properties();
            properties.put("javascript.basedir", ocbRecordData.getBaseDir().getCanonicalPath());
            properties.put("javascript.install.name", ocbRecordData.getDistribution());
            return properties;
        } catch (IOException e) {
            throw new CliException(e.getMessage(), e);
        } finally {
            logger.exit(properties);
        }
    }

    // Read input file as a string list
    private List<String> getInputFileAsStringList(OCBRecordData ocbRecordData) throws CliException {
        logger.entry(ocbRecordData);
        List<String> res = null;
        try {
            Path path = Paths.get(ocbRecordData.getInputFile());
            Charset charset = getInputFileEncoding(ocbRecordData);
            res = Files.readAllLines(path, charset);
            return res;
        } catch (IOException e) {
            throw new CliException(e.getMessage(), e);
        } finally {
            logger.exit(res);
        }
    }

    // Determine inputfile character encoding
    private Charset getInputFileEncoding(OCBRecordData ocbRecordData) {
        logger.entry(ocbRecordData);
        Charset charset = null;
        try {
            if (ocbRecordData.getInputEncoding() != null) {
                if ("UTF8".equalsIgnoreCase(ocbRecordData.getInputEncoding())) {
                    charset = Charset.forName("UTF-8");
                } else if ("LATIN1".equalsIgnoreCase(ocbRecordData.getInputEncoding())) {
                    charset = Charset.forName("ISO-8859-1");
                } else {
                    charset = Charset.forName("UTF-8");
                }
            } else {
                charset = Charset.forName("UTF-8");
            }
            return charset;
        } finally {
            logger.exit(charset);
        }
    }

    // Return input file as a string list
    private String getInputFileAsString(OCBRecordData ocbRecordData) throws CliException {
        logger.entry(ocbRecordData);
        String res = null;
        try {
            List<String> lines = ocbRecordData.getInputFileContentList();
            res = "";
            for (int i = 0; i < lines.size(); ++i) {
                res += lines.get(i);
                if (i < lines.size() - 1) {
                    res += System.lineSeparator();
                }
            }
            return res;
        } finally {
            logger.exit(res);
        }
    }

    // Read input file into internal data object
    private OCBRecordData readInputFileIntoData(OCBRecordData ocbRecordData) throws CliException {
        logger.entry(ocbRecordData);
        try {
            if (ocbRecordData.getInputFile() != null) {
                List<String> inputFileContentStringList = getInputFileAsStringList(ocbRecordData);
                ocbRecordData.setInputFileContentList(inputFileContentStringList);
                String inputFileContentString = getInputFileAsString(ocbRecordData);
                ocbRecordData.setInputFileContentString(inputFileContentString);
                logger.info("Detekteret følgende input filtype: " + detectInputFileContentType(ocbRecordData).typeToString());
            }
            return ocbRecordData;
        } finally {
            logger.exit(ocbRecordData);
        }
    }

    // Parse input file by file type
    private OCBRecordData parseFormatFromInput(OCBRecordData ocbRecordData) {
        logger.entry(ocbRecordData);
        try {
            if (ocbRecordData.getFormat() != null) {
                if ("MARC".equalsIgnoreCase(ocbRecordData.getFormat())) {
                    ocbRecordData.setFormatType(MarcType.MARC);
                } else if ("MARCXCHANGE".equalsIgnoreCase(ocbRecordData.getFormat())) {
                    ocbRecordData.setFormatType(MarcType.MARCXCHANGE);
                } else if ("JSON".equalsIgnoreCase(ocbRecordData.getFormat())) {
                    ocbRecordData.setFormatType(MarcType.JSON);
                } else {
                    ocbRecordData.setFormatType(MarcType.MARC);
                }
            } else {
                ocbRecordData.setFormatType(MarcType.MARC);
            }
            return ocbRecordData;
        } finally {
            logger.exit(ocbRecordData);
        }
    }

    // Determine file type of input file
    private MarcType detectInputFileContentType(OCBRecordData ocbRecordData) {
        logger.entry(ocbRecordData);
        MarcType res = MarcType.UNKNOWN;
        try {
            if (ocbRecordData.getInputFile() != null) {
                if (ocbRecordData.getInputFileContentString().contains("<record")
                        && ocbRecordData.getInputFileContentString().contains("<datafield")
                        && ocbRecordData.getInputFileContentString().contains("<subfield")) {
                    res = MarcType.MARCXCHANGE;
                } else if (ocbRecordData.getInputFileContentList().size() > 0 && ocbRecordData.getInputFileContentList().get(0).startsWith("001")) {
                    res = MarcType.MARC;
                } else if (ocbRecordData.getInputFileContentString().contains("\"fields\"")
                        && ocbRecordData.getInputFileContentString().contains("\"subfields\"")
                        && ocbRecordData.getInputFileContentString().contains("\"name\"")) {
                    res = MarcType.JSON;
                }
            }
            return res;
        } finally {
            logger.exit(res);
        }
    }

    // Initialize jackson with annotation classes
    private void addJacksonMixInAnnotations(ObjectMapper objectMapper) {
        logger.entry(objectMapper);
        try {
            for (Map.Entry<Class<?>, Class<?>> e : MixIns.getMixIns().entrySet()) {
                objectMapper.addMixInAnnotations(e.getKey(), e.getValue());
            }
        } finally {
            logger.exit();
        }
    }
}
