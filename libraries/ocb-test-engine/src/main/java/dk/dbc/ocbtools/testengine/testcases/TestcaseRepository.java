//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.util.HashMap;
import java.util.Map;

//-----------------------------------------------------------------------------
/**
 * Repository of Testcases load from different files.
 */
public class TestcaseRepository {
    public TestcaseRepository() {
        this.testcases = new HashMap<>();
    }

    //-------------------------------------------------------------------------
    //              Interface
    //-------------------------------------------------------------------------

    public void add( Testcase testcase ) {
        testcases.put( testcase.getName(), testcase );
    }

    public Testcase find( String name ) {
        return testcases.get( name );
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger logger = XLoggerFactory.getXLogger( Testcase.class );

    private Map<String, Testcase> testcases;
}
