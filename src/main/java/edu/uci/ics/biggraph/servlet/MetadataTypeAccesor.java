package edu.uci.ics.biggraph.servlet;

/**
 * Utilities for accessing to metadata stored in database.
 *
 * Note that this class only support dealing with ONLY ONE
 * instance of one data type. Accessing to more than one
 * instance of this data type in one call to this class
 * will result in unexpected consequences.
 *
 * This class will be subclassed by others.
 *
 * Created by liqiangw on 4/27/14.
 */
public abstract class MetadataTypeAccesor {
    /**
     * Load the data entry from database.
     */
    public abstract void loadEntry();

    /**
     * Write back the entry to database.
     */
    public abstract void storeEntry();
}
