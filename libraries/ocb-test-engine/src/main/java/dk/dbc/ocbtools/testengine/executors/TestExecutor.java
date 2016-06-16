//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.executors;

//-----------------------------------------------------------------------------
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
    boolean setup();

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
