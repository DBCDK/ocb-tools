//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.ocbtest;

//-----------------------------------------------------------------------------

import dk.dbc.ocbtools.commons.api.Subcommand;
import dk.dbc.ocbtools.commons.api.SubcommandDefinition;
import dk.dbc.ocbtools.commons.api.SubcommandExecutor;
import dk.dbc.ocbtools.testengine.reports.TestReport;
import dk.dbc.ocbtools.testengine.reports.TextReport;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 11/03/15.
 */
@Subcommand( name = "run",
             description = "Udfører en, flere eller alle testcases",
             usage = "[testcases, ...]" )
public class RunDefinition implements SubcommandDefinition {
    public RunDefinition() {
    }

    @Override
    public List<Option> createOptions() {
        List<Option> options = new ArrayList<>();

        Option option;
        option = new Option( "s", "summary", false, "Udskriver en opsummering af testen efter den er udført." );
        options.add( option );

        return options;
    }

    @Override
    public SubcommandExecutor createExecutor( File baseDir, CommandLine line ) {
        logger.entry( line );

        try {
            List<TestReport> reports = new ArrayList<>();

            logger.trace( "hasOption -s: {}", line.hasOption( "s" ) );
            logger.trace( "Args: {}", line.getArgList() );

            TextReport textReport = new TextReport();
            textReport.setPrintSummary( line.hasOption( "s" ) );
            reports.add( textReport );

            return new RunExecutor( baseDir, line.getArgList(), reports );
        }
        finally {
            logger.exit();
        }
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger logger = XLoggerFactory.getXLogger( RunDefinition.class );
}
