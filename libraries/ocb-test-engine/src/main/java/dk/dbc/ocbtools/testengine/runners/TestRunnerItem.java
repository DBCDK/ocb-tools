//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.runners;

//-----------------------------------------------------------------------------

import dk.dbc.ocbtools.testengine.executors.TestExecutor;
import dk.dbc.ocbtools.testengine.testcases.Testcase;

import java.util.List;

/**
 * Created by stp on 02/03/15.
 */
public class TestRunnerItem {
    public TestRunnerItem( Testcase testcase, List<TestExecutor> executors ) {
        this.testcase = testcase;
        this.executors = executors;
    }

    public Testcase getTestcase() {
        return testcase;
    }

    public void setTestcase( Testcase testcase ) {
        this.testcase = testcase;
    }

    public List<TestExecutor> getExecutors() {
        return executors;
    }

    public void setExecutors( List<TestExecutor> executors ) {
        this.executors = executors;
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private Testcase testcase;
    private List<TestExecutor> executors;
}
