//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------

import dk.dbc.iscrum.records.MarcRecord;
import dk.dbc.iscrum.records.MarcRecordFactory;
import dk.dbc.iscrum.utils.IOUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//-----------------------------------------------------------------------------
/**
 * Defines a testcase that is stored in a json file.
 */
public class UpdateTestcase extends BaseTestcase {
    private static final XLogger logger = XLoggerFactory.getXLogger( UpdateTestcase.class );

    /**
     * Numbers of bugs in Bugzilla that is related to this testcase.
     * <p/>
     * This property is not used, but defined here so it is posible to use the property
     * in json.
     */
    private List<String> bugs;

    private UpdateTestcaseSetup setup;
    private UpdateTestcaseRequest request;
    private UpdateTestcaseExpectedResult expected;

    public UpdateTestcase() {
        this.bugs = new ArrayList<>();
        this.setup = null;
        this.request = null;
        this.expected = null;
    }

    public List<String> getBugs() {
        return bugs;
    }

    public void setBugs( List<String> bugs ) {
        this.bugs = bugs;
    }

    public UpdateTestcaseSetup getSetup() {
        return setup;
    }

    public void setSetup( UpdateTestcaseSetup setup ) {
        this.setup = setup;
    }

    public UpdateTestcaseRequest getRequest() {
        return request;
    }

    public void setRequest( UpdateTestcaseRequest request ) {
        this.request = request;
    }

    public UpdateTestcaseExpectedResult getExpected() {
        return expected;
    }

    public void setExpected( UpdateTestcaseExpectedResult expected ) {
        this.expected = expected;
    }

    //-------------------------------------------------------------------------
    //              Factories
    //-------------------------------------------------------------------------

    public MarcRecord loadRecord() throws IOException {
        logger.entry();

        try {
            if( file == null ) {
                return null;
            }

            if( !file.isFile() ) {
                return null;
            }

            File recordFile = new File( file.getParent() + "/" + request.getRecord() );
            FileInputStream fis = new FileInputStream( recordFile );
            return MarcRecordFactory.readRecord( IOUtils.readAll( fis, "UTF-8" ) );
        }
        finally {
            logger.exit();
        }
    }

    //-------------------------------------------------------------------------
    //              Object
    //-------------------------------------------------------------------------

    @Override
    public boolean equals( Object o ) {
        if( this == o ) {
            return true;
        }
        if( !( o instanceof UpdateTestcase) ) {
            return false;
        }

        UpdateTestcase updateTestcase = (UpdateTestcase) o;

        if( description != null ? !description.equals( updateTestcase.description ) : updateTestcase.description != null ) {
            return false;
        }
        if( name != null ? !name.equals( updateTestcase.name ) : updateTestcase.name != null ) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + ( description != null ? description.hashCode() : 0 );
        return result;
    }

    @Override
    public String toString() {
        return "{ \"name\": \"" + name + "\", \"description\": \"" + description + "\" }";
    }
}
