//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------
/**
 * Defines the expected part of a mail send by Update.
 */
public class UpdateTestcaseExpectedMail {
    public UpdateTestcaseExpectedMail() {
        this.subject = "";
        this.body = "";
    }

    //-------------------------------------------------------------------------
    //              Properties
    //-------------------------------------------------------------------------

    public String getSubject() {
        return subject;
    }

    public void setSubject( String subject ) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody( String body ) {
        this.body = body;
    }

//-------------------------------------------------------------------------
    //              Object
    //-------------------------------------------------------------------------

    @Override
    public String toString() {
        return "UpdateTestcaseExpectedMail{" +
                "subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                '}';
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    /**
     * Subject of the mail.
     */
    private String subject;

    /**
     * File name of a text file that contains the expected body message of the mail.
     */
    private String body;
}
