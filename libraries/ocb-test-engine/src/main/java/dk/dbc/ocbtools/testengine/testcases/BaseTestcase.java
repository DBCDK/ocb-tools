package dk.dbc.ocbtools.testengine.testcases;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.File;

public class BaseTestcase {
    protected String name;

    @JsonIgnore
    String distributionName;

    // The file that this Testcase was created from. It may be null.
    @JsonIgnore
    protected File file;

    protected String description;

    BaseTestcase() {
        this.name = "";
        this.distributionName = "";
        this.file = null;
        this.description = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDistributionName() {
        return distributionName;
    }

    void setDistributionName(String distributionName) {
        this.distributionName = distributionName;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "BaseTestcase{" +
                "name='" + name + '\'' +
                ", distributionName='" + distributionName + '\'' +
                ", file=" + file +
                ", description='" + description + '\'' +
                '}';
    }
}
