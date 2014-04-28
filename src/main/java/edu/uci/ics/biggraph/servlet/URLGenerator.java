package edu.uci.ics.biggraph.servlet;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Translate AQL into valid URL. 
 */
public class URLGenerator {
    /**
     * Generate the final URL for HTTP request.
     */
    public static String generate(String host, int port, RestAPI type, String revisedCmd) {
        return "http://" + host + ":" + Integer.toString(port) + type.getQuery()
                + "?" + type.getFrag() + revisedCmd;
    }
    
//    static String generate(String host, int port, RestAPI type, String cmd) {
//        URI uri = null;
//        
//        try {
//            uri = new URI("http", null, host, port, type.getQuery(), type.getFrag() + cmd, null);
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
//        return uri != null ? uri.toASCIIString() : null;
//    }

    /**
     * Parse the AQL by removing/translating necessary notations.
     */
    public static String cmdParser(String cmd) {
        StringBuilder sb = new StringBuilder();
        int strlen = cmd.length();
        
        for (int i = 0; i < strlen; i++) {
            char c = cmd.charAt(i);
            
            switch (c) {
            case ' ':
            case '\t':
                sb.append("%20");
                while (i < strlen && 
                        (cmd.charAt(i) == ' ' || cmd.charAt(i) == '\t')) {
                    i++;
                }
                i--;
                break;
            case '\n':
            case '\r':
                break;
            default:
                sb.append(c);
                break;
            }
        }
        
        return sb.toString();
    }

    /**
     * Generate UPDATE command.
     * @param dataverse - the dataverse the required entry belongs to.
     * @param dataset - the dataset the required entry belongs to.
     * @param payload - items in this entry represented as strings.
     *                e.g.: "\"id\":123", "\"name\":\"John Doe\"".
     * @return the assembled command.
     */
    public static String update(String dataverse, String dataset, String[] payload) {
        StringBuilder sb = new StringBuilder();

        sb.append("use dataverse " + dataverse + ";\n")
          .append("insert into dataset " + dataset + "({");
        for (int i = 0; i < payload.length; i++) {
            sb.append(payload[i]);
            if (i != payload.length - 1) {
                sb.append(",");
            }
        }
        sb.append("});");

        return sb.toString();
    }

    /**
     * Generate QUERY command.
     * @param dataverse - the dataverse the required entry belongs to.
     * @param dataset - the dataset the required entry belongs to.
     * @return the assembled command.
     */
    public static String query(String dataverse, String dataset) {
        StringBuilder sb = new StringBuilder();

        sb.append("use dataverse " + dataverse + ";\n")
          .append("for $l in dataset('" + dataset + "') return $l;");

        return sb.toString();
    }
    
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        String test = "use dataverse company;" +
                      "for $l in dataset('Employee') return $l;";
        String test2 = "use dataverse company;" +
                       "insert into dataset Employee({ \"id\":123,\"name\":\"John Doe\"});";
        String revised = cmdParser(test2);
        System.out.println(generate("localhost", 19002, RestAPI.UPDATE, revised));
    }

}
