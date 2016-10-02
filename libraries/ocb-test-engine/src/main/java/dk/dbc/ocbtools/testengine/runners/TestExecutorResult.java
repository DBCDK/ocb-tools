package dk.dbc.ocbtools.testengine.runners;

import dk.dbc.ocbtools.testengine.executors.TestExecutor;

public class TestExecutorResult {
    private long time;
    private TestExecutor executor;
    private AssertionError assertionError;

    TestExecutorResult(long time, TestExecutor executor, AssertionError assertionError) {
        this.time = time;
        this.executor = executor;
        this.assertionError = assertionError;
    }

    public long getTime() {
        return time;
    }

    void setTime(long time) {
        this.time = time;
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

    @Override
    public String toString() {
        return "TestExecutorResult{" +
                "time=" + time +
                ", executor=" + executor +
                '}';
    }
}
