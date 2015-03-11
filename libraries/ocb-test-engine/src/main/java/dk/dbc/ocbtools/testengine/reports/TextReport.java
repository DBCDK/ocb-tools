package dk.dbc.ocbtools.testengine.reports;

import dk.dbc.iscrum.utils.logback.filters.BusinessLoggerFilter;
import dk.dbc.ocbtools.testengine.runners.TestExecutorResult;
import dk.dbc.ocbtools.testengine.runners.TestResult;
import dk.dbc.ocbtools.testengine.runners.TestcaseResult;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

/**
 * Class to produce a test report based on TestResult.
 */
public class TextReport {
    public TextReport( TestResult testResult ) {
        this.testResult = testResult;
    }

    //-------------------------------------------------------------------------
    //              Text report
    //-------------------------------------------------------------------------

    public void printReport() {
        output.entry();

        try {
            if( !testResult.hasError() ) {
                output.info( "No errors found." );
                return;
            }

            for( TestcaseResult testcaseResult : testResult ) {
                if( !testcaseResult.hasError() ) {
                    continue;
                }

                for( TestExecutorResult testExecutorResult : testcaseResult.getResults() ) {
                    if( testExecutorResult.hasError() ) {
                        output.error( "Testcase '{}' has an error: {}", testcaseResult.getTestcase().getName(), testExecutorResult.getAssertionError().getMessage() );
                        output.debug( "\tStacktrace: ", testExecutorResult.getAssertionError() );
                    }
                }
            }
        }
        finally {
            output.exit();
        }
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger output = XLoggerFactory.getXLogger( BusinessLoggerFilter.LOGGER_NAME );
    private TestResult testResult;
}
