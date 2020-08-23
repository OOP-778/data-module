package com.oop.datamodule.body;

public interface SqlDataBody extends DataBody {
    /**
     * Name of the table
     *
     * @return name of the table
     */
    String getTable();

    /**
     * Primary key of the object
     *
     * @return serialized primary key
     */
    String getKey();

    /**
     * Structure of the table
     * First value of the array is primary key!
     *
     * @return an array of strings for table structure
     */
    String[] getStructure();
}
