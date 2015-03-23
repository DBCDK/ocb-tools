//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------

/**
 * Created by stp on 13/03/15.
 */
public class TestcaseRecord {
    public TestcaseRecord() {
        this.record = "";
        this.type = null;
        this.deleted = false;
        this.link = null;
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

    public TestcaseRecordType getType() {
        return type;
    }

    public void setType( TestcaseRecordType type ) {
        this.type = type;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted( boolean deleted ) {
        this.deleted = deleted;
    }

    public String getLink() {
        return link;
    }

    public void setLink( String link ) {
        this.link = link;
    }

    //-------------------------------------------------------------------------
    //              Object
    //-------------------------------------------------------------------------

    @Override
    public String toString() {
        return "TestcaseRecord{" +
                "record='" + record + '\'' +
                ", type=" + type +
                ", deleted=" + deleted +
                ", link='" + link + '\'' +
                '}';
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private String record;
    private TestcaseRecordType type;
    private boolean deleted;
    private String link;
}
