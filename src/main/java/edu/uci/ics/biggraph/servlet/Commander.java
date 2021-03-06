package edu.uci.ics.biggraph.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Send AQL (AstrixDB Query Language) to interface with output data 
 * and database.
 */
public class Commander {
    private static final String USER_AGENT = "Mozilla/5.0";

    /**
     * Send HTTP GET request.
     *
     * @return payload get from GET request, or null on error.
     * @throws IOException 
     */
    public static String sendGet(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);

        int response = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + response);

        // Did not get payload successfully
        if (response != 200) {
            return null;
        }
        
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String line;
        StringBuilder sb = new StringBuilder();
        
        while ((line = in.readLine()) != null) {
            sb.append(line);
        }
        in.close();
        
        return sb.toString();
    }

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        System.out.println(sendGet("http://localhost:19002/query?query=use%20dataverse%20company;for%20$l%20in%20dataset('Employee')%20return%20$l;"));
    }
}
