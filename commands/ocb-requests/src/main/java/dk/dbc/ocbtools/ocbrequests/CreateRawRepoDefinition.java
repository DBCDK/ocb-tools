package dk.dbc.ocbtools.ocbrequests;

import dk.dbc.ocbtools.commons.api.Subcommand;
import dk.dbc.ocbtools.commons.api.SubcommandDefinition;
import dk.dbc.ocbtools.commons.api.SubcommandExecutor;
import dk.dbc.ocbtools.commons.cli.CliException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Definition of the 'rawrepo-create' sub command.
 */
@Subcommand(name = "rawrepo-create",
        description = "Creates requests for the Update service from records in a rawrepo",
        usage = "[options]")
public class CreateRawRepoDefinition implements SubcommandDefinition {
    public CreateRawRepoDefinition() {
    }

    /**
     * Must return a list of extra options to the subcommand that this implementation represents.
     *
     * @return List of options.
     */
    @Override
    public List<Option> createOptions() throws CliException {
        List<Option> options = new ArrayList<>();

        Option option;

        option = new Option("uc", "user-count", true, "Number of users to create requests for");
        option.setRequired(true);
        options.add(option);

        option = new Option("rpc", "request-peer-user", true, "Number of requests for each user");
        option.setRequired(true);
        options.add(option);

        option = new Option("a", "agency", true, "Agency id to create requests for");
        option.setRequired(true);
        options.add(option);

        return options;
    }

    /**
     * Factory method to create a CommandExecutor from the arguments parsed on the command line.
     * <p/>
     * This method is called after the arguments to the sub command has been succesfully parsed.
     *
     * @param baseDir
     * @param line    The parsed values from the command line.
     * @return A command executor.
     */
    @Override
    public SubcommandExecutor createExecutor(File baseDir, CommandLine line) throws CliException {
        CreateRawRepoExecutor executor = new CreateRawRepoExecutor();

        executor.setBaseDir(baseDir);
        executor.setAgencyId(Integer.parseInt(line.getOptionValue("a")));
        executor.setUserCount(Integer.parseInt(line.getOptionValue("uc")));
        executor.setRequestsPeerUser(Integer.parseInt(line.getOptionValue("rpc")));

        return executor;
    }
}
