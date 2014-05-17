package edu.uci.ics.biggraph.inputformat;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.ArrayList;

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
    public static ArrayList<String> ADM2Graph(String line) {
        if (line == null || line.equals("")) {
            return null;
        }

        ArrayList<String> ret = new ArrayList<String>();
        String tmp;
        System.out.println(line);

        JsonReader jsonReader = Json.createReader(new StringReader(line));
        JsonObject jsonObject = jsonReader.readObject();
        jsonReader.close();

        // read id
        tmp = jsonObject.get("source_node").toString();
        ret.add(tmp);

        // read target node (skipping label)
        tmp = jsonObject.get("target_node").toString();
        jsonReader = Json.createReader(new StringReader(tmp));
        JsonArray jsonArray = jsonReader.readArray();
        int size = jsonArray.size();

        ret.add(Integer.toString(size));
        for (int i = 0; i < size; i++) {
            ret.add(Integer.toString(jsonArray.getInt(i)));
        }

        // read target node weight
        tmp = jsonObject.get("weight").toString();
        jsonReader = Json.createReader(new StringReader(tmp));
        jsonArray = jsonReader.readArray();
        assert jsonArray.size() == size;
        for (int i = 0; i < size; i++) {
            ret.add(Double.toString(jsonArray.getJsonNumber(i).doubleValue()));
        }

        return ret;
    }

    public static void main(String[] args) {
        ArrayList<String> list;
        String str = "{\"source_node\":1,\"label\":\"Siming\",\"target_node\":[2,3],\"weight\":[1.0,1.0]}";
        list = ADM2Graph(str);
        for (String s : list) {
            System.out.println(s);
        }
    }
}
