//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.commons.api;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
/**
 * Defines the interface for execution a subcommand after the command line arguments
 * has been parsed.
 * <p/>
 * This interfaces uses the Command design pattern.
 */
public interface SubcommandExecutor {
    /**
     * Performes the actions for the subcommand.
     */
    public void actionPerformed();
}
