package dk.dbc.ocbtools.testengine.runners;

import dk.dbc.ocbtools.testengine.executors.TestExecutor;
import org.perf4j.StopWatch;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to run tests for a number of Updateservice testcases.
 */
public class UpdateTestRunner {
    private static final XLogger logger = XLoggerFactory.getXLogger(UpdateTestRunner.class);
    private List<UpdateTestRunnerItem> items;

    public UpdateTestRunner(List<UpdateTestRunnerItem> items) {
        this.items = items;
    }

    public TestResult run() {
        logger.entry();
        try {
            TestResult testResult = new TestResult();
            int count = 0;
            for (UpdateTestRunnerItem item : items) {
                count++;
                logger.info("Running testcase {} of {} '{}'", count, items.size(), item.getUpdateTestcase().getName());

                TestcaseResult tcResult = runTestcase(item);
                if (tcResult != null) {
                    testResult.add(tcResult);
                }
            }
            logger.info("");
            return testResult;
        } catch (Exception ex) {
            logger.error("Unable to run tests: {}", ex.getMessage());
            logger.debug("Exception stacktrace.", ex);
        } finally {
            logger.exit();
        }
        return null;
    }

    public TestcaseResult runTestcase(UpdateTestRunnerItem updateTestRunnerItem) {
        logger.entry(updateTestRunnerItem);
        TestcaseResult res = null;
        try {
            ArrayList<TestExecutorResult> testExecutorResults = new ArrayList<>();
            for (TestExecutor exec : updateTestRunnerItem.getExecutors()) {
                StopWatch watch = new StopWatch();
                try {
                    exec.setup();
                    try {
                        exec.executeTests();
                        watch.stop();
                        TestExecutorResult testExecutorResult = new TestExecutorResult(0, exec, null);
                        testExecutorResult.setTime(watch.getElapsedTime());
                        testExecutorResults.add(testExecutorResult);
                    } catch (AssertionError ex) {
                        watch.stop();
                        logger.error("Got assertion error runTestcase update {}", ex);
                        TestExecutorResult testExecutorResult = new TestExecutorResult(0, exec, ex);
                        testExecutorResult.setTime(watch.getElapsedTime());
                        testExecutorResults.add(testExecutorResult);
                    } catch (Throwable ex) {
                        logger.error("runTestcase update execute ERROR : {}", ex);
                        watch.stop();
                        TestExecutorResult testExecutorResult = new TestExecutorResult(0, exec, new AssertionError(ex.getMessage(), ex));
                        testExecutorResult.setTime(watch.getElapsedTime());
                        testExecutorResults.add(testExecutorResult);
                        throw new IllegalStateException("Unexpected error", ex);
                    }
                    exec.teardown();
                } catch (Throwable ex) {
                    logger.error("runTestcase update ERROR : ", ex);
                    exec.teardown();
                    watch.stop();
                    TestExecutorResult testExecutorResult = new TestExecutorResult(0, exec, new AssertionError(ex.getMessage(), ex));
                    testExecutorResult.setTime(watch.getElapsedTime());
                    testExecutorResults.add(testExecutorResult);
                    throw new IllegalStateException("Unexpected error", ex);
                }
            }
            res = new TestcaseResult(updateTestRunnerItem.getUpdateTestcase(), testExecutorResults);
            return res;
        } finally {
            logger.exit();
        }
    }
}
