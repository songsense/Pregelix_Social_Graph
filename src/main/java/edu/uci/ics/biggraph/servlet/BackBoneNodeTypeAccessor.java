package edu.uci.ics.biggraph.servlet;

/**
 * Created by liqiangw on 4/27/14.
 */
public class BackBoneNodeTypeAccessor extends DataTypeAccesor {
    private static BackBoneNodeTypeAccessor ourInstance = new BackBoneNodeTypeAccessor();

    public static BackBoneNodeTypeAccessor getInstance() {
        return ourInstance;
    }

    private BackBoneNodeTypeAccessor() {
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
