package dk.dbc.ocbtools.testengine.rawrepo;

import dk.dbc.marc.binding.MarcRecord;
import dk.dbc.marc.reader.DanMarc2LineFormatReader;
import dk.dbc.marc.reader.JsonReader;
import dk.dbc.marc.reader.LineFormatReader;
import dk.dbc.marc.reader.MarcReaderException;
import dk.dbc.marc.reader.MarcXchangeV1Reader;
import dk.dbc.marc.writer.MarcXchangeV1Writer;
import dk.dbc.oss.ns.catalogingbuild.BibliographicRecord;
import dk.dbc.oss.ns.catalogingbuild.ExtraRecordData;
import dk.dbc.oss.ns.catalogingbuild.RecordData;


import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class MarcConverter {

    public static MarcRecord convertFromMarcXChange(byte[] content) throws MarcReaderException {
        final ByteArrayInputStream buf = new ByteArrayInputStream(content);
        final MarcXchangeV1Reader reader = new MarcXchangeV1Reader(buf, StandardCharsets.UTF_8);

        return reader.read();
    }

    public static byte[] encodeMarcXchange(MarcRecord marcRecord) {
        final MarcXchangeV1Writer marcXchangeV1Writer = new MarcXchangeV1Writer();
        return marcXchangeV1Writer.write(marcRecord, StandardCharsets.UTF_8);
    }

    public static MarcRecord decodeLineFormat(String s) throws MarcReaderException {
        final ByteArrayInputStream buf = new ByteArrayInputStream(s.getBytes());
        final DanMarc2LineFormatReader lineFormatReader = new DanMarc2LineFormatReader(buf, StandardCharsets.UTF_8);

        return lineFormatReader.read();
    }

    public static MarcRecord convertFromMarcJson(byte[] content) throws MarcReaderException {
        final ByteArrayInputStream buf = new ByteArrayInputStream(content);
        final JsonReader reader = new JsonReader(buf);

        return reader.read();
    }

    public static BibliographicRecord newMarcRecord(MarcRecord record) {
        BibliographicRecord bibRecord = new BibliographicRecord();
        bibRecord.setRecordSchema("info:lc/xmlns/marcxchange-v1");
        bibRecord.setRecordPacking("xml");
        RecordData recData = new RecordData();
        recData.getContent().add("\n");
        recData.getContent().add(encodeMarcXchange(record));
        recData.getContent().add("\n");
        bibRecord.setRecordData(recData);
        ExtraRecordData extraRecordData = new ExtraRecordData();
        bibRecord.setExtraRecordData(extraRecordData);
        return bibRecord;
    }

}
