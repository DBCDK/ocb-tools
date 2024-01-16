package dk.dbc.ocbtools.testengine.utils.update;

import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.w3c.dom.Document;

public class BibliographicRecordExtraDataEncoder {
    private static final XLogger logger = XLoggerFactory.getXLogger(BibliographicRecordExtraDataEncoder.class);

    public BibliographicRecordExtraDataEncoder() {
    }

    public static Document toXmlDocument(BibliographicRecordExtraData data) throws JAXBException, ParserConfigurationException {
        logger.entry(new Object[0]);
        Document result = null;

        Document var7;
        try {
            JAXBContext jc = JAXBContext.newInstance(new Class[]{BibliographicRecordExtraData.class});
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty("jaxb.schemaLocation", "http://oss.dbc.dk/ns/updateRecordExtraData");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            marshaller.marshal(data, document);
            result = document;
            var7 = document;
        } finally {
            logger.exit(result);
        }

        return var7;
    }

    public static String toXmlString(BibliographicRecordExtraData data) throws JAXBException, ParserConfigurationException, TransformerException {
        logger.entry(new Object[0]);
        String result = null;

        String var6;
        try {
            Document document = toXmlDocument(data);
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty("omit-xml-declaration", "no");
            transformer.setOutputProperty("method", "xml");
            transformer.setOutputProperty("indent", "no");
            transformer.setOutputProperty("encoding", "UTF-8");
            transformer.transform(new DOMSource(document), new StreamResult(sw));
            var6 = sw.toString();
        } finally {
            logger.exit();
        }

        return var6;
    }
}
