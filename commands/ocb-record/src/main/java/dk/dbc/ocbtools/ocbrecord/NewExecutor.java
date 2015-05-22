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
import dk.dbc.iscrum.records.MarcRecordFactory;
import dk.dbc.iscrum.records.MarcXchangeFactory;
import dk.dbc.iscrum.records.marcxchange.ObjectFactory;
import dk.dbc.iscrum.records.marcxchange.RecordType;
import dk.dbc.iscrum.utils.json.Json;
import dk.dbc.iscrum.utils.logback.filters.BusinessLoggerFilter;
import dk.dbc.ocbtools.commons.api.SubcommandExecutor;
import dk.dbc.ocbtools.commons.cli.CliException;
import dk.dbc.ocbtools.ocbrecord.json.MixIns;
import dk.dbc.ocbtools.scripter.Distribution;
import dk.dbc.ocbtools.scripter.ScripterException;
import dk.dbc.ocbtools.scripter.ServiceScripter;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by thl on 2/19/15.
 */
public class NewExecutor implements SubcommandExecutor {

    private static final XLogger output = XLoggerFactory.getXLogger( BusinessLoggerFilter.LOGGER_NAME );

    private OCBRecordData ocbRecordData;

    public NewExecutor( OCBRecordData ocbRecordData ) {
        output.entry();
        this.ocbRecordData = ocbRecordData;
        output.exit();
    }

    @Override
    // Main command entrypoint
    public void actionPerformed() throws CliException {
        output.entry();
        try {
            ocbRecordData = parseFormatFromInput( ocbRecordData );
            ocbRecordData = readPropertiesFile( ocbRecordData );
            ocbRecordData = getFaustNumberFromOpenNumberRoll( ocbRecordData );
            validateProgramParameters( ocbRecordData );
            printProgramInfo( ocbRecordData );
            ocbRecordData = readInputFileIntoData( ocbRecordData );

            if ( ocbRecordData.isRemote() ) {
                String resultOfRemoteJavascriptCall = callWebService( ocbRecordData );
                String generatedOutput = generateOutput( ocbRecordData, resultOfRemoteJavascriptCall );
                printGeneratedOutput( ocbRecordData, generatedOutput );
            } else {
                ServiceScripter serviceScripter = getNewScripterService( ocbRecordData );
                String resultOfLocalJavascriptCall = callJavascript( serviceScripter, ocbRecordData );
                String generatedOutput = generateOutput( ocbRecordData, resultOfLocalJavascriptCall );
                printGeneratedOutput( ocbRecordData, generatedOutput );
            }
        } finally {
            output.exit();
        }
    }

    // Call the build webservice and return the json result as a string
    private String callWebService( OCBRecordData ocbRecordData ) throws CliException {
        output.entry( ocbRecordData );
        String res = null;
        try {
            String webServiceUrl = ocbRecordData.getBuildWsUrl();
            URL url = new URL( webServiceUrl );
            BuildService buildService = new BuildService( url );
            BuildPortType buildPortType = buildService.createPort();

            BuildRequest buildRequest = createBuildRequest( ocbRecordData );
            BuildResult buildResult = buildPortType.build( buildRequest );
            res = convertBuildResultToJson( buildResult );
            return res;
        } catch ( MalformedURLException e ) {
            output.catching( e );
            output.info( "Programmet fejlede med denne besked:" );
            output.info( e.getMessage() );
            throw new CliException( e.getMessage(), e );
        } finally {
            output.exit( res );
        }
    }

