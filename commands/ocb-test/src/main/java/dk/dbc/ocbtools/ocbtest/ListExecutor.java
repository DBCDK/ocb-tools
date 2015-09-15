//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.ocbtest;

//-----------------------------------------------------------------------------

import dk.dbc.iscrum.utils.logback.filters.BusinessLoggerFilter;
import dk.dbc.ocbtools.commons.api.SubcommandExecutor;
import dk.dbc.ocbtools.commons.cli.CliException;
import dk.dbc.ocbtools.commons.filesystem.OCBFileSystem;
import dk.dbc.ocbtools.commons.type.ApplicationType;
import dk.dbc.ocbtools.testengine.testcases.BaseTestcase;
import dk.dbc.ocbtools.testengine.testcases.BuildTestcaseRepository;
import dk.dbc.ocbtools.testengine.testcases.BuildTestcaseRepositoryFactory;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcaseRepository;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcaseRepositoryFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Executor for the list subcommand.
 */
public class ListExecutor implements SubcommandExecutor {
    private static final XLogger logger = XLoggerFactory.getXLogger(BusinessLoggerFilter.LOGGER_NAME);

    private File baseDir;
    private List<String> matchExpressions;
    private ApplicationType applicationType;

    public ListExecutor(File baseDir) {
        this.baseDir = baseDir;
        this.matchExpressions = null;
        this.applicationType = null;
    }

    @Override
    public void actionPerformed() throws CliException {
        logger.entry();

        try {
            logger.debug("Match expressions: {}", matchExpressions);

            List<BaseTestcase> baseTestcases = new ArrayList<>();
            OCBFileSystem fs = new OCBFileSystem(applicationType);
            if (applicationType == ApplicationType.UPDATE) {
                UpdateTestcaseRepository repo = UpdateTestcaseRepositoryFactory.newInstanceWithTestcases(fs);
                for (BaseTestcase tc : repo.findAllTestcases()) {
                    baseTestcases.add(tc);
                }
            } else if (applicationType == ApplicationType.BUILD) {
                BuildTestcaseRepository repo = BuildTestcaseRepositoryFactory.newInstanceWithTestcases(fs);
                for (BaseTestcase tc : repo.findAllTestcases()) {
                    baseTestcases.add(tc);
                }
            } else {
                throw new CliException("Unknown application type");
            }

            for (BaseTestcase tc : baseTestcases) {
                if (!matchAnyExpressions(tc, matchExpressions)) {
                    continue;
                }
                String filename = tc.getFile().getCanonicalPath();
                filename = filename.replace(fs.getBaseDir().getCanonicalPath() + "/", "");

                logger.info("{}:", tc.getName());
                logger.info("    Fil: {}", filename);
                logger.info("    Beskrivelse: {}", tc.getDescription());
            }
        } catch (IOException ex) {
            throw new CliException(ex.getMessage(), ex);
        } finally {
            logger.exit();
        }
    }

    private boolean matchAnyExpressions(BaseTestcase tc, List<String> regexps) {
        logger.entry();

        try {
            if (regexps.isEmpty()) {
                return true;
            }

            for (String regex : regexps) {
                if (tc.getName().matches(regex)) {
                    return true;
                }
            }

            return false;
        } finally {
            logger.exit();
        }
    }

    public List<String> getMatchExpressions() {
        return matchExpressions;
    }

    public void setMatchExpressions(List<String> matchExpressions) {
        this.matchExpressions = matchExpressions;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }
}
