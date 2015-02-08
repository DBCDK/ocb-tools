//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.ocbtest;

//-----------------------------------------------------------------------------
import dk.dbc.ocbtools.commons.api.Subcommand;
import dk.dbc.ocbtools.commons.api.SubcommandDefinition;
import dk.dbc.ocbtools.commons.api.SubcommandExecutor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.util.ArrayList;
import java.util.List;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 07/02/15.
 */
@Subcommand( name = "run",
             description = "Kører og tester en eller alle testcases" )
public class RunDefinition implements SubcommandDefinition {
    public RunDefinition() {
    }

    @Override
    public List<Option> createOptions() {
        return new ArrayList<>();
    }

    @Override
    public SubcommandExecutor createExecutor( CommandLine line ) {
        return new RunExecutor( line );
    }
}
