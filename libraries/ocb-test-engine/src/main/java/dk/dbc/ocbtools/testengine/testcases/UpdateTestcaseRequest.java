package dk.dbc.ocbtools.testengine.testcases;

import java.util.Map;

/**
 * Created by stp on 16/02/15.
 */
public class UpdateTestcaseRequest {
    private String templateName = null;
    private TestcaseAuthentication authentication = new TestcaseAuthentication();
    private Map<String, Object> headers = null;
    private String record = null;

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

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    @Override
    public String toString() {
        return "TestcaseRequest{" +
                "templateName='" + templateName + '\'' +
                ", authentication=" + authentication +
                ", headers=" + headers +
                ", record='" + record + '\'' +
                '}';
    }
}
