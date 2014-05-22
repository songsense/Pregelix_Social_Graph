package edu.uci.ics.biggraph.inputformat;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;

/**
 * ADMParser - Convert ADM graph file like:
 * {"source_node":1,"label":"Siming","target_node":{{2,3}},"weight":{{1.0,1.0}}}
 *
 * into standard to-be-computed graph format:
 *
 * "$(egoNode) $(neighborNum) $(N1) $(W1) $(N2) $(W2) ..."
 *
 * For more information,
 * @see https://docs.google.com/document/d/18jaKJT3OCVdKgXPB6JMClRKvsl2IWx8vJYfiTgFjxd0/edit
 *
 * Created by liqiangw on 5/17/14.
 */
public class ADMParser {
    /**
     * Convert ADM file into graph data that can be
     * understand by applications.
     *
     * It's hard coded.
     */
    public static ArrayList<String> ADM2Graph2(String line) {
        if (line == null || line.length() == 0) {
            return null;
        }
        System.out.println("In ADM2Graph: " + line);


        ArrayList<String> ret = new ArrayList<String>();
        JsonReader jsonReader = Json.createReader(new StringReader(line));
        Map map = jsonReader.readObject();

        // source_node
        ret.add(map.get("source_node").toString());

        // target_node (label skipped) and weight
        JsonArray targetNodes = (JsonArray) map.get("target_nodes");
        JsonArray weights = (JsonArray) map.get("weight");
        int size = targetNodes.size();
        if (size != weights.size()) {
            System.err.println("ADM2Graph: wrong formated line!");
            return null;
        }
        ret.add(Integer.toString(size));
        for (int i = 0; i < size; i++) {
            ret.add(targetNodes.get(i).toString());
            ret.add(weights.get(i).toString());
        }

        return ret;
    }


    @Deprecated
    public static ArrayList<String> ADM2Graph(String line) {
        if (line == null || line.length() == 0) {
            return null;
        }
        System.out.println("In ADM2Graph: " + line);

        char c;
        int len = line.length();
        ArrayList<String> slist = new ArrayList<String>();

        // source_node
        int index = line.indexOf(':', 0);
        StringBuilder sb = new StringBuilder();
        if (index < 0 || ++index >= len) {
            System.err.println("ADM2Graph: Invalid input format");
            return null;
        }
        while (Character.isDigit((c = line.charAt(index)))) {
            sb.append(c);
            index++;
        }
        if (line.charAt(index) != ',') {
            System.err.println("ADM2Graph: Invalid input format");
            return null;
        }
        slist.add(sb.toString());

        // target_node (label skipped)
        ArrayList<String> targets = new ArrayList<String>();
        index = line.indexOf(',', index + 1);
        if (index < 0) {
            System.err.println("ADM2Graph: Invalid input format");
            return null;
        }
        index = line.indexOf(':', index);
        if (index < 0) {
            System.err.println("ADM2Graph: Invalid input format");
            return null;
        }
        if (index + 2 >= len || line.charAt(++index) != '{'
                || line.charAt(++index) != '{') {
            System.err.println("ADM2Graph: Invalid input format");
            return null;
        }
        int nSize = 0;
        sb = new StringBuilder();
        while (++index < len
                && (Character.isDigit(c = line.charAt(index)) || c == ','
                    || c == '-' || c == '+')) {
            if (c != ',') {
                sb.append(c);
                if (index + 1 < len && !Character.isDigit(c = line.charAt(index + 1)) &&
                    c != '-' && c != '+') {
                    nSize++;
                    targets.add(sb.toString());
                    sb = new StringBuilder();
                }
            }
        }
        --index;
        slist.add(1, Integer.toString(nSize));
        if (index + 2 >= len || line.charAt(++index) != '}'
                || line.charAt(++index) != '}') {
            System.err.println("ADM2Graph: Invalid input format");
            return null;
        }

        // weight
        ArrayList<String> weights = new ArrayList<String>();
        index = line.indexOf(':', index);
        if (index < 0) {
            System.err.println("ADM2Graph: Invalid input format");
            return null;
        }
        if (index + 2 >= len || line.charAt(++index) != '{'
                || line.charAt(++index) != '{') {
            System.err.println("ADM2Graph: Invalid input format");
            return null;
        }
        while (++index < len
                && (Character.isDigit(c = line.charAt(index)) || c == ','
                    || c == '.' || c == '-' || c == '+')) {
            if (c != ',') {
                sb.append(c);
                if (index + 1 < len && !Character.isDigit(c = line.charAt(index + 1)) &&
                    c != '.' && c != '-' && c != '+') {
                    nSize--;
                    weights.add(sb.toString());
                    sb = new StringBuilder();
                }
            }
        }
        --index;
        if (nSize != 0 || index + 2 >= len || line.charAt(++index) != '}'
                || line.charAt(++index) != '}') {
            System.err.println("ADM2Graph: Invalid input format");
            return null;
        }

        // intermix the target nodes and their weight
        assert targets.size() == weights.size();
        int nodeSize = targets.size();
        for (int i = 0; i < nodeSize; i++) {
            slist.add(targets.get(i));
            slist.add(weights.get(i));
        }

        return slist;
    }

    /**
     * Split one line of ADM graph file into separate tokens.
     */
    public static String[] split(String line) {
        ArrayList<String> slist = ADMParser.ADM2Graph2(line.toString());
        int size = slist.size();
        String[] fields = new String[size];

        for (int i = 0; i < size; i++) {
            fields[i] = slist.get(i);
        }

        return fields;
    }

    public static void main(String[] args) {
        Object[] list;
        String str = args[0];
        list = ADM2Graph2(str).toArray();
        for (Object s : list) {
            System.out.println((String) s);
        }
    }
}
