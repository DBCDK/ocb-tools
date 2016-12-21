package dk.dbc.ocbtools.commons.filesystem;

import dk.dbc.iscrum.records.MarcRecord;
import dk.dbc.iscrum.records.MarcRecordFactory;
import dk.dbc.iscrum.utils.IOUtils;
import dk.dbc.ocbtools.commons.type.ApplicationType;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class OCBFileSystem {
    private static final XLogger logger = XLoggerFactory.getXLogger(OCBFileSystem.class);

    private static final String BIN_DIRNAME = "bin";
    private static final String DISTRIBUTIONS_DIRNAME = "distributions";
    private static final String ROOT_DIRECTORY_NAMES[] = {BIN_DIRNAME, DISTRIBUTIONS_DIRNAME};

    private static final String COMMON_DISTRIBUTION_DIRNAME = "common";
    private static final String SYSTEMTESTS_DIRNAME = "system-tests";

    private static final String SYSTEMTESTS_DIR_PATTERN = "%s/" + DISTRIBUTIONS_DIRNAME + "/%s/" + SYSTEMTESTS_DIRNAME + "/%s/";
    private static final String SYSTEMTESTS_FILE_EXT = ".json";

    private File baseDir;
    private ApplicationType applicationType;

    public OCBFileSystem(ApplicationType applicationType) throws IOException {
        this(".", applicationType);
    }

    public OCBFileSystem(String path, ApplicationType applicationType) throws IOException {
        this(new File(path).getAbsoluteFile(), applicationType);
    }

    public OCBFileSystem(File file, ApplicationType applicationType) throws IOException {
        this.baseDir = extractBaseDir(file);
        this.applicationType = applicationType;
    }

    public File getBaseDir() {
        return this.baseDir;
    }

    List<String> findDistributions() throws IOException {
        logger.entry();
        ArrayList<String> result = new ArrayList<>();
        try {
            File distributionsDir = new File(baseDir.getCanonicalPath() + "/" + DISTRIBUTIONS_DIRNAME);
            for (File file : distributionsDir.listFiles(new FileIgnoreFilter(COMMON_DISTRIBUTION_DIRNAME, ".svn"))) {
                if (file.isDirectory()) {
                    result.add(file.getName());
                }
            }
            return result;
        } finally {
            logger.exit(result);
        }
    }

    public List<SystemTest> findSystemtests() throws IOException {
        logger.entry();
        List<SystemTest> result = new ArrayList<>();
        try {
            String applicationStr = applicationType.toString().toLowerCase();
            for (String distName : findDistributions()) {
                logger.debug("TESTCASES in {} path : {}", distName, String.format(SYSTEMTESTS_DIR_PATTERN, baseDir.getCanonicalPath(), distName, applicationStr));
                File systemTestsDir = new File(String.format(SYSTEMTESTS_DIR_PATTERN, baseDir.getCanonicalPath(), distName, applicationStr));
                if (systemTestsDir.exists()) {
                    for (File file : findFiles(systemTestsDir, new FileExtensionFilter(SYSTEMTESTS_FILE_EXT))) {
                        logger.debug("Add testcase dist,file,type {},{},{}", distName, file, applicationType);
                        result.add(new SystemTest(distName, file, applicationType));
                    }
                }
            }
            return result;
        } finally {
            logger.exit(result);
        }
    }

    /**
     * Loads a MarcRecord from a file
     */
    public MarcRecord loadRecord(File baseDir, String filename) throws IOException {
        logger.entry();
        try {
            if (baseDir == null) {
                throw new IllegalArgumentException("baseDir can not be (null)");
            }
            if (!baseDir.isDirectory()) {
                return null;
            }
            File recordFile = new File(baseDir.getCanonicalPath() + "/" + filename);
            FileInputStream fis = new FileInputStream(recordFile);
            return MarcRecordFactory.readRecord(IOUtils.readAll(fis, "UTF-8"));
        } finally {
            logger.exit();
        }
    }

    /**
     * Loads a properties file from the baseDir of this file system.
     */
    public Properties loadSettings(String name) throws IOException {
        logger.entry(name);
        Properties props = null;
        try {
            String[] filenames = {
                    System.getProperty("user.home") + "/.ocb-tools/" + name + ".properties",
                    baseDir.getCanonicalFile() + "/etc/" + name + ".properties"
            };
            for (String filename : filenames) {
                File file = new File(filename);
                logger.debug("Checking settings from '{}'", file.getCanonicalPath());
                if (file.exists()) {
                    return props = loadSettings(file);
                }
            }
            return props = null;
        } finally {
            logger.exit(props);
        }
    }

    /**
     * Loads properties from a File instance.
     */
    private Properties loadSettings(File file) throws IOException {
        logger.entry(file);
        Properties props = new Properties();
        try {
            logger.debug("Loads settings from '{}'", file.getCanonicalPath());
            FileInputStream fileInputStream = new FileInputStream(file);
            props.load(fileInputStream);

            return props;
        } finally {
            logger.exit(props);
        }
    }

    /**
     * Extracts the base directory of the Opencat-business directory.
     *
     * @param file A File instance with the path of a directory.
     * @return The base directory.
     */
    private static File extractBaseDir(File file) throws IOException {
        logger.entry(file);
        try {
            if (file == null) {
                return null;
            }
            if (!file.isDirectory()) {
                return null;
            }
            int directoriesFound = 0;
            for (String name : file.list()) {
                if (Arrays.binarySearch(ROOT_DIRECTORY_NAMES, name) > -1) {
                    directoriesFound++;
                }
            }
            if (directoriesFound == ROOT_DIRECTORY_NAMES.length) {
                logger.debug("Found base dir at {}", file.getCanonicalPath());
                return file;
            }
            return extractBaseDir(file.getParentFile());
        } finally {
            logger.exit();
        }
    }

    private List<File> findFiles(File dir, FilenameFilter filter) throws IOException {
        logger.entry();
        List<File> result = new ArrayList<>();
        try {
            if (!dir.exists()) {
                return result;
            }
            result.addAll(Arrays.asList(dir.listFiles(filter)));
            File[] subDirs = dir.listFiles(pathname -> {
                return pathname.isDirectory();
            });
            for (File subDir : subDirs) {
                result.addAll(findFiles(subDir, filter));
            }
            return result;
        } finally {
            logger.exit(result);
        }
    }
}
