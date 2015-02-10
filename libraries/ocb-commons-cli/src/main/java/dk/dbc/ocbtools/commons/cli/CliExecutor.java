//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.commons.cli;

//-----------------------------------------------------------------------------

import dk.dbc.ocbtools.commons.api.Subcommand;
import dk.dbc.ocbtools.commons.api.SubcommandDefinition;
import org.apache.commons.cli.*;
import org.reflections.Reflections;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

//-----------------------------------------------------------------------------
/**
 * CliExecutor has the responsability to parse the arguments from the command
 * line executes the subcommand.
 * <p/>
 * The subcommand is executed by a SubcommandExecutor.
 */
public class CliExecutor {
    public CliExecutor( String commandName ) {
        this.commandName = commandName;
        this.baseDir = extractBaseDir( new File( "." ) );
    }

    //-------------------------------------------------------------------------
    //              Properties
    //-------------------------------------------------------------------------

    public File getBaseDir() {
        return this.baseDir;
    }

    //-------------------------------------------------------------------------
    //              Execution
    //-------------------------------------------------------------------------

    public void execute( String[] args ) throws IllegalAccessException, InstantiationException, IOException, CliException {
        logger.entry( args );

        try {
            if( args.length == 0 ) {
                printUsage();
                return;
            }

            String cmdName = args[ 0 ];
            String[] cmdArgs = new String[]{};
            if( args.length > 1 ) {
                cmdArgs = Arrays.copyOfRange( args, 1, args.length );
            }

            logger.info( "Using Opencat-business directory: {}", baseDir != null ? baseDir.getCanonicalPath() : "(null)" );

            boolean commandFoundAndExecuted = false;
            for( SubcommandDefinition def : getSubcommandDefinitions() ) {
                Class<?> clazz = def.getClass();
                Subcommand subCommand = clazz.getAnnotation( Subcommand.class );

                if( subCommand != null && subCommand.name().equals( cmdName ) ) {
                    Options options = new Options();
                    Option help = new Option( "h", "help", false, "Giver en beskrivelse af de enkelte options til kommandoen" );
                    options.addOption( help );

                    for( Option opt : def.createOptions() ) {
                        options.addOption( opt );
                    }

                    logger.debug( "Arguments to sub command: {}", Arrays.toString( cmdArgs ) );
                    CommandLine line = parseArguments( options, cmdArgs );
                    if( line != null ) {
                        if( line.hasOption( "help" ) ) {
                            logger.info( "" );
                            HelpFormatter formatter = new HelpFormatter();
                            formatter.printHelp( subCommandUsage( subCommand.name() ), options );

                            return;
                        }
                        def.createExecutor( baseDir, line ).actionPerformed();
                        commandFoundAndExecuted = true;
                        break;
                    }
                    else {
                        logger.error( "Ukendt argument(er)." );
                    }
                }
            }

            if( !commandFoundAndExecuted ) {
                logger.error( "Kommandoen '{}' findes ikke.", cmdName );
                logger.error( "" );
                printUsage();
            }

        }
        finally {
            logger.exit();
        }
    }

    //-------------------------------------------------------------------------
    //              Commands
    //-------------------------------------------------------------------------

    public static void main( String commandName, String[] args ) {
        logger.entry( commandName, args );

        try {
            logger.debug( "Arguments: {}", args );

            CliExecutor cli = new CliExecutor( commandName );
            cli.execute( args );

            System.exit( 0 );
        }
        catch( Exception ex ) {
            logger.error( ex.getMessage() );
            logger.error( "Error: {}", ex );

            System.exit( 1 );
        }
        finally {
            logger.exit();
        }
    }

    //-------------------------------------------------------------------------
    //              Registrations
    //-------------------------------------------------------------------------

    public List<SubcommandDefinition> getSubcommandDefinitions() throws IllegalAccessException, InstantiationException {
        logger.entry();

        List<SubcommandDefinition> definitions = new ArrayList<>();
        try {
            Reflections reflections = Reflections.collect();
            Set<Class<?>> subCommands = reflections.getTypesAnnotatedWith( Subcommand.class );
            for( Class<?> clazz : subCommands ) {
                Object instance = clazz.newInstance();
                if( instance instanceof SubcommandDefinition ) {
                    definitions.add( (SubcommandDefinition)instance );
                }
            }

            return definitions;
        }
        finally {
            logger.exit( definitions );
        }
    }

    public SubcommandDefinition findSubcommandDefinition( String subCommandName ) throws InstantiationException, IllegalAccessException {
        logger.entry();

        try {
            for( SubcommandDefinition def : getSubcommandDefinitions() ) {
                Class<?> clazz = def.getClass();
                Subcommand subCommand = clazz.getAnnotation( Subcommand.class );

                if( subCommand != null && subCommand.name().equals( subCommandName ) ) {
                    return def;
                }
            }

            return null;
        }
        finally {
            logger.exit();
        }
    }

    //-------------------------------------------------------------------------
    //              Helpers
    //-------------------------------------------------------------------------

    /**
     * Extracts the base directory of the Opencat-business directory.
     *
     * @param file A File instance with the path of a directory.
     *
     * @return The base directory.
     */
    private File extractBaseDir( File file ) {
        logger.entry( file );

        try {
            final String ROOT_DIRECTORY_NAMES[] = { "bin", "distributions" };

            if( file == null ) {
                return null;
            }

            if( !file.isDirectory() ) {
                return null;
            }

            int directoriesFound = 0;
            for( String name : file.list() ) {
                if( Arrays.binarySearch( ROOT_DIRECTORY_NAMES, name ) > -1 ) {
                    directoriesFound++;
                }
            }

            if( directoriesFound == ROOT_DIRECTORY_NAMES.length ) {
                return file;
            }

            return extractBaseDir( file.getParentFile() );
        }
        finally {
            logger.exit();
        }
    }

    private CommandLine parseArguments( Options options, String[] args ) {
        CommandLineParser parser = new GnuParser();

        try {
            return parser.parse( options, args );
        }
        catch ( ParseException exp ) {
            logger.error( "Parsing failed. Reason: " + exp.getMessage() );
            logger.debug( "Exception: {}", exp );
            return null;
        }
    }

    private void printUsage() throws InstantiationException, IllegalAccessException {
        logger.entry();

        try {
            logger.info( "Usage: {}", commandUsage() );
            logger.info( "" );

            for( SubcommandDefinition def : getSubcommandDefinitions() ) {
                Class<?> clazz = def.getClass();
                Subcommand subCommand = clazz.getAnnotation( Subcommand.class );

                if( subCommand != null ) {
                    logger.info( "{}: {}", subCommand.name(), subCommand.description() );
                }
            }
        }
        finally {
            logger.exit();
        }
    }

    private String commandUsage() {
        return String.format( "%s [kommando] [argumenter]", commandName );
    }

    private String subCommandUsage( String subCommandName ) throws IllegalAccessException, InstantiationException {
        SubcommandDefinition def = findSubcommandDefinition( subCommandName );
        Subcommand subCommand = def.getClass().getAnnotation( Subcommand.class );
        return String.format( "%s %s %s", commandName, subCommandName, subCommand.usage() );
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger logger = XLoggerFactory.getXLogger( CliExecutor.class );

    private String commandName;
    private File baseDir;
}
