//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------
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

    public String getRecord() {
        return record;
    }

    public void setRecord( String record ) {
        this.record = record;
    }

    @Override
    public String toString() {
        return "TestcaseRequest{" +
                "templateName='" + templateName + '\'' +
                ", record='" + record + '\'' +
                '}';
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private String templateName;
    private String record;
}
