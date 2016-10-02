package dk.dbc.ocbtools.testengine.testcases;

class TestcaseSolrQuery {
    private String query;
    private int numFound;

    public TestcaseSolrQuery() {
        query = "";
        numFound = -1;
    }

    @Override
    public String toString() {
        return "TestcaseSolrQuery{" +
                "query='" + query + '\'' +
                ", numFound=" + numFound +
                '}';
    }
}
