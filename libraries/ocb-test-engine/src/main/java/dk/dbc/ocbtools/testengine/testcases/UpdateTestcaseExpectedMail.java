package dk.dbc.ocbtools.testengine.testcases;

/**
 * Defines the expected part of a mail send by Update.
 */
public class UpdateTestcaseExpectedMail {
    private String subject;
    private String body;

    public UpdateTestcaseExpectedMail() {
        this.subject = "";
        this.body = "";
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "UpdateTestcaseExpectedMail{" +
                "subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
