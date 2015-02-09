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
 * Defines a subcommand to execute the JavaScript unittests.
 */
@Subcommand( name = "js-tests",
             description = "Kører og tester en eller alle unittests i JavaScript" )
public class JsTestsDefinition implements SubcommandDefinition {
    public JsTestsDefinition() {
    }

    @Override
    public List<Option> createOptions() {
        return new ArrayList<>();
    }

    @Override
    public SubcommandExecutor createExecutor( File baseDir, CommandLine line ) {
        logger.entry( line );

        try {
            return new JsTestsExecutor( baseDir, line.getArgList() );
        }
        finally {
            logger.exit();
        }
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger logger = XLoggerFactory.getXLogger( JsTestsDefinition.class );
}
