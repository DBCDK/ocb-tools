//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------
import dk.dbc.ocbtools.commons.filesystem.OCBFileSystem;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
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
            for( File file : fs.findSystemtests() ) {
                result.addAll( TestcaseFactory.newInstancesFromFile( file ) );
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
