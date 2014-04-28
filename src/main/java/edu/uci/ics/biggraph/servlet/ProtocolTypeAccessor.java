package edu.uci.ics.biggraph.servlet;

/**
 * Created by liqiangw on 4/27/14.
 */
public class ProtocolTypeAccessor extends DataTypeAccesor {
    private static ProtocolTypeAccessor ourInstance = new ProtocolTypeAccessor();

    public static ProtocolTypeAccessor getInstance() {
        return ourInstance;
    }

    private ProtocolTypeAccessor() {
    }

    /**
     * Load the data entry from database.
     */
    @Override
    public void loadEntry() {

    }

    /**
     * Write back the entry to database.
     */
    @Override
    public void storeEntry() {

    }
}
