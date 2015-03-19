//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.commons.filesystem;

//-----------------------------------------------------------------------------
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 13/02/15.
 */
public class OCBFileSystem {
    public OCBFileSystem() throws IOException {
        this( "." );
    }

    public OCBFileSystem( String path ) throws IOException {
        this( new File( path ).getAbsoluteFile() );
    }

    public OCBFileSystem( File file ) throws IOException {
        this.baseDir = extractBaseDir( file );
    }

    //-------------------------------------------------------------------------
    //              Properties
    //-------------------------------------------------------------------------

    public File getBaseDir() {
        return this.baseDir;
    }

    //-------------------------------------------------------------------------
    //              File methods
    //-------------------------------------------------------------------------

    public List<String> findDistributions() throws IOException {
        logger.entry();

        ArrayList<String> result = new ArrayList<>();
        try {
            File distributionsDir = new File( baseDir.getCanonicalPath() + "/" + DISTRIBUTIONS_DIRNAME );
            for( File file : distributionsDir.listFiles( new FileIgnoreFilter( COMMON_DISTRIBUTION_DIRNAME, ".svn" ) ) ) {
                if( file.isDirectory() ) {
                    result.add( file.getName() );
                }
            }

            return result;
        }
        finally {
            logger.exit( result );
        }
    }

    public List<SystemTest> findSystemtests() throws IOException {
        logger.entry();

        List<SystemTest> result = new ArrayList<>();
        try {
            for( String distName : findDistributions() ) {
                File systemTestsDir = new File( String.format( SYSTEMTESTS_DIR_PATTERN, baseDir.getCanonicalPath(), distName) );
                if( systemTestsDir.exists() ) {
                    for( File file : findFiles( systemTestsDir, new FileExtensionFilter( SYSTEMTESTS_FILE_EXT ) ) ) {
                        result.add( new SystemTest( distName, file ) );
                    }
                }
            }

            return result;
        }
        finally {
            logger.exit( result );
        }
    }

    /**
     * Loads a properties file from the baseDir of this file system.
     */
    public Properties loadSettings( String filename ) throws IOException {
        logger.entry();

        try {
            String dirPath = baseDir.getCanonicalFile() + "/etc";

            Properties props = new Properties();
            FileInputStream fileInputStream = new FileInputStream( dirPath + "/" + filename );
            props.load( fileInputStream );

            return props;
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
    private static File extractBaseDir( File file ) throws IOException {
        logger.entry( file );

        try {
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
                logger.debug( "Found base dir at {}", file.getCanonicalPath() );
                return file;
            }

            return extractBaseDir( file.getParentFile() );
        }
        finally {
            logger.exit();
        }
    }

    public List<File> findFiles( File dir, FilenameFilter filter ) throws IOException {
        logger.entry();

        List<File> result = new ArrayList<>();
        try {
            if( !dir.exists() ) {
                return result;
            }

            result.addAll( Arrays.asList( dir.listFiles( filter ) ) );
            File[] subDirs = dir.listFiles( new FileFilter() {
                @Override
                public boolean accept( File pathname ) {
                    return pathname.isDirectory();
                }
            } );

            for( File subDir : subDirs ) {
                result.addAll( findFiles( subDir, filter ) );
            }

            return result;
        }
        finally {
            logger.exit( result );
        }
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger logger = XLoggerFactory.getXLogger( OCBFileSystem.class );

    private static final String BIN_DIRNAME = "bin";
    private static final String DISTRIBUTIONS_DIRNAME = "distributions";
    private static final String ROOT_DIRECTORY_NAMES[] = { BIN_DIRNAME, DISTRIBUTIONS_DIRNAME };

    private static final String COMMON_DISTRIBUTION_DIRNAME = "common";
    private static final String SYSTEMTESTS_DIRNAME = "system-tests";

    private static final String SYSTEMTESTS_DIR_PATTERN = "%s/" + DISTRIBUTIONS_DIRNAME + "/%s/" + SYSTEMTESTS_DIRNAME;
    private static final String SYSTEMTESTS_FILE_EXT = ".json";

    private File baseDir;
}
