//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.commons.cli;

//-----------------------------------------------------------------------------
import dk.dbc.ocbtools.commons.api.Subcommand;
import dk.dbc.ocbtools.commons.api.SubcommandDefinition;
import org.reflections.Reflections;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
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
    public CliExecutor() {
        this.baseDir = extractBaseDir( new File( "." ) );
    }

    public CliExecutor( File baseDir ) {
        this.baseDir = baseDir;
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

    public void execute( String usage, String... args ) {
        logger.entry( args );

        try {
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

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger logger = XLoggerFactory.getXLogger( CliExecutor.class );

    private File baseDir;
}