    // Convert the buildresult to a json string
    private String convertBuildResultToJson( BuildResult buildResult ) throws CliException {
        output.entry( buildResult );
        String res = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            addJacksonMixInAnnotations( objectMapper );
            MarcRecord record = null;
            if ( buildResult != null && buildResult.getBibliographicRecord() != null && buildResult.getBibliographicRecord().getRecordData() != null) {
                List<Object> list = buildResult.getBibliographicRecord().getRecordData().getContent();
                for ( Object o : list ) {
                    if ( o instanceof Node ) {
                        record = MarcConverter.createFromMarcXChange( new DOMSource( ( Node ) o ) );
                        res = objectMapper.writeValueAsString( record );
                        break;
                    }
                }
            }
            return res;
        } catch ( JsonProcessingException e ) {
            throw new CliException( e.getMessage(), e );
        } finally {
            output.exit( res );
        }
    }

    // Create buildservice request from ocbRecordData object
    private BuildRequest createBuildRequest( OCBRecordData ocbRecordData ) throws CliException {
        output.entry( ocbRecordData );
        BuildRequest buildRequest = null;
        try {
            buildRequest = new BuildRequest();
            buildRequest.setSchemaName( ocbRecordData.getTemplate() );
            buildRequest.setTrackingId( ocbRecordData.getUuid() );
            BibliographicRecord bibliographicRecord = new BibliographicRecord();
            bibliographicRecord.setRecordPacking( OCBRecordStatics.RECORD_PACKING );
            bibliographicRecord.setRecordSchema( OCBRecordStatics.RECORD_SCHEMA );
            RecordData recordData = new RecordData();
            bibliographicRecord.setRecordData( recordData );
            ExtraRecordData extraRecordData = new ExtraRecordData();
            bibliographicRecord.setExtraRecordData( extraRecordData );
            buildRequest.setBibliographicRecord( bibliographicRecord );
            return buildRequest;
        } finally {
            output.exit( buildRequest );
        }
    }

    // Call the embedded buildservice javascript logic
    private String callJavascript( ServiceScripter serviceScripter, OCBRecordData ocbRecordData ) throws CliException {
        output.entry( serviceScripter, ocbRecordData );
        String res = null;
        try {
            Properties properties = getPropertiesForJavascript( ocbRecordData );
            String inputStringAsJson = getInputFileAsJsonString( ocbRecordData );
            Object recordObj = serviceScripter.callMethod( "openbuild.js", "buildRecord", ocbRecordData.getTemplate(), inputStringAsJson, ocbRecordData.getFaustNumber(), properties );
            res = recordObj.toString();
            return res;
        } catch ( ScripterException e ) {
            throw new CliException( e.getMessage(), e );
        } finally {
            output.exit( res );
        }
    }

    // Read the requested inputfile and return it as a json string
    private String getInputFileAsJsonString( OCBRecordData ocbRecordData ) throws CliException {
        output.entry( ocbRecordData );
        String res = null;
        try {
            if ( ocbRecordData.getInputFile() != null ) {
                MarcType marcType = detectInputFileContentType( ocbRecordData );
                switch ( marcType ) {
                    case JSON:
                        res = ocbRecordData.getInputFileContentString();
                        break;
                    case MARCXCHANGE:
                        res = convertMarcXchangeStringToJson( ocbRecordData );
                        break;
                    case MARC:
                        res = convertMarcStringToJson( ocbRecordData );
                        break;
                    case UNKNOWN:
                    default:
                        throw new CliException( "Fejl ved bestemmelse af input filformat" );
                }
            }
            return res;
        } finally {
            output.exit( res );
        }
    }

    // Convert a MarXchange string to a json string
    private String convertMarcXchangeStringToJson( OCBRecordData ocbRecordData ) throws CliException {
        output.entry( ocbRecordData );
        String res = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            addJacksonMixInAnnotations( objectMapper );
            MarcRecord marcRecord = MarcConverter.convertFromMarcXChange( ocbRecordData.getInputFileContentString() );
            res = objectMapper.writeValueAsString( marcRecord );
            return res;
        } catch ( JsonProcessingException e ) {
            throw new CliException( e.getMessage(), e );
        } finally {
            output.exit( res );
        }
    }

    // Convert a Marc string to a json string
    private String convertMarcStringToJson( OCBRecordData ocbRecordData ) throws CliException {
        output.entry( ocbRecordData );
        String res = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            addJacksonMixInAnnotations( objectMapper );
            MarcRecord marcRecord = MarcRecordFactory.readRecord( ocbRecordData.getInputFileContentString() );
            res = objectMapper.writeValueAsString( marcRecord );
            return res;
        } catch ( JsonProcessingException e ) {
            throw new CliException( e.getMessage(), e );
        } finally {
            output.exit( res );
        }
    }

    // Create a new scriptor service object. The scriptor service contains the javascript environment
    private ServiceScripter getNewScripterService( OCBRecordData ocbRecordData ) throws CliException {
        output.entry( ocbRecordData );
        ServiceScripter serviceScripter = null;
        try {
            serviceScripter = new ServiceScripter();
            serviceScripter.setBaseDir( ocbRecordData.getBaseDir().getCanonicalPath() );
            serviceScripter.setModulesKey( "modules.search.path" );
            ArrayList<Distribution> distributions = new ArrayList<>();
            distributions.add( new Distribution( ocbRecordData.getDistribution(), "distributions/" + ocbRecordData.getDistribution() ) );
            serviceScripter.setDistributions( distributions );
            serviceScripter.setServiceName( "build" );
            return serviceScripter;
        } catch ( IOException e ) {
            throw new CliException( e.getMessage(), e );
        } finally {
            output.exit( serviceScripter );
        }
    }

    // Write the formatted buildservice result string to a file
    private void writeResultToFile( OCBRecordData ocbRecordData, String result ) throws CliException {
        output.entry( ocbRecordData, result );
        try {
            File file = new File( ocbRecordData.getOutputFile() );
            if ( !file.exists() ) {
                Path path = Paths.get( ocbRecordData.getOutputFile() );
                Files.write( path, result.getBytes( Charset.forName( "UTF-8" ) ) );
            } else {
                throw new CliException( "Fejl, output filen findes allerede." );
            }
        } catch ( IOException ex ) {
            throw new CliException( ex.getMessage(), ex );
        } finally {
            output.exit();
        }
    }

    // Generates output in the request format (JSON, MARC, MarcXchange) and returns it as a string
    private String generateOutput( OCBRecordData ocbRecordData, String result ) throws CliException {
        output.entry( ocbRecordData, result );
        String res = null;
        try {
            switch ( ocbRecordData.getFormatType() ) {
                case JSON:
                    res = result;
                    break;
                case MARCXCHANGE:
                    res = decodeJsonToMarcXchange( result );
                    break;
                case UNKNOWN:
                case MARC:
                default:
                    res = decodeJsonToMarc( result );
                    break;
            }
            return res;
        } finally {
            output.exit( res );
        }
    }

    // Convert a json string to a MARC formatted string
    private String decodeJsonToMarc( String result ) throws CliException {
        output.entry( result );
        String res = null;
        try {
            MarcRecord marcRecord = Json.decode( result, MarcRecord.class );
            res = marcRecord.toString();
            return res;
        } catch ( IOException ex ) {
            throw new CliException( ex.getMessage(), ex );
        } finally {
            output.exit( res );
        }
    }

    // Convert a json string to a MarcXchange formatted string
    private String decodeJsonToMarcXchange( String result ) throws CliException {
        output.entry( result );
        String res = null;
        try {
            String MARCXCHANGE_1_1_SCHEMA_LOCATION = "http://www.loc.gov/standards/iso25577/marcxchange-1-1.xsd";

            MarcRecord marcRecord = Json.decode( result, MarcRecord.class );
            RecordType marcXhangeType = MarcXchangeFactory.createMarcXchangeFromMarc( marcRecord );
            ObjectFactory objectFactory = new ObjectFactory();
            JAXBElement<RecordType> jAXBElement = objectFactory.createRecord( marcXhangeType );

            JAXBContext jaxbContext = JAXBContext.newInstance( RecordType.class );
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty( Marshaller.JAXB_SCHEMA_LOCATION, MARCXCHANGE_1_1_SCHEMA_LOCATION );

            DocumentBuilderFactory dbf;
            dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware( true );
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            marshaller.marshal( jAXBElement, document );

            // Code below has shamelessly been taken from these answers:
            // http://stackoverflow.com/a/10356325
            // http://stackoverflow.com/a/139096
            DOMSource domSource = new DOMSource( document );
            StringWriter stringWriter = new StringWriter();
            StreamResult streamResult = new StreamResult( stringWriter );
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform( domSource, streamResult );
            res = stringWriter.toString();
            return res;
        } catch ( ParserConfigurationException | JAXBException | IOException | TransformerException e ) {
            throw new CliException( e.getMessage(), e );
        } finally {
            output.exit( res );
        }
    }

    // Output the formatted result to either a file or screen
    private void printGeneratedOutput( OCBRecordData ocbRecordData, String result ) throws CliException {
        output.entry( ocbRecordData, result );
        try {
            if ( ocbRecordData.getOutputFile() != null ) {
                writeResultToFile( ocbRecordData, result );
            } else {
                output.info( "\n-------------- Output start --------------\n" + result + "\n-------------- Output slut ---------------" );
            }
        } finally {
            output.exit();
        }
    }

    // Read the properties file and populate internal data object
    private OCBRecordData readPropertiesFile( OCBRecordData ocbRecordData ) throws CliException {
        output.entry( ocbRecordData );
        try {
            InputStream inputStream = new FileInputStream( "ocb-record.settings" );
            InputStreamReader inputStreamReader = new InputStreamReader( inputStream, "UTF8" );
            Properties properties = new Properties();
            properties.load( inputStreamReader );
            String webserviceUrl;
            if ( ocbRecordData.getDistribution().equalsIgnoreCase( "fbs" ) ) {
                if ( !properties.containsKey( OCBRecordStatics.PROP_BUILD_FBS_URL ) ) {
                    throw new CliException( "Property " + OCBRecordStatics.PROP_BUILD_FBS_URL + " ikke fundet i settings fil." );
                }
                webserviceUrl = properties.getProperty( OCBRecordStatics.PROP_BUILD_FBS_URL );
            } else if ( ocbRecordData.getDistribution().equalsIgnoreCase( "dataio" ) ) {
                if ( !properties.containsKey( OCBRecordStatics.PROP_BUILD_DATAIO_URL ) ) {
                    throw new CliException( "Property " + OCBRecordStatics.PROP_BUILD_DATAIO_URL + " ikke fundet i settings fil." );
                }
                webserviceUrl = properties.getProperty( OCBRecordStatics.PROP_BUILD_DATAIO_URL );
            } else {
                // This shouldn't happen, but probably did anyway, bugger
                throw new CliException( "Property " + OCBRecordStatics.PROP_BUILD_DATAIO_URL + " ikke fundet i settings fil." );
            }
            ocbRecordData.setBuildWsUrl( webserviceUrl );

            if ( !properties.containsKey( OCBRecordStatics.PROP_OPENNUMBERROLL_URL ) ) {
                throw new CliException( "Property " + OCBRecordStatics.PROP_OPENNUMBERROLL_URL + " ikke fundet i settings fil." );
            }
            String openNumberRollUrl = properties.getProperty( OCBRecordStatics.PROP_OPENNUMBERROLL_URL );
            ocbRecordData.setOpenNumberRollUrl( openNumberRollUrl );
            return ocbRecordData;
        } catch ( FileNotFoundException ex ) {
            throw new CliException( "ocb-record.settings fil ikke fundet.", ex );
        } catch ( IOException ex ) {
            throw new CliException( ex.getMessage(), ex );
        } finally {
            output.exit( ocbRecordData );
        }
    }

    // Get faust number from opennumberroll
    private OCBRecordData getFaustNumberFromOpenNumberRoll( OCBRecordData ocbRecordData ) throws CliException {
        output.entry( ocbRecordData );
        OCBRecordData res = ocbRecordData;
        try {
            String openNumberRollUrl = ocbRecordData.getOpenNumberRollUrl();
            Client client = ClientBuilder.newClient();
            WebTarget webTarget = client.target( openNumberRollUrl );
            Response response = webTarget.request().get();
            if ( response.getStatus() != 200 ) {
                throw new CliException( "Fik statuskode " + response.getStatus() + " fra OpenNumberRoll" );
            }
            String openNumberRollRsponse = response.readEntity( String.class );
            String faustNumber = getFaustNumberFromOpenNumberRollReponse( openNumberRollRsponse );
            res.setFaustNumber( faustNumber );
            return res;
        } finally {
            output.exit( res );
        }
    }

    // Extract faustnumber from opennumberroll xml response
    private String getFaustNumberFromOpenNumberRollReponse( String openNumberRollResponse ) throws CliException {
        output.entry( openNumberRollResponse );
        String res = null;
        try {
            String tag = "rollNumber>";
            int startIdx = openNumberRollResponse.indexOf( tag );
            startIdx += tag.length();
            if ( startIdx < 1 ) {
                throw new CliException( "Kunne ikke finde rollNumber tag i svaret fra OpenNumberRoll" );
            }

            int endIdx = openNumberRollResponse.indexOf( "<", startIdx );
            res = openNumberRollResponse.substring( startIdx, endIdx );
            return res;
        } finally {
            output.exit( res );
        }
    }

    // Output program running parameters
    private void printProgramInfo( OCBRecordData ocbRecordData ) {
        output.entry( ocbRecordData );
        try {
            output.info( "ocb-record parametre:");
            output.info( "Distribution.....: " + ocbRecordData.getDistribution() );
            if ( ocbRecordData.getOutputFile() != null ) {
                output.info( "Output filnavn...: " + ocbRecordData.getOutputFile() );
            } else {
                output.info( "Output filnavn...: ingen (direkte til skærm)" );
            }
            if ( ocbRecordData.getInputFile() != null ) {
                output.info( "Input filnavn....: " + ocbRecordData.getInputFile() );
            } else {
                output.info( "Input filnavn....: ingen (ny post genereres)" );
            }
            output.info( "Skabelon.........: " + ocbRecordData.getTemplate() );
            output.info( "Format...........: " + ocbRecordData.getFormatType().typeToString() );
            output.info( "Faust nummer.....: " + ocbRecordData.getFaustNumber() );
            if ( ocbRecordData.getInputEncoding() != null && ocbRecordData.getInputFile() != null ) {
                output.info( "Filkodning.......: " + ocbRecordData.getInputEncoding() );
            } else {
                output.info( "Filkodning.......: ignoreret (ingen inputfil angivet)" );
            }
            if ( ocbRecordData.isRemote() ) {
                output.info( "Remote...........: ja, mod webservice" );
            } else {
                output.info( "Remote...........: nej, lokalt" );
            }
            if ( ocbRecordData.isRemote() ) {
                output.info( "Tracking id......: " + ocbRecordData.getUuid() );
                output.info( "Build WS url.....: " + ocbRecordData.getBuildWsUrl() );
            }
            output.info( "Numberroll url...: " + ocbRecordData.getOpenNumberRollUrl() );
        } finally {
            output.exit();
        }
    }

    // Validate input parameters
    private void validateProgramParameters( OCBRecordData ocbRecordData ) throws CliException {
        output.entry( ocbRecordData );
        try {
            if ( !validateProgramParametersBaseDir( ocbRecordData ) ) {
                throw new CliException( "Programmet synes ikke at være startet i den korrekt rodfolder. Programmet skal køres fra den samme opencat-business folder som bin og distributions folderne." );
            }

            if ( !validateProgramParametersDistribution( ocbRecordData ) ) {
                throw new CliException( "Kunne ikke finde distributionen: " + ocbRecordData.getDistribution() );
            }

            if ( !validateProgramParametersOutputFile( ocbRecordData ) ) {
                throw new CliException( "Output filen findes allerede." );
            }

            if ( !validateProgramParametersTemplate( ocbRecordData ) ) {
                throw new CliException( "Kunne ikke finde skabelonen: " + ocbRecordData.getTemplate() );
            }

            if ( !validateProgramParametersFormat( ocbRecordData ) ) {
                throw new CliException( "Format: " + ocbRecordData.getFormat() + " er ikke blandt de tilladte værdier" );
            }

            if ( ocbRecordData.getInputFile() != null ) {
                if ( !validateProgramParametersInputFile( ocbRecordData ) ) {
                    throw new CliException( "Kunne ikke finde input fil: " + ocbRecordData.getTemplate() );
                }
                if ( !validateProgramParametersCharset( ocbRecordData ) ) {
                    throw new  CliException( "Ukendst fil koding: " + ocbRecordData.getInputEncoding() );
                }
            }
        } finally {
            output.exit();
        }
    }

    // Validates program parameter basedir is correct
    private Boolean validateProgramParametersBaseDir( OCBRecordData ocbRecordData ) throws CliException {
        output.entry( ocbRecordData );
        Boolean res = false;
        try {
            File baseDir = ocbRecordData.getBaseDir();
            Path path = Paths.get( baseDir.getCanonicalPath() );
            if ( Files.isDirectory( path ) ) {
                String distributionsDirName = baseDir.getCanonicalPath().concat( "/distributions" );
                path = Paths.get( distributionsDirName );
                res = Files.isDirectory( path );
            }
            return res;
        } catch ( IOException ex ) {
            throw new CliException( ex.getMessage(), ex );
        } finally {
            output.exit( res );
        }
    }

    // Validates program distribution is correct
    private Boolean validateProgramParametersDistribution( OCBRecordData ocbRecordData ) throws CliException {
        output.entry( ocbRecordData );
        Boolean res = false;
        try {
            if ( ocbRecordData.getDistribution().equalsIgnoreCase( "fbs" ) || ocbRecordData.getDistribution().equalsIgnoreCase( "dataio" ) ) {
                File baseDir = ocbRecordData.getBaseDir();
                String distribution = ocbRecordData.getDistribution();
                String distributionsDirName = baseDir.getCanonicalPath().concat( "/distributions/" ).concat( distribution );
                Path path = Paths.get( distributionsDirName );
                res = Files.isDirectory( path );
            }
            return res;
        } catch ( IOException ex ) {
            throw new CliException( ex.getMessage(), ex );
        } finally {
            output.exit( res );
        }
    }

    // Validates program parameter output is correct
    private Boolean validateProgramParametersOutputFile( OCBRecordData ocbRecordData ) throws CliException {
        output.entry( ocbRecordData );
        Boolean res = true;
        try {
            if ( ocbRecordData.getOutputFile() != null ) {
                File file = new File( ocbRecordData.getOutputFile() );
                res = !file.exists();
            }
            return res;
        } finally {
            output.exit( res );
        }
    }

    // Validates program parameter template (skabelon) is correct
    private Boolean validateProgramParametersTemplate( OCBRecordData ocbRecordData ) throws CliException {
        output.entry( ocbRecordData );
        Boolean res = false;
        try {
            File baseDir = ocbRecordData.getBaseDir();
            String distribution = ocbRecordData.getDistribution();
            String template = ocbRecordData.getTemplate();
            String distributionsDirName = baseDir.getCanonicalPath().concat( "/distributions" );
            String completeTemplatePath = distributionsDirName.concat( "/" ).concat( distribution ).concat( "/templates/" ).concat( template ).concat( ".json" );
            Path path = Paths.get( completeTemplatePath );
            res = Files.exists( path );
            return res;
        } catch ( IOException ex ) {
            throw new CliException( ex.getMessage(), ex );
        } finally {
            output.exit( res );
        }
    }

    // Validates program parameter format is correct
    private Boolean validateProgramParametersFormat( OCBRecordData ocbRecordData ) {
        output.entry( ocbRecordData );
        Boolean res = true;
        try {
            if ( ocbRecordData.getFormat() != null ) {
                String format = ocbRecordData.getFormat();
                if ( !( "MARC".equalsIgnoreCase( format ) || "MARCXCHANGE".equalsIgnoreCase( format ) || "JSON".equalsIgnoreCase( format ) ) ) {
                    res = false;
                }
            }
            return res;
        } finally {
            output.exit( res );
        }
    }

    // Validates program parameter input file is correct
    private Boolean validateProgramParametersInputFile( OCBRecordData ocbRecordData ) {
        output.entry( ocbRecordData );
        Boolean res = false;
        try {
            if ( ocbRecordData.getInputFile() != null ) {
                File file = new File( ocbRecordData.getInputFile() );
                res = file.exists();
            }
            return res;
        } finally {
            output.exit( res );
        }
    }

    // Validates program parameter input file charset is correct
    private Boolean validateProgramParametersCharset( OCBRecordData ocbRecordData ) {
        output.entry( ocbRecordData );
        Boolean res = true;
        try {
            if ( ocbRecordData.getInputEncoding() != null ) {
                if ( !( "UTF-8".equalsIgnoreCase( ocbRecordData.getInputEncoding() )
                        || "UTF8".equalsIgnoreCase( ocbRecordData.getInputEncoding() )
                        || "LATIN-1".equalsIgnoreCase( ocbRecordData.getInputEncoding() )
                        || "LATIN1".equalsIgnoreCase( ocbRecordData.getInputEncoding() ) ) ) {
                    res = false;
                }
            }
            return res;
        } finally {
            output.exit( res );
        }
    }

    // Read javascript properties file
    private Properties getPropertiesForJavascript( OCBRecordData ocbRecordData ) throws CliException {
        output.entry( ocbRecordData );
        Properties properties = null;
        try {
            properties = new Properties();
            properties.put( "javascript.basedir", ocbRecordData.getBaseDir().getCanonicalPath() );
            properties.put( "javascript.install.name", ocbRecordData.getDistribution() );
            return properties;
        } catch ( IOException e ) {
            throw new CliException( e.getMessage(), e );
        } finally {
            output.exit( properties );
        }
    }

    // Read input file as a string list
    private List<String> getInputFileAsStringList( OCBRecordData ocbRecordData ) throws CliException {
        output.entry( ocbRecordData );
        List<String> res = null;
        try {
            Path path = Paths.get( ocbRecordData.getInputFile() );
            Charset charset = getInputFileEncoding( ocbRecordData );
            res = Files.readAllLines( path, charset );
            return res;
        } catch ( IOException e ) {
            throw new CliException( e.getMessage(), e );
        } finally {
            output.exit( res );
        }
    }

    // Determine inputfile character encoding
    private Charset getInputFileEncoding( OCBRecordData ocbRecordData ) {
        output.entry( ocbRecordData );
        Charset charset = null;
        try {
            if ( ocbRecordData.getInputEncoding() != null ) {
                if ( "UTF8".equalsIgnoreCase( ocbRecordData.getInputEncoding() ) ) {
                    charset = Charset.forName( "UTF-8" );
                } else if ( "LATIN1".equalsIgnoreCase( ocbRecordData.getInputEncoding() ) ) {
                    charset = Charset.forName( "ISO-8859-1" );
                } else {
                    charset = Charset.forName( "UTF-8" );
                }
            } else {
                charset = Charset.forName( "UTF-8" );
            }
            return charset;
        } finally {
            output.exit( charset );
        }
    }

    // Return input file as a string list
    private String getInputFileAsString( OCBRecordData ocbRecordData ) throws CliException {
        output.entry( ocbRecordData );
        String res = null;
        try {
            List<String> lines = ocbRecordData.getInputFileContentList();
            res = "";
            for ( int i = 0 ; i < lines.size() ; ++i ) {
                res += lines.get( i );
                if ( i < lines.size() - 1 ) {
                    res += System.lineSeparator();
                }
            }
            return res;
        } finally {
            output.exit( res );
        }
    }

    // Read input file into internal data object
    private OCBRecordData readInputFileIntoData( OCBRecordData ocbRecordData ) throws CliException {
        output.entry( ocbRecordData );
        try {
            if ( ocbRecordData.getInputFile() != null ) {
                List<String> inputFileContentStringList = getInputFileAsStringList( ocbRecordData );
                ocbRecordData.setInputFileContentList( inputFileContentStringList );
                String inputFileContentString = getInputFileAsString( ocbRecordData );
                ocbRecordData.setInputFileContentString( inputFileContentString );
                output.info( "Detekteret følgende input filtype: " + detectInputFileContentType( ocbRecordData ).typeToString() );
            }
            return ocbRecordData;
        } finally {
            output.exit( ocbRecordData );
        }
    }

    // Parse input file by file type
    private OCBRecordData parseFormatFromInput( OCBRecordData ocbRecordData ) {
        output.entry( ocbRecordData );
        try {
            if ( ocbRecordData.getFormat() != null ) {
                if ( "MARC".equalsIgnoreCase( ocbRecordData.getFormat() ) ) {
                    ocbRecordData.setFormatType( MarcType.MARC );
                } else if ( "MARCXCHANGE".equalsIgnoreCase( ocbRecordData.getFormat() ) ) {
                    ocbRecordData.setFormatType( MarcType.MARCXCHANGE );
                } else if ( "JSON".equalsIgnoreCase( ocbRecordData.getFormat() ) ) {
                    ocbRecordData.setFormatType( MarcType.JSON );
                } else {
                    ocbRecordData.setFormatType( MarcType.MARC );
                }
            } else {
                ocbRecordData.setFormatType( MarcType.MARC );
            }
            return ocbRecordData;
        } finally {
            output.exit( ocbRecordData );
        }
    }

    // Determine file type of input file
    private MarcType detectInputFileContentType( OCBRecordData ocbRecordData ) {
        output.entry( ocbRecordData );
        MarcType res = MarcType.UNKNOWN;
        try {
            if ( ocbRecordData.getInputFile() != null ) {
                if ( ocbRecordData.getInputFileContentString().contains( "<record" )
                        && ocbRecordData.getInputFileContentString().contains( "<datafield" )
                        && ocbRecordData.getInputFileContentString().contains( "<subfield" ) ) {
                    res = MarcType.MARCXCHANGE;
                } else if ( ocbRecordData.getInputFileContentList().size() > 0 && ocbRecordData.getInputFileContentList().get( 0 ).startsWith( "001" ) ) {
                    res = MarcType.MARC;
                } else if ( ocbRecordData.getInputFileContentString().contains( "\"fields\"" )
                        && ocbRecordData.getInputFileContentString().contains( "\"subfields\"" )
                        && ocbRecordData.getInputFileContentString().contains( "\"name\"" ) ) {
                    res = MarcType.JSON;
                }
            }
            return res;
        } finally {
            output.exit( res );
        }
    }

    // Initialize jackson with annotation classes
    private void addJacksonMixInAnnotations( ObjectMapper objectMapper ) {
        output.entry( objectMapper );
        try {
            for ( Map.Entry<Class<?>, Class<?>> e : MixIns.getMixIns().entrySet() ) {
                objectMapper.addMixInAnnotations( e.getKey(), e.getValue() );
            }
        } finally {
            output.exit();
        }
    }
}
