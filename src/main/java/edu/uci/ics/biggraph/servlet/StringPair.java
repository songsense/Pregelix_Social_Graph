package edu.uci.ics.biggraph.servlet;

/**
 * Created by liqiangw on 4/27/14.
 */
public class StringPair {
    private String key = null;
    private String value = null;

    public StringPair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
