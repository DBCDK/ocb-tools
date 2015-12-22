//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.reports;

//-----------------------------------------------------------------------------

import dk.dbc.iscrum.utils.logback.filters.BusinessLoggerFilter;
import dk.dbc.ocbtools.testengine.runners.TestExecutorResult;
import dk.dbc.ocbtools.testengine.runners.TestResult;
import dk.dbc.ocbtools.testengine.runners.TestcaseResult;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

//-----------------------------------------------------------------------------

/**
 * Class to produce a test report based on TestResult.
 */
public class TextReport implements TestReport {
    private static final XLogger output = XLoggerFactory.getXLogger(BusinessLoggerFilter.LOGGER_NAME);
    private boolean printSummary;

    public TextReport() {
        this.printSummary = false;
    }

    public void setPrintSummary(boolean printSummary) {
        this.printSummary = printSummary;
    }

    //-------------------------------------------------------------------------
    //              Text report
    //-------------------------------------------------------------------------

    @Override
    public void produce(TestResult testResult) {
        output.entry();

        try {
            if (!testResult.hasError()) {
                output.info("");
                output.info("No errors found.");
            } else {
                for (TestcaseResult testcaseResult : testResult) {
                    if (!testcaseResult.hasError()) {
                        continue;
                    }

                    for (TestExecutorResult testExecutorResult : testcaseResult.getResults()) {
                        if (testExecutorResult.hasError()) {
                            output.error("Testcase '{}' has an error with this executor: {}", testcaseResult.getBaseTestcase().getName(), testExecutorResult.getExecutor().name());
                            output.error("The testcase is found in file: {}", testcaseResult.getBaseTestcase().getFile().getCanonicalPath());
                            output.error("Error message: {}", testExecutorResult.getAssertionError().getMessage());
                            output.debug("\tStacktrace: ", testExecutorResult.getAssertionError());
                            output.error("");
                        }
                    }
                }
            }

            if (printSummary) {
                produceSummary(testResult);
            }
        } catch (IOException ex) {
            output.error("Generating text report error: {}", ex.getMessage());
            output.debug("Stacktrace", ex);
        } finally {
            output.exit();
        }
    }

    private void produceSummary(TestResult testResult) {
        output.entry();

        try {
            final int width = 80;

            NumberFormat numberFormat = NumberFormat.getNumberInstance();
            if( numberFormat instanceof DecimalFormat ) {
                DecimalFormat df = (DecimalFormat) numberFormat;

                df.setDecimalSeparatorAlwaysShown( true );
            }

            output.info(makeDots("-", width));
            for (TestcaseResult testcaseResult : testResult) {
                String resultStr = testcaseResult.hasError() ? "FAILED" : "SUCCESS";

                String timeFormatted = numberFormat.format( testcaseResult.getTime() / 1000.0 );

                String dots;
                int otherTextLengths = 8; // Number of extra spaces/special chars in the format string.
                otherTextLengths += testcaseResult.getBaseTestcase().getName().length();
                otherTextLengths += resultStr.length();
                otherTextLengths += timeFormatted.length();
                dots = makeDots(".", width - otherTextLengths);

                output.info("{} {} {} [ {} s]", testcaseResult.getBaseTestcase().getName(), dots, resultStr, timeFormatted);
            }

            output.info(makeDots("-", width));
            if (testResult.hasError()) {
                output.info("TEST FAILED");
            } else {
                output.info("TEST SUCCESS");
            }
            output.info(makeDots("-", width));
        } finally {
            output.exit();
        }
    }

    private String makeDots(String dotChar, int length) {
        String str = "";
        for (int i = 0; i < length; i++) {
            str += dotChar;
        }

        return str;
    }
}
