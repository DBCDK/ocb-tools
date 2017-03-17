package dk.dbc.ocbtools.testengine.runners;

import dk.dbc.ocbtools.testengine.executors.TestExecutor;
import org.perf4j.StopWatch;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to run tests for a number of Buildservice testcases.
 */
public class BuildTestRunner {
    private static final XLogger output = XLoggerFactory.getXLogger(BuildTestRunner.class);
    private List<BuildTestRunnerItem> items;

    public BuildTestRunner(List<BuildTestRunnerItem> items) {
        this.items = items;
    }

    public TestResult run() {
        output.entry();

        TestResult testResult = null;
        try {
            testResult = new TestResult();

            for (BuildTestRunnerItem item : items) {
                output.info("Running testcase '{}'", item.getBuildTestcase().getName());

                TestcaseResult tcResult = runTestcase(item);
                if (tcResult != null) {
                    testResult.add(tcResult);
                }
            }
            output.info("");
            return testResult;
        } catch (Exception e) {
            output.error("Unable to run tests: {}", e.getMessage());
            output.debug("Exception stacktrace.", e);
            return null;
        } finally {
            output.exit(testResult);
        }
    }

    private TestcaseResult runTestcase(BuildTestRunnerItem buildTestRunnerItem) {
        output.entry(buildTestRunnerItem);

        TestcaseResult res = null;
        try {
            ArrayList<TestExecutorResult> testExecutorResults = new ArrayList<>();
            for (TestExecutor exec : buildTestRunnerItem.getExecutors()) {
                StopWatch watch = new StopWatch();

                try {
                    if (exec.setup()) {

                        try {
                            exec.executeTests();

                            watch.stop();
                            TestExecutorResult testExecutorResult = new TestExecutorResult(0, exec, null);
                            testExecutorResult.setTime(watch.getElapsedTime());
                            testExecutorResults.add(testExecutorResult);
                        } catch (AssertionError e) {
                            watch.stop();
                            TestExecutorResult testExecutorResult = new TestExecutorResult(0, exec, e);
                            testExecutorResult.setTime(watch.getElapsedTime());
                            testExecutorResults.add(testExecutorResult);
                        } catch (Throwable e) {
                            watch.stop();
                            TestExecutorResult testExecutorResult = new TestExecutorResult(0, exec, new AssertionError(e.getMessage(), e));
                            testExecutorResult.setTime(watch.getElapsedTime());
                            testExecutorResults.add(testExecutorResult);
                            throw new IllegalStateException("Unexpected error", e);
                        }
                    } else {
                        return res;
                    }
                    exec.teardown();
                } catch (Throwable ex) {
                    watch.stop();
                    TestExecutorResult testExecutorResult = new TestExecutorResult(0, exec, new AssertionError(ex.getMessage(), ex));
                    testExecutorResult.setTime(watch.getElapsedTime());
                    testExecutorResults.add(testExecutorResult);
                    throw new IllegalStateException("Unexpected error", ex);
                }
            }
            res = new TestcaseResult(buildTestRunnerItem.getBuildTestcase(), testExecutorResults);
            return res;
        } finally {
            output.exit(res);
        }
    }
}
