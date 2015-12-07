package dk.dbc.ocbtools.ocbrecord;

import dk.dbc.ocbtools.commons.cli.CliExecutor;

/**
 * This contains the main function for the command line tool ocb-record
 */
public class OCBRecord {
    public static void main( String[] args ) {
        System.exit( CliExecutor.main( "ocb-record", args ) );
    }
}
