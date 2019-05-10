package dk.dbc.ocbtools.testengine.testcases;

import java.util.Map;

public class UpdateTestcaseRequest {
    private String templateName = null;
    private TestcaseAuthentication authentication = new TestcaseAuthentication();
    private Map<String, Object> headers = null;
    private String record = null;
    private Boolean check001cd = false;
    private Boolean matchd09 = true;

    public UpdateTestcaseRequest() {
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public TestcaseAuthentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(TestcaseAuthentication authentication) {
        this.authentication = authentication;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    public boolean isCheck001cd() {
        return check001cd;
    }

    public boolean isMatchd09() {
        return matchd09;
    }

    public void setCheck001cd(boolean check001cd) {
        this.check001cd = check001cd;
    }

    @Override
    public String toString() {
        return "UpdateTestcaseRequest{" +
                "templateName='" + templateName + '\'' +
                ", authentication=" + authentication +
                ", headers=" + headers +
                ", record='" + record + '\'' +
                ", check001cd=" + check001cd +
                ", matchd09=" + matchd09 +
                '}';
    }
}
