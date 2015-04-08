//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------

import dk.dbc.iscrum.records.MarcRecord;
import dk.dbc.iscrum.records.MarcRecordFactory;
import dk.dbc.iscrum.utils.IOUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
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
public class Testcase {
    public Testcase() {
        this.name = "";
        this.bugs = new ArrayList<>();
        this.distributionName = "";
        this.description = "";
        this.setup = null;
        this.request = null;
        this.expected = null;
        this.file = null;
    }

    //-------------------------------------------------------------------------
    //              Properties
    //-------------------------------------------------------------------------

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public List<String> getBugs() {
        return bugs;
    }

    public void setBugs( List<String> bugs ) {
        this.bugs = bugs;
    }

    public String getDistributionName() {
        return distributionName;
    }

    public void setDistributionName( String distributionName ) {
        this.distributionName = distributionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public TestcaseSetup getSetup() {
        return setup;
    }

    public void setSetup( TestcaseSetup setup ) {
        this.setup = setup;
    }

    public TestcaseRequest getRequest() {
        return request;
    }

    public void setRequest( TestcaseRequest request ) {
        this.request = request;
    }

    public TestcaseExpectedResult getExpected() {
        return expected;
    }

    public void setExpected( TestcaseExpectedResult expected ) {
        this.expected = expected;
    }

    public File getFile() {
        return file;
    }

    public void setFile( File file ) {
        this.file = file;
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
        if( !( o instanceof Testcase ) ) {
            return false;
        }

        Testcase testcase = (Testcase) o;

        if( description != null ? !description.equals( testcase.description ) : testcase.description != null ) {
            return false;
        }
        if( name != null ? !name.equals( testcase.name ) : testcase.name != null ) {
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

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger logger = XLoggerFactory.getXLogger( Testcase.class );

    private String name;

    /**
     * Numbers of bugs in Bugzilla that is related to this testcase.
     * <p/>
     * This property is not used, but defined here so it is posible to use the property
     * in json.
     */
    private List<String> bugs;

    @JsonIgnore
    private String distributionName;

    private String description;
    private TestcaseSetup setup;
    private TestcaseRequest request;
    private TestcaseExpectedResult expected;

    /**
     * The file that this Testcase was created from.
     * <p/>
     * It may be null.
     */
    @JsonIgnore
    private File file;
}
