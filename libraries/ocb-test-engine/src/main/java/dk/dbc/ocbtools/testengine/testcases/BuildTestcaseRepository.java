package dk.dbc.ocbtools.testengine.testcases;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Repository of Testcases load from different files.
 */
public class BuildTestcaseRepository {
    private List<BuildTestcase> buildTestcases;

    public BuildTestcaseRepository() {
        this.buildTestcases = new ArrayList<>();
    }

    public void addAll(Collection<BuildTestcase> collection) {
        buildTestcases.addAll(collection);
    }

    public List<BuildTestcase> findAllTestcases() {
        return buildTestcases;
    }

    public List<String> findAllTestcaseNames() {
        List<String> names = new ArrayList<>();
        for (BuildTestcase uc : buildTestcases) {
            names.add(uc.getName());
        }
        return names;
    }
}
