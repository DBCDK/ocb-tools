package dk.dbc.ocbtools.ocbtest;

import dk.dbc.ocbtools.commons.api.Subcommand;
import dk.dbc.ocbtools.commons.api.SubcommandDefinition;
import dk.dbc.ocbtools.commons.api.SubcommandExecutor;
import dk.dbc.ocbtools.commons.cli.CliException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines a subcommand to list testcases.
 */
@Subcommand(name = "list",
        description = "Lister de testcases på tværs af filer, som evt. matcher et regulært udtryk.",
        usage = "[regex]")
public class ListDefinition implements SubcommandDefinition {
    private static final XLogger logger = XLoggerFactory.getXLogger(ListDefinition.class);

    @Override
    public List<Option> createOptions() {
        List<Option> options = new ArrayList<>();
        return options;
    }

    @Override
    public SubcommandExecutor createExecutor(File baseDir, CommandLine line) throws CliException {
        logger.entry(baseDir, line);
        try {
            ListExecutor listExecutor = new ListExecutor(baseDir);
            listExecutor.setMatchExpressions(line.getArgList());
            listExecutor.setApplicationType(CommonMethods.parseApplicationType(line));
            return listExecutor;
        } finally {
            logger.exit();
        }
    }
}
