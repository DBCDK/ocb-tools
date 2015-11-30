//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.runners;

//-----------------------------------------------------------------------------

import dk.dbc.iscrum.utils.logback.filters.BusinessLoggerFilter;
import dk.dbc.ocbtools.testengine.executors.TestExecutor;
import org.perf4j.StopWatch;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.util.ArrayList;
import java.util.List;

//-----------------------------------------------------------------------------

/**
 * Class to run tests for a number of Updateservice testcases.
 */
public class UpdateTestRunner {
    private static final XLogger output = XLoggerFactory.getXLogger(BusinessLoggerFilter.LOGGER_NAME);
    private List<UpdateTestRunnerItem> items;

    public UpdateTestRunner(List<UpdateTestRunnerItem> items) {
        this.items = items;
    }

    public TestResult run() {
        output.entry();

        try {
            TestResult testResult = new TestResult();

            for (UpdateTestRunnerItem item : items) {
                output.info("Running testcase '{}'", item.getUpdateTestcase().getName());

                TestcaseResult tcResult = runTestcase(item);
                if (tcResult != null) {
                    testResult.add(tcResult);
                }
            }
            output.info("");

            return testResult;
        } catch (Exception ex) {
            output.error("Unable to run tests: {}", ex.getMessage());
            output.debug("Exception stacktrace.", ex);
        } finally {
            output.exit();
        }

        return null;
    }

    public TestcaseResult runTestcase(UpdateTestRunnerItem updateTestRunnerItem) {
        output.entry(updateTestRunnerItem);

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
                        TestExecutorResult testExecutorResult = new TestExecutorResult(0, exec, ex);
                        testExecutorResult.setTime(watch.getElapsedTime());
                        testExecutorResults.add(testExecutorResult);
                    } catch (Error | Exception ex) {
                        watch.stop();
                        TestExecutorResult testExecutorResult = new TestExecutorResult(0, exec, new AssertionError(ex.getMessage(), ex));
                        testExecutorResult.setTime(watch.getElapsedTime());
                        testExecutorResults.add(testExecutorResult);
                    }

                    exec.teardown();
                } catch (AssertionError ex) {
                    watch.stop();
                    TestExecutorResult testExecutorResult = new TestExecutorResult(0, exec, ex);
                    testExecutorResult.setTime(watch.getElapsedTime());
                    testExecutorResults.add(testExecutorResult);
                } catch (Error | Exception ex) {
                    watch.stop();
                    TestExecutorResult testExecutorResult = new TestExecutorResult(0, exec, new AssertionError(ex.getMessage(), ex));
                    testExecutorResult.setTime(watch.getElapsedTime());
                    testExecutorResults.add(testExecutorResult);
                }
            }

            return new TestcaseResult(updateTestRunnerItem.getUpdateTestcase(), testExecutorResults);
        } finally {
            output.exit();
        }
    }
}