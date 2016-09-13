package dk.dbc.ocbtools.testengine.testcases;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a setup structure in a testcase json file.
 */
public class UpdateTestcaseSetup {
    private List<Integer> holdings;
    private List<UpdateTestcaseRecord> rawrepo;
    private List<TestcaseSolrQuery> solr;

    public UpdateTestcaseSetup() {
        this.holdings = new ArrayList<>();
        this.rawrepo = null;
        this.solr = null;
    }

    public List<Integer> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<Integer> holdings) {
        this.holdings = holdings;
    }

    public List<UpdateTestcaseRecord> getRawrepo() {
        return rawrepo;
    }

    public void setRawrepo(List<UpdateTestcaseRecord> rawrepo) {
        this.rawrepo = rawrepo;
    }

    public List<TestcaseSolrQuery> getSolr() {
        return solr;
    }

    public void setSolr(List<TestcaseSolrQuery> solr) {
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
}
