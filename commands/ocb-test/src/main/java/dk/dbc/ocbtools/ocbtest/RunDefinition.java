//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.ocbtest;

//-----------------------------------------------------------------------------
import dk.dbc.ocbtools.commons.api.Subcommand;
import dk.dbc.ocbtools.commons.api.SubcommandDefinition;
import dk.dbc.ocbtools.commons.api.SubcommandExecutor;
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
        return new ArrayList<>();
    }

    @Override
    public SubcommandExecutor createExecutor( File baseDir, CommandLine line ) {
        logger.entry( line );

        try {
            return new RunExecutor( baseDir, line.getArgList() );
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
