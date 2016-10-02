package dk.dbc.ocbtools.ocbtest;

import dk.dbc.iscrum.utils.ResourceBundles;
import dk.dbc.ocbtools.commons.cli.CliException;
import dk.dbc.ocbtools.commons.type.ApplicationType;
import org.apache.commons.cli.CommandLine;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.util.ResourceBundle;

class CommonMethods {
    private static final XLogger logger = XLoggerFactory.getXLogger(CommonMethods.class);

    static ApplicationType parseApplicationType(CommandLine line) throws CliException {
        logger.entry();
        ApplicationType res = null;
        try {
            String input = line.getOptionValue("a").toUpperCase();
            switch (input) {
                case "B":
                case "BUILD":
                    res = ApplicationType.BUILD;
                    break;
                case "U":
                case "UPDATE":
                    res = ApplicationType.UPDATE;
                    break;
                default:
                    ResourceBundle bundle = ResourceBundles.getBundle("commons_error");
                    throw new CliException(bundle.getString("commons.application.arg.error"));
            }
            return res;
        } finally {
            logger.exit(res);
        }
    }
}
