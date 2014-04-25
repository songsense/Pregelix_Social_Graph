package edu.uci.ics.biggraph.servlet;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Translate AQL into valid URL. 
 */
public class URLGenerator {
    static String generate(String host, int port, RestAPI type, String cmd) {
        URI uri = null;
        
        try {
            uri = new URI("http", null, host, port, type.getQuery(), type.getFrag() + cmd, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return uri != null ? uri.toASCIIString() : null;
    }
    
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        System.out.println(generate("localhost", 19001, RestAPI.DDL, ""));
    }

}
