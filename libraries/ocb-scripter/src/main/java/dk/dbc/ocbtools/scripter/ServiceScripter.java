package dk.dbc.ocbtools.scripter;

import dk.dbc.jslib.ClasspathSchemeHandler;
import dk.dbc.jslib.Environment;
import dk.dbc.jslib.FileSchemeHandler;
import dk.dbc.jslib.ModuleHandler;
import dk.dbc.jslib.SchemeURI;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ServiceScripter {
    private static final XLogger logger = XLoggerFactory.getXLogger(ServiceScripter.class);
    private static final String COMMON_INSTALL_NAME = "file";
    private static final String COMMON_DISTRIBUTION_PATH = "distributions/common";
    private static final String MODULES_PATH_PATTERN = "%s/%s/src";
    private static final String ENTRYPOINTS_PATTERN = MODULES_PATH_PATTERN + "/entrypoints/%s/%s";

    private String baseDir = "";
    private List<Distribution> distributions = null;
    private String modulesKey = "";
    private String serviceName = "";
    private Map<String, Environment> environments = new HashMap<>();

    public ServiceScripter() {
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public void setDistributions(List<Distribution> distributions) {
        this.distributions = distributions;
    }

    public void setModulesKey(String modulesKey) {
        this.modulesKey = modulesKey;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Calls a function in a JavaScript environment and returns the result.
     * <p/>
     * The JavaScript environment is created and cached by the filename.
     *
     * @param fileName   JavaScript file to load in the environment.
     * @param methodName Name of the function to call.
     * @param args       Arguments to the function.
     * @return The result of the JavaScript function.
     * @throws ScripterException Encapsulate any exception from Rhino or is throwned
     *                           in case of an error. For instance if the file can not be loaded.
     */
    public Object callMethod(String fileName, String methodName, Object... args) throws ScripterException {
        logger.entry(fileName, methodName, args);
        Object result = null;
        try {
            if (!environments.containsKey(fileName)) {
                environments.put(fileName, createEnvironment(fileName));
            }
            Environment envir = environments.get(fileName);
            result = envir.callMethod(methodName, args);
            return result;
        } catch (Exception e) {
            throw new ScripterException(e.getMessage(), e);
        } finally {
            logger.exit(result);
        }
    }

    /**
     * Returns a list of completes path to any module paths used by the environment.
     * <p/>
     * Only paths to external files are returned.
     *
     * @return List of module paths.
     */
    public List<String> getModulePaths() throws IOException {
        logger.entry();
        try {
            ArrayList<String> modulePaths = new ArrayList<>();
            String[] searchPaths = createModulesHandler().getSearchPaths().split(" ");
            logger.debug("Search paths: {}", searchPaths);
            for (String searchPath : searchPaths) {
                String[] path = searchPath.split(":");
                if (path.length == 2) {
                    String pathType = path[0];
                    String pathDir = path[1];
                    File file;
                    String modulesDir;
                    if (pathType.equals(COMMON_INSTALL_NAME)) {
                        modulesDir = String.format(MODULES_PATH_PATTERN, baseDir, COMMON_DISTRIBUTION_PATH);
                        file = new File(modulesDir + "/" + pathDir);
                        if (file.isDirectory()) {
                            logger.debug("Adding module path: {}", file.getCanonicalPath());
                            modulePaths.add(file.getCanonicalPath());
                        }
                    } else {
                        for (Distribution dist : distributions) {
                            modulesDir = String.format(MODULES_PATH_PATTERN, baseDir, dist.getDirName());
                            file = new File(modulesDir + "/" + pathDir);
                            if (file.isDirectory()) {
                                logger.debug("Adding module path: {}", file.getCanonicalPath());
                                modulePaths.add(file.getCanonicalPath());
                            }
                        }
                    }
                }
            }
            return modulePaths;
        } finally {
            logger.exit();
        }
    }

    /**
     * Constructs a new Environment from a file.
     *
     * @param fileName The name of the file to load into the new Environment.
     * @return The new Environment.
     * @throws ScripterException Throwed in case of I/O errors.
     */
    private Environment createEnvironment(String fileName) throws ScripterException {
        logger.entry(fileName);
        String jsFileName = "";
        try {
            Environment envir = new Environment();
            envir.registerUseFunction(createModulesHandler());

            for (Distribution dist : distributions) {
                jsFileName = String.format(ENTRYPOINTS_PATTERN, baseDir, dist.getDirName(), serviceName, fileName);
                logger.debug("Calculated js filename: {}", jsFileName);

                File file = new File(jsFileName);
                if (file.exists() && file.isFile()) {
                    logger.info("Trying to evaluate {} in the new JavaScript Environment", jsFileName);
                    envir.evalFile(jsFileName);

                    return envir;
                }
            }
            throw new ScripterException("Unable to find an environment for file %s", fileName);
        } catch (Exception e) {
            logger.error("Unable to load file {}: {}", jsFileName, e.getMessage());
            throw new ScripterException(e.getMessage(), e);
        } finally {
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

            for (Distribution dist : distributions) {
                modulesDir = String.format(MODULES_PATH_PATTERN, baseDir, dist.getDirName());
                logger.debug("Adding distribution to module handler: {} -> {}", dist.getSchemaName(), modulesDir);
                handler.registerHandler(dist.getSchemaName(), new FileSchemeHandler(modulesDir));
                addSearchPathsFromSettingsFile(handler, dist.getSchemaName(), modulesDir);
            }

            modulesDir = String.format(MODULES_PATH_PATTERN, baseDir, COMMON_DISTRIBUTION_PATH);
            logger.debug("Adding distribution to module handler: {} -> {}", COMMON_INSTALL_NAME, modulesDir);
            handler.registerHandler(COMMON_INSTALL_NAME, new FileSchemeHandler(modulesDir));
            addSearchPathsFromSettingsFile(handler, COMMON_INSTALL_NAME, modulesDir);

            handler.registerHandler("classpath", new ClasspathSchemeHandler(this.getClass().getClassLoader()));
            addSearchPathsFromSettingsFile(handler, "classpath", getClass().getResourceAsStream("jsmodules.settings"));
            return handler;
        } catch (IOException e) {
            logger.warn("Unable to load properties from resource 'jsmodules.settings'");
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            logger.exit();
        }
    }

    private void addSearchPathsFromSettingsFile(ModuleHandler handler, String schemeName, String modulesDir) {
        logger.entry(handler, schemeName, modulesDir);
        String fileName = modulesDir + "/settings.properties";
        try {
            File file = new File(fileName);
            addSearchPathsFromSettingsFile(handler, schemeName, new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            logger.warn("The file '{}' does not exist.", fileName);
        } catch (IOException ex) {
            logger.warn("Unable to load properties from file '{}'", fileName);
            logger.error(ex.getMessage(), ex);
        } finally {
            logger.exit();
        }
    }

    private void addSearchPathsFromSettingsFile(ModuleHandler handler, String schemeName, InputStream is) throws IOException {
        logger.entry(handler, schemeName, is);
        try {
            Properties props = new Properties();
            props.load(is);
            if (!props.containsKey(modulesKey)) {
                logger.warn("Search path for modules is not specified");
                return;
            }
            String moduleSearchPathString = props.getProperty(modulesKey);
            if (moduleSearchPathString != null && !moduleSearchPathString.isEmpty()) {
                String[] moduleSearchPath = moduleSearchPathString.split(";");
                for (String s : moduleSearchPath) {
                    handler.addSearchPath(new SchemeURI(schemeName + ":" + s));
                }
            }
        } finally {
            logger.exit();
        }
    }
}
