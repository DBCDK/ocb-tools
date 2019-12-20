package dk.dbc.ocbtools.commons.filesystem;

import dk.dbc.ocbtools.commons.type.ApplicationType;

import java.io.File;

public class SystemTest implements Comparable<SystemTest> {
    private String distributionName;
    private File file;
    private ApplicationType applicationType;

    public SystemTest(String distributionName, File file, ApplicationType applicationType) {
        this.distributionName = distributionName;
        this.file = file;
        this.applicationType = applicationType;
    }

    public String getDistributionName() {
        return distributionName;
    }

    public File getFile() {
        return file;
    }

    @Override
    public int compareTo(SystemTest o) {
        return file.compareTo(o.getFile());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SystemTest that = (SystemTest) o;

        if (distributionName != null ? !distributionName.equals(that.distributionName) : that.distributionName != null) {
            return false;
        }
        if (file != null ? !file.equals(that.file) : that.file != null) {
            return false;
        }
        if (applicationType != null ? !(applicationType == that.applicationType) : that.applicationType != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = distributionName != null ? distributionName.hashCode() : 0;
        result = 31 * result + (file != null ? file.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("{distributionName:%s, file:%s}", distributionName, file);
    }
}
