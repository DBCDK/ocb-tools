package dk.dbc.ocbtools.testengine.utils.update;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = ""
)
@XmlRootElement(
        namespace = "http://oss.dbc.dk/ns/updateRecordExtraData",
        name = "updateRecordExtraData"
)
public class BibliographicRecordExtraData {
    private static final XLogger logger = XLoggerFactory.getXLogger(BibliographicRecordExtraData.class);
    public static final String NAMESPACE = "http://oss.dbc.dk/ns/updateRecordExtraData";
    private String providerName = null;

    public BibliographicRecordExtraData() {
    }

    public String getProviderName() {
        return this.providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }
}
