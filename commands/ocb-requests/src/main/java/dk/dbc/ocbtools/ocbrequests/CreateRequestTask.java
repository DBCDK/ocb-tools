//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.ocbrequests;

//-----------------------------------------------------------------------------
import dk.dbc.iscrum.records.AgencyNumber;
import dk.dbc.iscrum.records.MarcConverter;
import dk.dbc.iscrum.records.MarcRecord;
import dk.dbc.iscrum.utils.logback.filters.BusinessLoggerFilter;
import dk.dbc.ocbtools.ocbrequests.rawrepo.RecordEntity;
import dk.dbc.updateservice.client.BibliographicRecordExtraData;
import dk.dbc.updateservice.client.BibliographicRecordFactory;
import dk.dbc.updateservice.service.api.*;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.*;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 05/04/16.
 */
public class CreateRequestTask implements Runnable {


    private static final XLogger logger = XLoggerFactory.getXLogger( CreateRequestTask.class );
    private static final XLogger output = XLoggerFactory.getXLogger( BusinessLoggerFilter.LOGGER_NAME );

    private File baseDir;
    private Integer userNumber;
    private Integer requestNumber;
    private AgencyNumber agencyId;
    private RecordEntity record;

    public CreateRequestTask( File baseDir ) {
        this.baseDir = baseDir;
        this.userNumber = null;
        this.requestNumber = null;
        this.record = null;
        this.agencyId = null;
    }

    public File getBaseDir() {
        return baseDir;
    }

    public void setBaseDir( File baseDir ) {
        this.baseDir = baseDir;
    }

    public Integer getUserNumber() {
        return userNumber;
    }

    public void setUserNumber( Integer userNumber ) {
        this.userNumber = userNumber;
    }

    public Integer getRequestNumber() {
        return requestNumber;
    }

    public void setRequestNumber( Integer requestNumber ) {
        this.requestNumber = requestNumber;
    }

    public AgencyNumber getAgencyId() {
        return agencyId;
    }

    public void setAgencyId( AgencyNumber agencyId ) {
        this.agencyId = agencyId;
    }

    public RecordEntity getRecord() {
        return record;
    }

    public void setRecord( RecordEntity record ) {
        this.record = record;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used to create a thread, starting the thread
     * causes the object's <code>run</code> method to be called in that separately executing thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        logger.entry();

        try {
            File userDir = new File( baseDir.getCanonicalPath() + "/user-" + userNumber );
            if( !userDir.exists() ) {
                userDir.mkdirs();
            }

            String filename = baseDir.getCanonicalPath() + "/" + String.format( "user-%s/request-%s.xml", userNumber, requestNumber );
            try( FileOutputStream fos = new FileOutputStream( filename, false ) ) {
                SOAPMessage message = createSoapRequest();
                message.writeTo( fos );
            }
            catch( Exception ex ) {
                output.error( ex.getMessage() );
                logger.error( ex.getMessage(), ex );
            }
            logger.info( "Wrote file: {}", filename );
        }
        catch( IOException ex ) {
            output.error( ex.getMessage() );
            logger.error( ex.getMessage(), ex );
        }
        finally {
            logger.exit();
        }
    }

    public SOAPMessage createSoapRequest() throws ParserConfigurationException, JAXBException, SAXException, IOException, SOAPException {
        logger.entry();

        try {
            UpdateRecordRequest request = createRequest();

            JAXBContext jc = JAXBContext.newInstance( "dk.dbc.updateservice.service.api" );
            Marshaller m = jc.createMarshaller();

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();

            UpdateRecord soapOperation = new UpdateRecord();
            soapOperation.setUpdateRecordRequest( request );
            m.marshal( soapOperation, doc );

            MessageFactory myMsgFct = MessageFactory.newInstance();
            SOAPMessage message = myMsgFct.createMessage();
            SOAPBody soapBody = message.getSOAPBody();
            soapBody.addDocument( doc );

            return message;
        }
        catch( Exception ex ) {
            output.error( ex.getMessage() );
            logger.error( ex.getMessage(), ex );

            throw ex;
        }
        finally {
            logger.exit();
        }
    }

    public UpdateRecordRequest createRequest() throws IOException, ParserConfigurationException, JAXBException, SAXException {
        logger.entry();

        try {
            MarcRecord recordData = MarcConverter.convertFromMarcXChange( record.contentAsXml() );

            UpdateRecordRequest request = new UpdateRecordRequest();

            Authentication auth = new Authentication();
            auth.setUserIdAut( "netpunkt" );
            auth.setGroupIdAut( agencyId.toString() );
            auth.setPasswordAut( "20Koster" );

            request.setAuthentication(auth);
            request.setSchemaName( "ffu" );
            request.setTrackingId( String.format( "User-%s:%s", userNumber, requestNumber ) );

            request.setOptions(null);

            BibliographicRecordExtraData extraData = new BibliographicRecordExtraData();
            extraData.setProviderName( "opencataloging-update" );

            request.setBibliographicRecord( BibliographicRecordFactory.newMarcRecord( recordData, extraData ) );

            return request;
        }
        finally {
            logger.exit();
        }
    }
}
