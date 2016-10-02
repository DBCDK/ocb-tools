package dk.dbc.ocbtools.testengine.runners;

import dk.dbc.ocbtools.testengine.executors.TestExecutor;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcase;

import java.util.List;

public class UpdateTestRunnerItem {
    private UpdateTestcase updateTestcase;
    private List<TestExecutor> executors;

    public UpdateTestRunnerItem(UpdateTestcase updateTestcase, List<TestExecutor> executors) {
        this.updateTestcase = updateTestcase;
        this.executors = executors;
    }

    UpdateTestcase getUpdateTestcase() {
        return updateTestcase;
    }

    public void setUpdateTestcase(UpdateTestcase updateTestcase) {
        this.updateTestcase = updateTestcase;
    }

    public List<TestExecutor> getExecutors() {
        return executors;
    }

    public void setExecutors(List<TestExecutor> executors) {
        this.executors = executors;
    }
}
