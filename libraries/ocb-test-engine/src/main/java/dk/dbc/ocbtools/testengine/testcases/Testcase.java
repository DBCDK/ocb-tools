//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------
import org.codehaus.jackson.annotate.JsonIgnore;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;

//-----------------------------------------------------------------------------
/**
 * Defines a testcase that is stored in a json file.
 */
public class Testcase {
    public Testcase() {
        this.name = "";
        this.description = "";
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

    public String getDescription() {
        return description;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public File getFile() {
        return file;
    }

    public void setFile( File file ) {
        this.file = file;
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
    private String description;

    /**
     * The file that this Testcase was created from.
     * <p/>
     * It may be null.
     */
    @JsonIgnore
    private File file;
}
