package edu.uci.ics.biggraph.servlet;

/**
 * Map AQL interface to inner implementation of URL builder 
 * 
 * @see http://asterixdb.ics.uci.edu/documentation/api.html#QueryStatusApi
 */
public enum RestAPI {
    DDL ("/ddl", "ddl="),
    UPDATE ("/update", "statements="),
    QUERY ("/query", "query="),
    ASYNC_RESULT ("/query/result", "handle="),
    QUERY_STATUS ("/query/status", "handle=mode=asynchronous&"); // XXX: not sure
    
    private final String frag;
    private final String query;
    
    private RestAPI(String query, String frag) {
        this.query = query;
        this.frag = frag;
    }
    
    public String getQuery() {
        return query;
    }
    
    public String getFrag() {
        return frag;
    }
}
