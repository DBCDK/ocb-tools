package dk.dbc.ocbtools.ocbrecord;

import dk.dbc.ocbtools.commons.api.Subcommand;
import dk.dbc.ocbtools.commons.api.SubcommandDefinition;
import dk.dbc.ocbtools.commons.api.SubcommandExecutor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Subcommand(name = "new",
        description = "Opret en ny post.",
        usage = "<parametre>")
public class NewDefinition implements SubcommandDefinition {

    private static final XLogger output = XLoggerFactory.getXLogger(NewDefinition.class);

    @Override
    public List<Option> createOptions() {
        List<Option> options = new ArrayList<>();
        Option option;
        option = new Option("c", "charset", true, "Fil kodning. Hvis denne parameter ikke angives antages UTF8. Mulige værdier: UTF8, LATIN1. Ignoreres hvis remote værdien sættes.");
        options.add(option);
        option = new Option("d", "distribution", true, "Distribution der skal anvendes. Obligatorisk værdi.");
        option.setRequired(true);
        options.add(option);
        option = new Option("f", "format", true, "Format i et af følgende tilladte værdier: MARC, MarcXchange, JSON. Hvis denne parameter ikke angives antages MARC.");
        options.add(option);
        option = new Option("i", "input", true, "Fil der skal bruges som base for ny post. Hvis denne parameter ikke angives oprettes en ny post ud fra den angivne skabelon. Ignoreres hvis remote værdien sættes.");
        options.add(option);
        option = new Option("o", "output", true, "Filnavn hvor output skal skrives i. Eks. <testcase.xml>. Hvis denne parameter ikke angives skrives output til skærmen.");
        options.add(option);
        option = new Option("r", "remote", false, "Hvis dette flag er sat bruges Build webservicen i stedet for de indlejrede javascripts.");
        options.add(option);
        option = new Option("s", "skabelon", true, "Skabelonen der skal bruges. Obligatorisk værdi.");
        option.setRequired(true);
        options.add(option);
        return options;
    }

    @Override
    public SubcommandExecutor createExecutor(File baseDir, CommandLine line) {
        output.entry(baseDir, line);
        try {
            OCBRecordData ocbRecordData = new OCBRecordData();
            ocbRecordData.setBaseDir(baseDir);
            for (Option o : line.getOptions()) {
                if ("c".equalsIgnoreCase(o.getOpt())) {
                    ocbRecordData.setInputEncoding(o.getValue());
                }
                if ("d".equalsIgnoreCase(o.getOpt())) {
                    ocbRecordData.setDistribution(o.getValue());
                }
                if ("f".equalsIgnoreCase(o.getOpt())) {
                    ocbRecordData.setFormat(o.getValue());
                }
                if ("i".equalsIgnoreCase(o.getOpt())) {
                    ocbRecordData.setInputFile(o.getValue());
                }
                if ("o".equalsIgnoreCase(o.getOpt())) {
                    ocbRecordData.setOutputFile(o.getValue());
                }
                if ("r".equalsIgnoreCase(o.getOpt())) {
                    ocbRecordData.setRemote(true);
                }
                if ("s".equalsIgnoreCase(o.getOpt())) {
                    ocbRecordData.setTemplate(o.getValue());
                }
                output.trace("Fandt parameteren: " + o.getOpt() + " og værdi: " + o.getValue());
            }
            return new NewExecutor(ocbRecordData);
        } finally {
            output.exit();
        }
    }
}
