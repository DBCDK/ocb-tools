//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------
import java.util.ArrayList;
import java.util.List;

//-----------------------------------------------------------------------------
/**
 * Represents a setup structure in a testcase json file.
 */
public class TestcaseSetup {
    public TestcaseSetup() {
        this.holdings = new ArrayList<>();
        this.rawrepo = null;
        this.solr = null;
    }

    //-------------------------------------------------------------------------
    //              Properties
    //-------------------------------------------------------------------------

    public List<Integer> getHoldings() {
        return holdings;
    }

    public void setHoldings( List<Integer> holdings ) {
        this.holdings = holdings;
    }

    public List<TestcaseRecord> getRawrepo() {
        return rawrepo;
    }

    public void setRawrepo( List<TestcaseRecord> rawrepo ) {
        this.rawrepo = rawrepo;
    }

    public List<TestcaseSolrQuery> getSolr() {
        return solr;
    }

    public void setSolr( List<TestcaseSolrQuery> solr ) {
        this.solr = solr;
    }

    @Override
    public String toString() {
        return "TestcaseSetup{" +
                "holdings=" + holdings +
                ", rawrepo=" + rawrepo +
                ", solr=" + solr +
                '}';
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private List<Integer> holdings;
    private List<TestcaseRecord> rawrepo;
    private List<TestcaseSolrQuery> solr;
}
