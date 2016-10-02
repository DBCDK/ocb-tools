package dk.dbc.ocbtools.testengine.runners;

import dk.dbc.ocbtools.testengine.testcases.BaseTestcase;

import java.util.List;

public class TestcaseResult {
    private BaseTestcase baseTestcase;
    private List<TestExecutorResult> results;

    TestcaseResult(BaseTestcase baseTestcase, List<TestExecutorResult> results) {
        this.baseTestcase = baseTestcase;
        this.results = results;
    }

    public BaseTestcase getBaseTestcase() {
        return baseTestcase;
    }

    public List<TestExecutorResult> getResults() {
        return results;
    }

    public boolean hasError() {
        for (TestExecutorResult testExecutorResult : results) {
            if (testExecutorResult.hasError()) {
                return true;
            }
        }
        return false;
    }

    public long getTime() {
        long time = 0;
        for (TestExecutorResult testExecutorResult : results) {
            time += testExecutorResult.getTime();
        }
        return time;
    }

    int countErrors() {
        int errors = 0;
        for (TestExecutorResult testExecutorResult : results) {
            if (testExecutorResult.getAssertionError() != null)
                errors++;
        }
        return errors;
    }

    int countTests() {
        return results.size();
    }

    @Override
    public String toString() {
        return "TestcaseResult{" +
                "baseTestcase=" + baseTestcase +
                ", results=" + results +
                '}';
    }
}
