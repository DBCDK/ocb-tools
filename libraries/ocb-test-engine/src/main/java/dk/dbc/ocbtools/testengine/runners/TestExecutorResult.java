//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.runners;

//-----------------------------------------------------------------------------
import dk.dbc.ocbtools.testengine.executors.TestExecutor;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 02/03/15.
 */
public class TestExecutorResult {
    public TestExecutorResult( long time, TestExecutor executor, AssertionError assertionError ) {
        this.time = time;
        this.executor = executor;
        this.assertionError = assertionError;
    }

    public long getTime() {
        return time;
    }

    public TestExecutor getExecutor() {
        return executor;
    }

    public AssertionError getAssertionError() {
        return assertionError;
    }

    public boolean hasError() {
        return assertionError != null;
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger logger = XLoggerFactory.getXLogger( TestExecutorResult.class );

    private long time;
    private TestExecutor executor;
    private AssertionError assertionError;
}