package dk.dbc.ocbtools.testengine.reports;

import dk.dbc.ocbtools.testengine.runners.TestExecutorResult;
import dk.dbc.ocbtools.testengine.runners.TestResult;
import dk.dbc.ocbtools.testengine.runners.TestcaseResult;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Class to produce a test report based on TestResult.
 */
public class TextReport implements TestReport {
    private static final XLogger logger = XLoggerFactory.getXLogger(TextReport.class);
    private boolean printSummary;

    public TextReport() {
        this.printSummary = false;
    }

    public void setPrintSummary(boolean printSummary) {
        this.printSummary = printSummary;
    }

    @Override
    public void produce(TestResult testResult) {
        logger.entry();

        try {
            if (!testResult.hasError()) {
                logger.info("");
                logger.info("No errors found.");
            } else {
                for (TestcaseResult testcaseResult : testResult) {
                    if (!testcaseResult.hasError()) {
                        continue;
                    }

                    for (TestExecutorResult testExecutorResult : testcaseResult.getResults()) {
                        if (testExecutorResult.hasError()) {
                            logger.error("Testcase '{}' has an error with this executor: {}", testcaseResult.getBaseTestcase().getName(), testExecutorResult.getExecutor().name());
                            logger.error("The testcase is found in file: {}", testcaseResult.getBaseTestcase().getFile().getCanonicalPath());
                            logger.error("Error message: {}", testExecutorResult.getAssertionError().getMessage());
                            logger.debug("\tStacktrace: ", testExecutorResult.getAssertionError());
                            logger.error("");
                        }
                    }
                }
            }

            if (printSummary) {
                produceSummary(testResult);
            }
        } catch (IOException ex) {
            logger.error("Generating text report error: {}", ex.getMessage());
            logger.debug("Stacktrace", ex);
        } finally {
            logger.exit();
        }
    }

    private void produceSummary(TestResult testResult) {
        logger.entry();
        try {
            final int width = 80;
            NumberFormat numberFormat = NumberFormat.getNumberInstance();
            if (numberFormat instanceof DecimalFormat) {
                DecimalFormat df = (DecimalFormat) numberFormat;

                df.setDecimalSeparatorAlwaysShown(true);
            }

            logger.info(makeDots("-", width));
            for (TestcaseResult testcaseResult : testResult) {
                String resultStr = testcaseResult.hasError() ? "FAILED" : "SUCCESS";

                String timeFormatted = numberFormat.format(testcaseResult.getTime() / 1000.0);

                String dots;
                int otherTextLengths = 8; // Number of extra spaces/special chars in the format string.
                otherTextLengths += testcaseResult.getBaseTestcase().getName().length();
                otherTextLengths += resultStr.length();
                otherTextLengths += timeFormatted.length();
                dots = makeDots(".", width - otherTextLengths);

                logger.info("{} {} {} [ {} s]", testcaseResult.getBaseTestcase().getName(), dots, resultStr, timeFormatted);
            }

            logger.info(makeDots("-", width));
            if (testResult.hasError()) {
                logger.info("TEST FAILED");
            } else {
                logger.info("TEST SUCCESS");
            }
            logger.info(makeDots("-", width));
        } finally {
            logger.exit();
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
