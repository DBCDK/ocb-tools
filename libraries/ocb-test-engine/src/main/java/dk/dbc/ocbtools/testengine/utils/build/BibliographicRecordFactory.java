package dk.dbc.ocbtools.testengine.utils.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import dk.dbc.common.records.utils.IOUtils;
import dk.dbc.marc.binding.MarcRecord;
import dk.dbc.marc.reader.MarcReaderException;
import dk.dbc.ocbtools.testengine.rawrepo.MarcConverter;
import dk.dbc.oss.ns.catalogingbuild.BibliographicRecord;
import dk.dbc.oss.ns.catalogingbuild.ExtraRecordData;
import dk.dbc.oss.ns.catalogingbuild.RecordData;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class BibliographicRecordFactory {
    private static final XLogger logger = XLoggerFactory.getXLogger(BibliographicRecordFactory.class);

    public BibliographicRecordFactory() {
    }

    public static BibliographicRecord loadResource(String resourceName) throws ParserConfigurationException, SAXException, IOException {
        return loadResource(BibliographicRecordFactory.class.getResourceAsStream(resourceName));
    }

    public static BibliographicRecord loadResource(InputStream in) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
        Document doc = xmlBuilder.parse(in);
        BibliographicRecord record = new BibliographicRecord();
        record.setRecordSchema("info:lc/xmlns/marcxchange-v1");
        record.setRecordPacking("xml");
        RecordData recData = new RecordData();
        recData.getContent().add("\n");
        recData.getContent().add(doc.getDocumentElement());
        recData.getContent().add("\n");
        record.setRecordData(recData);
        ExtraRecordData extraRecordData = new ExtraRecordData();
        record.setExtraRecordData(extraRecordData);
        return record;
    }

    public static BibliographicRecord loadMarcRecordInLineFormat(String resourceName) throws ParserConfigurationException, SAXException, IOException, JAXBException, MarcReaderException {
        return loadMarcRecordInLineFormat(BibliographicRecordFactory.class.getResourceAsStream(resourceName));
    }

    public static BibliographicRecord loadMarcRecordInLineFormat(File file) throws ParserConfigurationException, SAXException, IOException, JAXBException, MarcReaderException {
        return loadMarcRecordInLineFormat((InputStream)(new FileInputStream(file)));
    }

    public static BibliographicRecord loadMarcRecordInLineFormat(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException, JAXBException, MarcReaderException {
        return newMarcRecord(MarcConverter.decodeLineFormat(IOUtils.readAll(inputStream, "UTF-8")));
    }

    public static BibliographicRecord newMarcRecord(MarcRecord record) throws ParserConfigurationException, SAXException, IOException, JAXBException {
        BibliographicRecord bibRecord = new BibliographicRecord();
        bibRecord.setRecordSchema("info:lc/xmlns/marcxchange-v1");
        bibRecord.setRecordPacking("xml");
        RecordData recData = new RecordData();
        recData.getContent().add("\n");
        recData.getContent().add(MarcConverter.encodeMarcXchange(record));
        recData.getContent().add("\n");
        bibRecord.setRecordData(recData);
        ExtraRecordData extraRecordData = new ExtraRecordData();
        bibRecord.setExtraRecordData(extraRecordData);
        return bibRecord;
    }
}
