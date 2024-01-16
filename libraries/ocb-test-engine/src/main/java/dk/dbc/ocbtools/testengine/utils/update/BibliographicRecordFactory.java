package dk.dbc.ocbtools.testengine.utils.update;


import dk.dbc.marc.binding.MarcRecord;
import dk.dbc.marc.reader.DanMarc2LineFormatReader;
import dk.dbc.marc.reader.MarcReaderException;
import dk.dbc.marc.writer.MarcXchangeV1Writer;
import dk.dbc.oss.ns.catalogingupdate.BibliographicRecord;
import dk.dbc.oss.ns.catalogingupdate.ExtraRecordData;
import dk.dbc.oss.ns.catalogingupdate.RecordData;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BibliographicRecordFactory {

    public static BibliographicRecord loadResource(File file, BibliographicRecordExtraData data) throws ParserConfigurationException, IOException, JAXBException, MarcReaderException {
        final DanMarc2LineFormatReader lineFormatReader = new DanMarc2LineFormatReader(new FileInputStream(file), StandardCharsets.UTF_8);
        final MarcRecord marcRecord = lineFormatReader.read();
        final MarcXchangeV1Writer writer = new MarcXchangeV1Writer();
        writer.setProperty(MarcXchangeV1Writer.Property.ADD_XML_DECLARATION, false);

        final BibliographicRecord bibliographicRecord = new BibliographicRecord();
        bibliographicRecord.setRecordSchema("info:lc/xmlns/marcxchange-v1");
        bibliographicRecord.setRecordPacking("xml");
        final RecordData recData = new RecordData();
        recData.getContent().add("\n");
        recData.getContent().add(new String(writer.write(marcRecord, StandardCharsets.UTF_8)));
        recData.getContent().add("\n");
        bibliographicRecord.setRecordData(recData);
        bibliographicRecord.setExtraRecordData(createExtraRecordData(data));

        return bibliographicRecord;
    }

    private static ExtraRecordData createExtraRecordData(BibliographicRecordExtraData data) throws JAXBException, ParserConfigurationException {
        final ExtraRecordData extraRecordData = new ExtraRecordData();
        if (data != null) {
            Document extraRecordDocument = BibliographicRecordExtraDataEncoder.toXmlDocument(data);
            extraRecordData.getContent().add("\n");
            extraRecordData.getContent().add(extraRecordDocument.getDocumentElement());
            extraRecordData.getContent().add("\n");
        }

        return extraRecordData;
    }
}
