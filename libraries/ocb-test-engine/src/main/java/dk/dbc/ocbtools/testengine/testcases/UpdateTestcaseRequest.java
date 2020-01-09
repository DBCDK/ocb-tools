package dk.dbc.ocbtools.testengine.testcases;

import java.util.Map;

public class UpdateTestcaseRequest {
    private String templateName = null;
    private TestcaseAuthentication authentication = new TestcaseAuthentication();
    private Map<String, Object> headers = null;
    private String record = null;
    private String restType = "";
    private Boolean check001c = false;
    private Boolean check001d = false;
    private String ignoreFieldsInMatch = "";

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

    public String getRestType() {
        return restType;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    public boolean isCheck001c() {
        return check001c;
    }
    public void setCheck001c(boolean check001c) {
        this.check001c = check001c;
    }

    public boolean isCheck001d() {
        return check001d;
    }

    public void setCheck001d(boolean check001d) {
        this.check001d = check001d;
    }
    public String getIgnoreFieldsInMatch() {
        return ignoreFieldsInMatch;
    }

    @Override
    public String toString() {
        return "UpdateTestcaseRequest{" +
                "templateName='" + templateName + '\'' +
                ", authentication=" + authentication +
                ", headers=" + headers +
                ", record='" + record + '\'' +
                ", check001c=" + check001c +
                ", check001d=" + check001d +
                ", ignoreFieldsInMatch='" + ignoreFieldsInMatch + '\'' +
                ", restType='" + restType + '\'' +
                '}';
    }
}
