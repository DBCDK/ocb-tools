//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.ocbwait;

//-----------------------------------------------------------------------------
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 26/04/16.
 */
public class WebServiceResponse {
    public WebServiceResponse( Integer responseCode, InputStream is ) throws IOException {
        this.responseCode = responseCode;

        BufferedReader br = new BufferedReader( new InputStreamReader( is ) );

        this.response = "";
        String line;
        while ((line = br.readLine()) != null) {
            this.response += line;
        }
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public String getResponse() {
        return response;
    }

    @Override
    public String toString() {
        return "WebServiceResponse{" +
                "responseCode=" + responseCode +
                ", response='" + response + '\'' +
                '}';
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private Integer responseCode;
    private String response;
}
