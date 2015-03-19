//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 13/03/15.
 */
public class TestcaseRecord {
    public TestcaseRecord() {
        this.record = "";
    }

    //-------------------------------------------------------------------------
    //              Properties
    //-------------------------------------------------------------------------

    public String getRecord() {
        return record;
    }

    public void setRecord( String record ) {
        this.record = record;
    }

    @Override
    public String toString() {
        return "TestcaseRecord{" +
                "record='" + record + '\'' +
                '}';
    }

//-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private String record;
}
