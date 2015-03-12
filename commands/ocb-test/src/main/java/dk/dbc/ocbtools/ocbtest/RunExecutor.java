//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.ocbtest;

//-----------------------------------------------------------------------------

import dk.dbc.iscrum.utils.logback.filters.BusinessLoggerFilter;
import dk.dbc.ocbtools.commons.api.SubcommandExecutor;
import dk.dbc.ocbtools.commons.cli.CliException;
import dk.dbc.ocbtools.commons.filesystem.OCBFileSystem;
import dk.dbc.ocbtools.testengine.executors.CheckTemplateExecutor;
import dk.dbc.ocbtools.testengine.executors.TestExecutor;
import dk.dbc.ocbtools.testengine.executors.ValidateRecordExecutor;
import dk.dbc.ocbtools.testengine.reports.TestReport;
import dk.dbc.ocbtools.testengine.runners.TestResult;
import dk.dbc.ocbtools.testengine.runners.TestRunner;
import dk.dbc.ocbtools.testengine.runners.TestRunnerItem;
import dk.dbc.ocbtools.testengine.testcases.Testcase;
import dk.dbc.ocbtools.testengine.testcases.TestcaseRepository;
import dk.dbc.ocbtools.testengine.testcases.TestcaseRepositoryFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 11/03/15.
 */
public class RunExecutor implements SubcommandExecutor {
    public RunExecutor( File baseDir, List<String> tcNames, List<TestReport> reports ) {
        this.baseDir = baseDir;
        this.tcNames = tcNames;
        this.reports = reports;

    }

    @Override
    public void actionPerformed() throws CliException {
        output.entry();

        try {
            OCBFileSystem fs = new OCBFileSystem();
            TestcaseRepository repo = TestcaseRepositoryFactory.newInstanceWithTestcases( fs );

            List<TestRunnerItem> items = new ArrayList<>();
            for( Testcase tc : repo.findAll() ) {
                if( !matchAnyNames( tc, tcNames ) ) {
                    continue;
                }

                List<TestExecutor> executors = new ArrayList<>();
                executors.add( new CheckTemplateExecutor( baseDir, tc ) );
                if( tc.getValidation() != null ) {
                    executors.add( new ValidateRecordExecutor( baseDir, tc ) );
                }

                items.add( new TestRunnerItem( tc, executors ) );
            }

            TestRunner runner = new TestRunner( items );
            TestResult testResult = runner.run();

            for( TestReport report : reports ) {
                report.produce( testResult );
            }

            if( testResult.hasError() ) {
                output.error( "" );
                throw new CliException( "Errors where found in system-tests." );
            }
        }
        catch( IOException ex ) {
            throw new CliException( ex.getMessage(), ex );
        }
        finally {
            output.exit();
        }
    }

    private boolean matchAnyNames( Testcase tc, List<String> names ) {
        output.entry();

        try {
            if( names.isEmpty() ) {
                return true;
            }

            return names.contains( tc.getName() );
        }
        finally {
            output.exit();
        }
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger output = XLoggerFactory.getXLogger( BusinessLoggerFilter.LOGGER_NAME );

    private File baseDir;
    private List<String> tcNames;
    private List<TestReport> reports;
}
