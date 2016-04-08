//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.ocbrequests;

//-----------------------------------------------------------------------------
import dk.dbc.iscrum.records.AgencyNumber;
import dk.dbc.ocbtools.ocbrequests.rawrepo.RecordEntity;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.*;
import javax.xml.soap.SOAPMessage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 05/04/16.
 */
public class CreateRequestTaskTest {
    @BeforeClass
    public static void connectDatabase() {
        emf = Persistence.createEntityManagerFactory( "ocb-requests" );
    }

    @AfterClass
    public static void disconnectDatabase() {
        if( emf != null ) {
            emf.close();
        }
    }

    @Test
    public void testFindRecords() throws Exception {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<RecordEntity> query = em.createNamedQuery( "findRecordsByAgencyId", RecordEntity.class );
            query.setParameter( "agencyid", 850160 );

            List<RecordEntity> records = query.getResultList();

            CreateRequestTask task = new CreateRequestTask( new File( "." ) );
            task.setAgencyId( new AgencyNumber( 850160 ) );
            task.setUserNumber( 1 );
            task.setRequestNumber( 1 );
            task.setRecord( records.get(0) );

            SOAPMessage message = task.createSoapRequest();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            message.writeTo( bytes );

            //assertThat( bytes.toString( "UTF-8" ), equalTo( "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\\\"http://schemas.xmlsoap.org/soap/envelope/\\\"><SOAP-ENV:Header/><SOAP-ENV:Body><updateRecord xmlns=\\\"http://oss.dbc.dk/ns/catalogingUpdate\\\"><updateRecordRequest><authentication><groupIdAut>850160</groupIdAut><passwordAut>20Koster</passwordAut><userIdAut>netpunkt</userIdAut></authentication><schemaName>ffu</schemaName><bibliographicRecord><recordSchema>info:lc/xmlns/marcxchange-v1</recordSchema><recordPacking>xml</recordPacking><recordData>\\n<record xmlns=\\\"info:lc/xmlns/marcxchange-v1\\\" xmlns:xsi=\\\"http://www.w3.org/2001/XMLSchema-instance\\\" xsi:schemaLocation=\\\"info:lc/xmlns/marcxchange-v1\\\"><leader>00000n    2200000   4500</leader><datafield ind1=\\\"0\\\" ind2=\\\"0\\\" tag=\\\"001\\\"><subfield code=\\\"a\\\">000414946</subfield><subfield code=\\\"b\\\">850160</subfield><subfield code=\\\"f\\\">a</subfield></datafield><datafield ind1=\\\"0\\\" ind2=\\\"0\\\" tag=\\\"004\\\"><subfield code=\\\"r\\\">n</subfield><subfield code=\\\"a\\\">i</subfield></datafield><datafield ind1=\\\"0\\\" ind2=\\\"0\\\" tag=\\\"008\\\"><subfield code=\\\"t\\\">a</subfield><subfield code=\\\"a\\\">2012</subfield><subfield code=\\\"l\\\">dan</subfield><subfield code=\\\"v\\\">7</subfield></datafield><datafield ind1=\\\"0\\\" ind2=\\\"0\\\" tag=\\\"009\\\"><subfield code=\\\"a\\\">a</subfield><subfield code=\\\"g\\\">xx</subfield></datafield><datafield ind1=\\\"0\\\" ind2=\\\"0\\\" tag=\\\"096\\\"><subfield code=\\\"y\\\">DIIS</subfield><subfield code=\\\"a\\\">News</subfield><subfield code=\\\"z\\\">850160</subfield><subfield code=\\\"u\\\">Fotokopier leveres ikke</subfield><subfield code=\\\"r\\\">a</subfield></datafield><datafield ind1=\\\"0\\\" ind2=\\\"0\\\" tag=\\\"245\\\"><subfield code=\\\"a\\\">Mens vi venter på Europa</subfield><subfield code=\\\"e\\\">Cecilie Felicia Stokholm Banke</subfield></datafield><datafield ind1=\\\"0\\\" ind2=\\\"0\\\" tag=\\\"557\\\"><subfield code=\\\"a\\\">Berlingske</subfield><subfield code=\\\"v\\\">8. marts 2012</subfield></datafield><datafield ind1=\\\"0\\\" ind2=\\\"0\\\" tag=\\\"700\\\"><subfield code=\\\"a\\\">Banke</subfield><subfield code=\\\"h\\\">Cecilie Felicia Stokholm</subfield></datafield><datafield ind1=\\\"0\\\" ind2=\\\"0\\\" tag=\\\"856\\\"><subfield code=\\\"u\\\">http://www.diis.dk/node/1966</subfield><subfield code=\\\"y\\\">Details and download</subfield></datafield></record>\\n</recordData><extraRecordData>\\n<ns2:updateRecordExtraData xmlns:ns2=\\\"http://oss.dbc.dk/ns/updateRecordExtraData\\\" xmlns:xsi=\\\"http://www.w3.org/2001/XMLSchema-instance\\\" xsi:schemaLocation=\\\"http://oss.dbc.dk/ns/updateRecordExtraData\\\"><providerName xmlns=\\\"\\\" xmlns:ns4=\\\"http://oss.dbc.dk/ns/catalogingUpdate\\\">opencataloging-update</providerName></ns2:updateRecordExtraData>\\n</extraRecordData></bibliographicRecord><options/><trackingId>User-1:1</trackingId></updateRecordRequest></updateRecord></SOAP-ENV:Body></SOAP-ENV:Envelope>" ) );
        }
        finally {
            em.close();
        }
    }

    private static EntityManagerFactory emf;
}
