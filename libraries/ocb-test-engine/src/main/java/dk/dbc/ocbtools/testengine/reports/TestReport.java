package dk.dbc.ocbtools.testengine.reports;

import dk.dbc.ocbtools.testengine.runners.TestResult;

public interface TestReport {
    void produce( TestResult testResult );
}
