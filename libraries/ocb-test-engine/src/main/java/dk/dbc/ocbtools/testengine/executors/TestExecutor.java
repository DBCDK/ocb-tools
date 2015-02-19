//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.executors;

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
/**
 * Defines the interface to execute a Testcase.
 */
public interface TestExecutor {
    public void setup();
    public void teardown();
    public void executeTests();
}
