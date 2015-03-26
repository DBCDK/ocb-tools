//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.ocbtest;

//-----------------------------------------------------------------------------

import dk.dbc.iscrum.utils.logback.filters.BusinessLoggerFilter;
import dk.dbc.ocbtools.commons.api.SubcommandExecutor;
import dk.dbc.ocbtools.commons.cli.CliException;
import dk.dbc.ocbtools.commons.filesystem.OCBFileSystem;
import dk.dbc.ocbtools.testengine.executors.*;
import dk.dbc.ocbtools.testengine.reports.TestReport;
import dk.dbc.ocbtools.testengine.runners.TestResult;
import dk.dbc.ocbtools.testengine.runners.TestRunner;
import dk.dbc.ocbtools.testengine.runners.TestRunnerItem;
import dk.dbc.ocbtools.testengine.testcases.Testcase;
import dk.dbc.ocbtools.testengine.testcases.TestcaseRepository;
import dk.dbc.ocbtools.testengine.testcases.TestcaseRepositoryFactory;
import dk.dbc.ocbtools.testengine.testcases.ValidationResult;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//-----------------------------------------------------------------------------
/**
 * Executes the 'run' subcommand.
 */
public class RunExecutor implements SubcommandExecutor {
    public RunExecutor( File baseDir ) {
        this.baseDir = baseDir;
        this.useRemote = false;
        this.tcNames = null;
        this.reports = null;
    }

    public void setUseRemote( boolean useRemote ) {
        this.useRemote = useRemote;
    }

    public void setTcNames( List<String> tcNames ) {
        this.tcNames = tcNames;
    }

    public void setReports( List<TestReport> reports ) {
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

                if( !this.useRemote ) {
                    if( tc.getExpected().getValidation() != null ) {
                        executors.add( new ValidateRecordExecutor( baseDir, tc ) );
                    }
                    else {
                        executors.add( new CheckTemplateExecutor( baseDir, tc ) );
                    }
                }
                else {
                    List<ValidationResult> validation = tc.getExpected().getValidation();
                    if( validation != null ) {
                        executors.add( new RemoteValidateExecutor( tc ) );
                    }

                    if( tc.getExpected().getUpdate() != null ) {
                        executors.add( new RemoteUpdateExecutor( tc ) );
                    }
                    else if( validation != null && !validation.isEmpty() ) {
                        executors.add( new RemoteUpdateExecutor( tc ) );
                    }
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
        output.entry( tc, names );

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

    private boolean useRemote;
    private List<String> tcNames;

    private List<TestReport> reports;
}
