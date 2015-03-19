package dk.dbc.ocbtools.testengine.testcases;

/**
 * Created by stp on 13/03/15.
 */
public class TestcaseSolrQuery {
    public TestcaseSolrQuery() {
        this.query = "";
        this.numFound = -1;
    }

    //-------------------------------------------------------------------------
    //              Properties
    //-------------------------------------------------------------------------

    public String getQuery() {
        return query;
    }

    public void setQuery( String query ) {
        this.query = query;
    }

    public int getNumFound() {
        return numFound;
    }

    public void setNumFound( int numFound ) {
        this.numFound = numFound;
    }

    @Override
    public String toString() {
        return "TestcaseSolrQuery{" +
                "query='" + query + '\'' +
                ", numFound=" + numFound +
                '}';
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private String query;
    private int numFound;
}
