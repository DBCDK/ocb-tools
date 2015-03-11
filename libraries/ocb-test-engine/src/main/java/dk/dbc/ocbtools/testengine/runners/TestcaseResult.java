//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.runners;

//-----------------------------------------------------------------------------
import dk.dbc.ocbtools.testengine.testcases.Testcase;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.util.List;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 02/03/15.
 */
public class TestcaseResult {
    public TestcaseResult( Testcase testcase, List<TestExecutorResult> results ) {
        this.testcase = testcase;
        this.results = results;
    }

    public Testcase getTestcase() {
        return testcase;
    }

    public List<TestExecutorResult> getResults() {
        return results;
    }

    public boolean hasError() {
        for( TestExecutorResult testExecutorResult : results ) {
            if( testExecutorResult.hasError() ) {
                return true;
            }
        }

        return false;
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger logger = XLoggerFactory.getXLogger( TestcaseResult.class );

    private Testcase testcase;
    private List<TestExecutorResult> results;
}
