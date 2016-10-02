package dk.dbc.ocbtools.testengine.testcases;

public class TestcaseAuthentication {
    private String group;
    private String user;
    private String password;

    public TestcaseAuthentication() {
        group = "";
        user = "";
        password = "";
    }

    public String getGroup() {
        return group;
    }

    public void setGroup( String group ) {
        this.group = group;
    }

    public String getUser() {
        return user;
    }

    public void setUser( String user ) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword( String password ) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "TestcaseAuthentication{" +
                "group='" + group + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
