//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//-----------------------------------------------------------------------------
/**
 * Repository of Testcases load from different files.
 */
public class TestcaseRepository {
    public TestcaseRepository() {
        this.testcases = new ArrayList<>();
    }

    //-------------------------------------------------------------------------
    //              Interface
    //-------------------------------------------------------------------------

    public void addAll( Collection<Testcase> collection ) {
        testcases.addAll( collection );
    }

    public List<Testcase> findAll() {
        return testcases;
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger logger = XLoggerFactory.getXLogger( TestcaseRepository.class );

    private List<Testcase> testcases;
}
