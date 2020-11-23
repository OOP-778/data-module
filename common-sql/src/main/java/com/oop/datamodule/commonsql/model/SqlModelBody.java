package com.oop.datamodule.commonsql.model;

import com.oop.datamodule.api.model.ModelBody;

public interface SqlModelBody extends ModelBody {
    /**
     * Structure of the table
     * First value of the array is primary key!
     *
     * @return an array of strings for table structure
     */
    String[] getStructure();
}
