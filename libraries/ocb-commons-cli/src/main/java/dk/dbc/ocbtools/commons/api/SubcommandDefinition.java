//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.commons.api;

//-----------------------------------------------------------------------------

import dk.dbc.ocbtools.commons.cli.CliException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.io.File;
import java.util.List;

//-----------------------------------------------------------------------------
/**
 * Interface to parse the arguments parsed to a given subcommand.
 * <p/>
 * The arguments are read from the command line.
 */
public interface SubcommandDefinition {
    /**
     * Must return a list of extra options to the subcommand that
     * this implementation represents.
     *
     * @return List of options.
     */
    List<Option> createOptions() throws CliException;

    /**
     * Factory method to create a CommandExecutor from the arguments parsed
     * on the command line.
     * <p/>
     * This method is called after the arguments to the sub command has been
     * succesfully parsed.
     *
     * @param line The parsed values from the command line.
     *
     * @return A command executor.
     */
    SubcommandExecutor createExecutor( File baseDir, CommandLine line ) throws CliException;
}
