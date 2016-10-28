package dk.dbc.ocbtools.testengine.executors;

import dk.dbc.holdingsitems.HoldingsItemsException;
import dk.dbc.rawrepo.RawRepoException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Defines the interface to execute a Testcase.
 */
public interface TestExecutor {
    /**
     * @return The name of this test excutor.
     */
    String name();

    /**
     * Setup the testcase.
     * <p/>
     * This method is called before the tests are run.
     */
    boolean setup() throws IOException, JAXBException, SQLException, RawRepoException, HoldingsItemsException, ClassNotFoundException;

    /**
     * Tear down the testcase.
     * <p/>
     * This method is called after the tests are run.
     */
    void teardown();

    /**
     * Runs all tests.
     */
    void executeTests();
}
