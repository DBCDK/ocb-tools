//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.runners;

//-----------------------------------------------------------------------------
import dk.dbc.iscrum.utils.logback.filters.BusinessLoggerFilter;
import dk.dbc.ocbtools.testengine.executors.TestExecutor;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.util.ArrayList;
import java.util.List;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 20/02/15.
 */
public class TestRunner {
    public TestRunner( List<TestRunnerItem> items ) {
        this.items = items;
    }

    public TestResult run() {
        output.entry();

        try {
            TestResult testResult = new TestResult();

            for( TestRunnerItem item : items ) {
                TestcaseResult tcResult = runTestcase( item );
                if( tcResult != null ) {
                    testResult.add( tcResult );
                }
            }

            return testResult;
        }
        catch( Exception ex ) {
            output.error( "Unable to run tests: {}", ex.getMessage() );
            output.debug( "Exception stacktrace.", ex );
        }
        finally {
            output.exit();
        }

        return null;
    }

    public TestcaseResult runTestcase( TestRunnerItem testRunnerItem ) {
        output.entry();

        try {
            ArrayList<TestExecutorResult> testExecutorResults = new ArrayList<>();
            for( TestExecutor exec : testRunnerItem.getExecutors() ) {
                try {
                    exec.setup();
                    exec.executeTests();
                    exec.teardown();

                    testExecutorResults.add( new TestExecutorResult( 0, exec, null ) );
                }
                catch( AssertionError ex ) {
                    testExecutorResults.add( new TestExecutorResult( 0, exec, ex ) );
                }
            }

            return new TestcaseResult( testRunnerItem.getTestcase(), testExecutorResults );
        }
        finally {
            output.exit();
        }
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger output = XLoggerFactory.getXLogger( BusinessLoggerFilter.LOGGER_NAME );
    private List<TestRunnerItem> items;
}
