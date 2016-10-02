package dk.dbc.ocbtools.commons.api;

import dk.dbc.ocbtools.commons.cli.CliException;

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
    void actionPerformed() throws CliException;
}
