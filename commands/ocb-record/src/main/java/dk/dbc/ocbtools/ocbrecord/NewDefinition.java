package dk.dbc.ocbtools.ocbrecord;

import dk.dbc.iscrum.utils.logback.filters.BusinessLoggerFilter;
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


/**
 * Created by thl on 2/19/15.
 */
@Subcommand( name = "new",
             description = "Opret en ny post.",
             usage = "<parametre>" )
public class NewDefinition implements SubcommandDefinition {

    private static final XLogger output = XLoggerFactory.getXLogger( BusinessLoggerFilter.LOGGER_NAME );

    @Override
    public List<Option> createOptions() {
        List<Option> options = new ArrayList<>();
        Option option;
        option = new Option( "o", "output", true, "Filnavn hvor output skal skrives i. Eks. <testcase.xml>. Hvis denne parameter ikke angives skrives output til skærmen." );
        options.add( option );
        option = new Option( "d", "distribution", true, "Distribution der skal anvendes. Obligatorisk værdi." );
        option.setRequired( true );
        options.add( option );
        option = new Option( "s", "skabelon", true, "Skabelonen der skal bruges. Obligatorisk værdi." );
        option.setRequired( true );
        options.add( option );
        option = new Option( "f", "format", true, "Format i et af følgende tilladte værdier: MARC, MarcXchange, JSON. Hvis denne parameter ikke angives antages MARC." );
        options.add( option );
        option = new Option( "i", "input", true, "Fil der skal bruges som base for ny post. Hvis denne parameter ikke angives oprettes en ny post ud fra den angivne skabelon." );
        options.add( option );
        option = new Option( "c", "charset", true, "Fil kodning. Hvis denne parameter ikke angives antages UTF8. Mulige værdier: UTF8, ISO8859." );
        options.add( option );
        return options;
    }

    @Override
    public SubcommandExecutor createExecutor( File baseDir, CommandLine line ) {
        output.entry( baseDir, line );
        try {
            OCBRecordData ocbRecordData = new OCBRecordData();
            ocbRecordData.setBaseDir( baseDir );
            for ( Option o : line.getOptions() ) {
                if ( "d".equalsIgnoreCase( o.getOpt() ) ) {
                    ocbRecordData.setDistribution( o.getValue() );
                }
                if ( "o".equalsIgnoreCase( o.getOpt() ) ) {
                    ocbRecordData.setOutputFile( o.getValue() );
                }
                if ( "s".equalsIgnoreCase( o.getOpt() ) ) {
                    ocbRecordData.setTemplate( o.getValue() );
                }
                if ( "f".equalsIgnoreCase( o.getOpt() ) ) {
                    ocbRecordData.setFormat( o.getValue() );
                }
                if ( "i".equalsIgnoreCase( o.getOpt() ) ) {
                    ocbRecordData.setInputFile( o.getValue() );
                }
                if ( "c".equalsIgnoreCase( o.getOpt() ) ) {
                    ocbRecordData.setInputEncoding( o.getValue() );
                }
                output.trace( "Fandt parameteren: " + o.getOpt() + " og værdi: " + o.getValue() );
            }
            return new NewExecutor( ocbRecordData );
        } finally {
            output.exit();
        }
    }
}
