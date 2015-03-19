//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------

import java.util.List;

/**
 * Created by stp on 13/03/15.
 */
public class TestcaseSetup {
    public TestcaseSetup() {
        this.rawrepo = null;
        this.solr = null;
    }

    //-------------------------------------------------------------------------
    //              Properties
    //-------------------------------------------------------------------------

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
                "rawrepo=" + rawrepo +
                ", solr=" + solr +
                '}';
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private List<TestcaseRecord> rawrepo;
    private List<TestcaseSolrQuery> solr;
}
