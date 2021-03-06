package dk.dbc.ocbtools.commons.cli;

import dk.dbc.ocbtools.commons.api.Subcommand;
import dk.dbc.ocbtools.commons.api.SubcommandDefinition;
import dk.dbc.ocbtools.commons.api.SubcommandExecutor;
import dk.dbc.ocbtools.commons.filesystem.OCBFileSystem;
import dk.dbc.ocbtools.commons.type.ApplicationType;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.perf4j.StopWatch;
import org.reflections.Reflections;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * CliExecutor has the responsibility to parse the arguments from the command
 * line executes the subcommand.
 * <p/>
 * The subcommand is executed by a SubcommandExecutor.
 */
public class CliExecutor {
    private static final XLogger logger = XLoggerFactory.getXLogger(CliExecutor.class);

    private String commandName;

    private CliExecutor(String commandName) {
        this.commandName = commandName;
    }

    private void execute(String[] args) throws IllegalAccessException, InstantiationException, IOException, CliException {
        logger.entry(args.toString());

        try {
            if (args.length == 0) {
                printUsage();
                return;
            }

            String cmdName = args[0];
            String[] cmdArgs = new String[]{};
            if (args.length > 1) {
                cmdArgs = Arrays.copyOfRange(args, 1, args.length);
            }

            OCBFileSystem fs = new OCBFileSystem(ApplicationType.UPDATE);
            logger.debug("Using Opencat-business directory: {}", fs.getBaseDir() != null ? fs.getBaseDir().getCanonicalPath() : "(null)");
            logger.debug("");

            boolean commandFoundAndExecuted = false;
            for (SubcommandDefinition def : getSubcommandDefinitions()) {
                Class<?> clazz = def.getClass();
                Subcommand subCommand = clazz.getAnnotation(Subcommand.class);

                if (subCommand != null && subCommand.name().equals(cmdName)) {
                    commandFoundAndExecuted = true;
                    Options options = new Options();
                    Option help = new Option("h", "help", false, "Giver en beskrivelse af de enkelte options til kommandoen");
                    options.addOption(help);

                    for (Option opt : def.createOptions()) {
                        options.addOption(opt);
                    }

                    logger.debug("Arguments to sub command: {}", Arrays.toString(cmdArgs));

                    CommandLine line = parseArguments(subCommand, options, cmdArgs);
                    if (line != null) {
                        if (line.hasOption("help")) {
                            printUsage(subCommand, options);
                            return;
                        }
                        final SubcommandExecutor executor = def.createExecutor(fs.getBaseDir(), line);
                        if (executor != null) {
                            executor.actionPerformed();
                        }
                        break;
                    } else {
                        logger.error("Ukendt argument(er).");
                    }
                }
            }

            if (!commandFoundAndExecuted) {
                logger.error("Kommandoen '{}' findes ikke.", cmdName);
                logger.error("");
                printUsage();
            }

        } finally {
            logger.exit();
        }
    }

    public static int main(String commandName, String[] args) {
        logger.entry(commandName, args);

        StopWatch watch = new StopWatch();
        try {
            logger.debug("Command: {}", commandName);
            logger.debug("Arguments: {}", args.toString());

            CliExecutor cli = new CliExecutor(commandName);
            cli.execute(args);

            return 0;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            logger.debug("Error: ", ex);

            return 1;
        } finally {
            long elapsedTime = watch.getElapsedTime();
            logger.debug("Elapsed time for command '{}': {}", commandName, elapsedTime);

            NumberFormat numberFormat = NumberFormat.getNumberInstance();
            if (numberFormat instanceof DecimalFormat) {
                DecimalFormat df = (DecimalFormat) numberFormat;

                df.setDecimalSeparatorAlwaysShown(true);
            }
            logger.info("");
            logger.info("Command '{}' executed in {} seconds", commandName, numberFormat.format(elapsedTime / 1000.0));
            logger.exit();
        }
    }

    private List<SubcommandDefinition> getSubcommandDefinitions() throws IllegalAccessException, InstantiationException {
        logger.entry();

        List<SubcommandDefinition> definitions = new ArrayList<>();
        try {
            Reflections reflections = Reflections.collect();
            Set<Class<?>> subCommands = reflections.getTypesAnnotatedWith(Subcommand.class);
            for (Class<?> clazz : subCommands) {
                Object instance = clazz.newInstance();
                if (instance instanceof SubcommandDefinition) {
                    definitions.add((SubcommandDefinition) instance);
                }
            }

            return definitions;
        } finally {
            logger.exit(definitions);
        }
    }

    private SubcommandDefinition findSubcommandDefinition(String subCommandName) throws InstantiationException, IllegalAccessException {
        logger.entry();

        try {
            for (SubcommandDefinition def : getSubcommandDefinitions()) {
                Class<?> clazz = def.getClass();
                Subcommand subCommand = clazz.getAnnotation(Subcommand.class);

                if (subCommand != null && subCommand.name().equals(subCommandName)) {
                    return def;
                }
            }

            return null;
        } finally {
            logger.exit();
        }
    }

    private CommandLine parseArguments(Subcommand subCommand, Options options, String[] args) throws IllegalAccessException, InstantiationException {
        logger.entry(options, args);
        CommandLineParser parser = new GnuParser();

        try {
            return parser.parse(options, args);
        } catch (ParseException exp) {
            if (!isHelpInArgsList(args)) {
                logger.error("Parsing failed. Reason: " + exp.getMessage());
            }
            logger.debug("Exception: {}", exp);
            printUsage(subCommand, options);
            return null;
        } finally {
            logger.exit();
        }
    }

    private Boolean isHelpInArgsList(String[] args) {
        logger.entry(args.toString());
        Boolean res = false;
        try {
            for (String arg : args) {
                if ("-h".equalsIgnoreCase(arg) || "--help".equalsIgnoreCase(arg) || "help".equalsIgnoreCase(arg)) {
                    res = true;
                    break;
                }
            }
            return res;
        } finally {
            logger.exit(res);
        }
    }

    private void printUsage() throws InstantiationException, IllegalAccessException {
        logger.entry();
        try {
            logger.info("Usage: {}", commandUsage());
            logger.info("");

            for (SubcommandDefinition def : getSubcommandDefinitions()) {
                Class<?> clazz = def.getClass();
                Subcommand subCommand = clazz.getAnnotation(Subcommand.class);

                logger.info("{}: {}", subCommand.name(), subCommand.description());
            }
        } finally {
            logger.exit();
        }
    }

    private void printUsage(Subcommand subCommand, Options options) throws InstantiationException, IllegalAccessException {
        logger.entry(options);
        try {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(subCommandUsage(subCommand.name()), options);
        } finally {
            logger.exit();
        }
    }

    private String commandUsage() {
        return String.format("%s [kommando] [argumenter]", commandName);
    }

    private String subCommandUsage(String subCommandName) throws IllegalAccessException, InstantiationException {
        SubcommandDefinition def = findSubcommandDefinition(subCommandName);
        Subcommand subCommand = def.getClass().getAnnotation(Subcommand.class);
        return String.format("%s %s %s", commandName, subCommandName, subCommand.usage());
    }


}
