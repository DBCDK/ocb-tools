//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//-----------------------------------------------------------------------------

/**
 * Repository of Testcases load from different files.
 */
public class UpdateTestcaseRepository {
    private List<UpdateTestcase> updateTestcases;

    public UpdateTestcaseRepository() {
        this.updateTestcases = new ArrayList<>();
    }

    //-------------------------------------------------------------------------
    //              Interface
    //-------------------------------------------------------------------------

    public void addAll(Collection<UpdateTestcase> collection) {
        updateTestcases.addAll(collection);
    }

    public List<UpdateTestcase> findAllTestcases() {
        return updateTestcases;
    }

    public List<String> findAllTestcaseNames() {
        List<String> names = new ArrayList<>();
        for (UpdateTestcase uc : updateTestcases) {
            names.add(uc.getName());
        }
        return names;
    }

}
