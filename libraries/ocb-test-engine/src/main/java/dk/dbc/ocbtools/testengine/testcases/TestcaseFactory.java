//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------

import dk.dbc.iscrum.utils.json.Json;
import dk.dbc.ocbtools.commons.filesystem.SystemTest;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.IOException;
import java.util.List;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 15/02/15.
 */
public class TestcaseFactory {
    //-------------------------------------------------------------------------
    //              Loaders
    //-------------------------------------------------------------------------

    public static List<Testcase> newInstances( SystemTest systemTest ) throws IOException {
        logger.entry();

        List<Testcase> result = null;
        try {
            result = Json.decodeArray( systemTest.getFile(), Testcase.class );
            for( Testcase tc : result ) {
                tc.setDistributionName( systemTest.getDistributionName() );
                tc.setFile( systemTest.getFile() );
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
