//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------

import dk.dbc.iscrum.utils.json.Json;
import dk.dbc.ocbtools.commons.filesystem.SystemTest;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

//-----------------------------------------------------------------------------
/**
 * Testcase factory to create Testcase instances from json files.
 */
public class TestcaseFactory {
    //-------------------------------------------------------------------------
    //              Loaders
    //-------------------------------------------------------------------------

    public static List<Testcase> newInstances( SystemTest systemTest ) throws IOException {
        logger.entry( systemTest );

        List<Testcase> result = null;
        try {
            result = Json.decodeArray( systemTest.getFile(), Testcase.class );
            for( Testcase tc : result ) {
                tc.setDistributionName( systemTest.getDistributionName() );
                tc.setFile( systemTest.getFile() );

                if( tc.getSetup() != null && tc.getSetup().getRawrepo() != null ) {
                    for( TestcaseRecord testcaseRecord : tc.getSetup().getRawrepo() ) {
                        testcaseRecord.setRecordFile( new File( tc.getFile().getParent() + "/" + testcaseRecord.getRecord() ) );
                    }
                }

                if( tc.getExpected() != null && tc.getExpected().getUpdate() != null && tc.getExpected().getUpdate().getRawrepo() != null ) {
                    for( TestcaseRecord testcaseRecord : tc.getExpected().getUpdate().getRawrepo() ) {
                        testcaseRecord.setRecordFile( new File( tc.getFile().getParent() + "/" + testcaseRecord.getRecord() ) );
                    }
                }
            }

            return result;
        }
        finally {
            logger.exit();
        }
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger logger = XLoggerFactory.getXLogger( TestcaseRepositoryFactory.class );
}
