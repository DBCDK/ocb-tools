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
 * Defines a subcommand to list testcases.
 */
@Subcommand( name = "list",
             description = "Lister de testcases på tværs af filer, som evt. matcher et regulært udtryk.",
             usage = "[regex]" )
public class ListDefinition implements SubcommandDefinition {
    public ListDefinition() {
    }

    @Override
    public List<Option> createOptions() {
        return new ArrayList<>();
    }

    @Override
    public SubcommandExecutor createExecutor( File baseDir, CommandLine line ) {
        logger.entry( line );

        try {
            return new ListExecutor( baseDir, line.getArgList() );
        }
        finally {
            logger.exit();
        }
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger logger = XLoggerFactory.getXLogger( ListDefinition.class );
}
