package dk.dbc.ocbtools.testengine.runners;

import dk.dbc.ocbtools.testengine.executors.TestExecutor;
import dk.dbc.ocbtools.testengine.testcases.BuildTestcase;

import java.util.List;

public class BuildTestRunnerItem {
    private BuildTestcase buildTestcase;
    private List<TestExecutor> executors;

    public BuildTestRunnerItem(BuildTestcase buildTestcase, List<TestExecutor> executors) {
        this.buildTestcase = buildTestcase;
        this.executors = executors;
    }

    BuildTestcase getBuildTestcase() {
        return buildTestcase;
    }

    public List<TestExecutor> getExecutors() {
        return executors;
    }

    public void setExecutors(List<TestExecutor> executors) {
        this.executors = executors;
    }

    @Override
    public String toString() {
        return "BuildTestRunnerItem{" +
                "buildTestcase=" + buildTestcase +
                ", executors=" + executors +
                '}';
    }
}
