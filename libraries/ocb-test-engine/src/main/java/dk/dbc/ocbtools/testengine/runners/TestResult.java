package dk.dbc.ocbtools.testengine.runners;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.util.ArrayList;

public class TestResult extends ArrayList<TestcaseResult> {
    private static final XLogger logger = XLoggerFactory.getXLogger(TestResult.class);

    TestResult() {
        super();
    }

    public boolean hasError() {
        for (TestcaseResult testcaseResult : this) {
            if (testcaseResult.hasError()) {
                return true;
            }
        }

        return false;
    }

    public long getTime() {
        long time = 0;
        for (TestcaseResult testcaseResult : this) {
            time += testcaseResult.getTime();
        }

        return time;
    }

    public int countErrors() {
        int errors = 0;
        for (TestcaseResult testcaseResult : this) {
            errors += testcaseResult.countErrors();
        }

        return errors;
    }

    public int countTests() {
        int tests = 0;
        for (TestcaseResult testcaseResult : this) {
            tests += testcaseResult.countTests();
        }

        return tests;
    }

    @Override
    public String toString() {
        return "TestResult{} " + super.toString();
    }

}
