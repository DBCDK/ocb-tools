//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------

import dk.dbc.ocbtools.commons.filesystem.OCBFileSystem;
import dk.dbc.ocbtools.commons.filesystem.SystemTest;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.IOException;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 14/02/15.
 */
public class TestcaseRepositoryFactory {
    //-------------------------------------------------------------------------
    //              Loaders
    //-------------------------------------------------------------------------

    public static TestcaseRepository newInstanceWithTestcases( OCBFileSystem fs ) throws IOException {
        logger.entry();

        TestcaseRepository result = new TestcaseRepository();
        try {
            for( SystemTest systemTest : fs.findSystemtests() ) {
                result.addAll( TestcaseFactory.newInstances( systemTest ) );
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
