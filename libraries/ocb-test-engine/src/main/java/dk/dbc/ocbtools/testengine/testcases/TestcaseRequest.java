//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------
import java.util.Map;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 16/02/15.
 */
public class TestcaseRequest {
    public TestcaseRequest() {
        this.templateName = null;
        this.record = null;
    }

    //-------------------------------------------------------------------------
    //              Properties
    //-------------------------------------------------------------------------

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName( String templateName ) {
        this.templateName = templateName;
    }

    public TestcaseAuthentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication( TestcaseAuthentication authentication ) {
        this.authentication = authentication;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders( Map<String, Object> headers ) {
        this.headers = headers;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord( String record ) {
        this.record = record;
    }

    //-------------------------------------------------------------------------
    //              Object
    //-------------------------------------------------------------------------

    @Override
    public String toString() {
        return "TestcaseRequest{" +
                "templateName='" + templateName + '\'' +
                ", authentication=" + authentication +
                ", headers=" + headers +
                ", record='" + record + '\'' +
                '}';
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private String templateName;
    private TestcaseAuthentication authentication;
    private Map<String, Object> headers;
    private String record;
}
