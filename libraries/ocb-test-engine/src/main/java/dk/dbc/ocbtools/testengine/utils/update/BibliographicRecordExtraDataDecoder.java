package dk.dbc.ocbtools.testengine.utils.update;

import javax.xml.bind.JAXB;
import javax.xml.transform.Source;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

public class BibliographicRecordExtraDataDecoder {
    private static final XLogger logger = XLoggerFactory.getXLogger(BibliographicRecordExtraDataDecoder.class);

    public BibliographicRecordExtraDataDecoder() {
    }

    public static BibliographicRecordExtraData fromXml(Source xml) {
        logger.entry(new Object[0]);
        BibliographicRecordExtraData result = null;

        BibliographicRecordExtraData var2;
        try {
            var2 = result = (BibliographicRecordExtraData)JAXB.unmarshal(xml, BibliographicRecordExtraData.class);
        } finally {
            logger.exit(result);
        }

        return var2;
    }
}
