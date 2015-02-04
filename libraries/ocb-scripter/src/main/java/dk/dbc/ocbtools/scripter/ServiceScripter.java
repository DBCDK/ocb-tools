//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.scripter;

//-----------------------------------------------------------------------------
import dk.dbc.jslib.*;
import org.mozilla.javascript.JavaScriptException;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

//-----------------------------------------------------------------------------
public class ServiceScripter {
    //-------------------------------------------------------------------------
    //              Constructors
    //-------------------------------------------------------------------------

    /**
     * Constructs a basic ServiceScripter with all properties set to empty values.
     * <p/>
     * Remember to use the setters to initialize the properties before calling
     * <code>callMethod</code>.
     */
    public ServiceScripter() {
        this( "", "", "" );
    }

    /**
     * Constructs a basic ServiceScripter with the parsed values.
     *
     * @param baseDir          The full path to the root of the Opencat-Business installation.
     * @param distributionName Name of the distribution to use.
     * @param serviceName      Name of the service that is using this ServiceScripter.
     */
    public ServiceScripter( String baseDir, String distributionName, String serviceName ) {
        this.baseDir = baseDir;
        this.distributionName = distributionName;
        this.serviceName = serviceName;
    }

    //-------------------------------------------------------------------------
    //              Properties
    //-------------------------------------------------------------------------

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir( String baseDir ) {
        this.baseDir = baseDir;
    }

    public String getDistributionName() {
        return distributionName;
    }

    public void setDistributionName( String distributionName ) {
        this.distributionName = distributionName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName( String serviceName ) {
        this.serviceName = serviceName;
    }

    //-------------------------------------------------------------------------
    //              Script interface
    //-------------------------------------------------------------------------

    /**
     * Calls a function in a JavaScript environment and returns the result.
     * <p/>
     * The JavaScript environment is created and cached by the filename.
     *
     * @param fileName   JavaScript file to load in the environment.
     * @param methodName Name of the function to call.
     * @param args       Arguments to the function.
     *
     * @return The result of the JavaScript function.
     *
     * @throws ScripterException Encapsulate any exception from Rhino or is throwned
     *         in case of an error. For instance if the file can not be loaded.
     */
    public Object callMethod( String fileName, String methodName, Object... args ) throws ScripterException {
        logger.entry( fileName, methodName, args );

        Object result = null;
        try {
            if( !environments.containsKey( fileName ) ) {
                environments.put( fileName, createEnvironment( fileName ) );
            }

            Environment envir = environments.get( fileName );
            result = envir.callMethod( methodName, args );

            return result;
        }
        catch( JavaScriptException ex ) {
            throw new ScripterException( ex.getMessage(), ex );
        }
        finally {
            logger.exit( result );
        }
    }

    //-------------------------------------------------------------------------
    //              Helpers
    //-------------------------------------------------------------------------

    /**
     * Constructs a new Environment from a file.
     *
     * @param fileName The name of the file to load into the new Environment.
     *
     * @return The new Environment.
     *
     * @throws ScripterException Throwed in case of I/O errors.
     */
    private Environment createEnvironment( String fileName ) throws ScripterException {
        logger.entry( fileName );

        String jsFileName = "";
        try {
            Environment envir = new Environment();
            envir.registerUseFunction( createModulesHandler() );

            jsFileName = String.format( ENTRYPOINTS_PATTERN, baseDir, distributionName, serviceName, fileName );
            logger.info( "Trying to evaluate {} in the new JavaScript Environment", jsFileName );
            envir.evalFile( jsFileName );

            return envir;
        }
        catch( IOException ex ) {
            logger.error( "Unable to load file {}: {}", jsFileName, ex.getMessage() );
            throw new ScripterException( ex.getMessage(), ex );
        }
        finally {
            logger.exit();
        }
    }

    /**
     * Constructs a new moduloe handler to load modules from the Opencat-Business
     * installation.
     *
     * @return The new ModuleHandler
     */
    private ModuleHandler createModulesHandler() {
        logger.entry();

        try {
            ModuleHandler handler = new ModuleHandler();
            String modulesDir;

            modulesDir = String.format( MODULES_PATH_PATTERN, baseDir, distributionName );
            handler.registerHandler( "file", new FileSchemeHandler( modulesDir ) );
            addSearchPathsFromSettingsFile( handler, "file", modulesDir );

            modulesDir = String.format( MODULES_PATH_PATTERN, baseDir, COMMON_INSTALL_NAME );
            handler.registerHandler( COMMON_INSTALL_NAME, new FileSchemeHandler( modulesDir ) );
            addSearchPathsFromSettingsFile( handler, COMMON_INSTALL_NAME, modulesDir );

            handler.registerHandler( "classpath", new ClasspathSchemeHandler( this.getClass().getClassLoader() ) );
            addSearchPathsFromSettingsFile( handler, "classpath", getClass().getResourceAsStream( "jsmodules.settings" ) );

            return handler;
        }
        catch( IOException ex ) {
            logger.warn( "Unable to load properties from resource 'jsmodules.settings'" );
            logger.error( ex.getMessage(), ex );

            return null;
        }
        finally {
            logger.exit();
        }
    }

    private void addSearchPathsFromSettingsFile( ModuleHandler handler, String schemeName, String modulesDir ) {
        logger.entry( handler, schemeName, modulesDir );

        String fileName = modulesDir + "/settings.properties";
        try {
            File file = new File( fileName );

            addSearchPathsFromSettingsFile( handler, schemeName, new FileInputStream( file ) );
        }
        catch( FileNotFoundException ex ) {
            logger.warn( "The file '{}' does not exist.", fileName );
            return;
        }
        catch( IOException ex ) {
            logger.warn( "Unable to load properties from file '{}'", fileName );
            logger.error( ex.getMessage(), ex );
        }
        finally {
            logger.exit();
        }
    }

    private void addSearchPathsFromSettingsFile( ModuleHandler handler, String schemeName, InputStream is ) throws IOException {
        logger.entry( handler, schemeName, is );

        try {
            Properties props = new Properties();
            props.load( is );

            if( !props.containsKey( "modules.search.path" ) ) {
                logger.warn( "Search path for modules is not specified" );
                return;
            }

            String moduleSearchPathString = props.getProperty( "modules.search.path" );
            if( moduleSearchPathString != null && !moduleSearchPathString.isEmpty() ) {
                String[] moduleSearchPath = moduleSearchPathString.split( ";" );
                for( String s : moduleSearchPath ) {
                    handler.addSearchPath( new SchemeURI( schemeName + ":" + s ) );
                }
            }
        }
        finally {
            logger.exit();
        }
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger logger = XLoggerFactory.getXLogger( ServiceScripter.class );

    private static final String COMMON_INSTALL_NAME = "common";
    private static final String MODULES_PATH_PATTERN = "%s/distributions/%s/src";
    private static final String ENTRYPOINTS_PATTERN = MODULES_PATH_PATTERN + "/entrypoints/%s/%s";

    /**
     * Base directory of the Opencat-Business installation.
     */
    private String baseDir;

    /**
     * Name of the distribution to use.
     */
    private String distributionName;

    /**
     * Name of the service that is using this Scripter.
     */
    private String serviceName;

    /**
     * @brief Map of Environment for our Rhino JavaScript engine.
     */
    private Map<String, Environment> environments = new HashMap<>();
}
