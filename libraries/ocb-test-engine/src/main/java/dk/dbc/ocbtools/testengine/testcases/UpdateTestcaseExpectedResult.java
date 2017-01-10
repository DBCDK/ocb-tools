package dk.dbc.ocbtools.testengine.testcases;

/**
 * Defines the expected result of a update testcase in json.
 */
public class UpdateTestcaseExpectedResult {
    private UpdateTestcaseExpectedValidateResult validation = null;
    private UpdateTestcaseExpectedUpdateResult update = null;

    public UpdateTestcaseExpectedResult() {
    }

    public UpdateTestcaseExpectedValidateResult getValidation() {
        return validation;
    }

    public void setValidation(UpdateTestcaseExpectedValidateResult validation) {
        this.validation = validation;
    }

    public UpdateTestcaseExpectedUpdateResult getUpdate() {
        return update;
    }

    public void setUpdate(UpdateTestcaseExpectedUpdateResult update) {
        this.update = update;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UpdateTestcaseExpectedResult that = (UpdateTestcaseExpectedResult) o;

        if (validation != null ? !validation.equals(that.validation) : that.validation != null) return false;
        return update != null ? update.equals(that.update) : that.update == null;

    }

    @Override
    public int hashCode() {
        int result = validation != null ? validation.hashCode() : 0;
        result = 31 * result + (update != null ? update.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UpdateTestcaseExpectedResult{" +
                "validation=" + validation +
                ", update=" + update +
                '}';
    }
}
