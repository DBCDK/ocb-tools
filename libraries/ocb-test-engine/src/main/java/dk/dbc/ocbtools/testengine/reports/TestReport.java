package dk.dbc.ocbtools.testengine.reports;

import dk.dbc.ocbtools.testengine.runners.TestResult;

/**
 * Created by stp on 12/03/15.
 */
public interface TestReport {
    public void produce( TestResult testResult );
}
