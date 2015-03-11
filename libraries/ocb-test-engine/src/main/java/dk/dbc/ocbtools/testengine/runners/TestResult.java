//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.runners;

//-----------------------------------------------------------------------------

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.util.ArrayList;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 02/03/15.
 */
public class TestResult extends ArrayList<TestcaseResult> {
    public TestResult( ) {
        super();
    }

    public boolean hasError() {
        for( TestcaseResult testcaseResult : this ) {
            if( testcaseResult.hasError() ) {
                return true;
            }
        }

        return false;
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger logger = XLoggerFactory.getXLogger( TestResult.class );
}
