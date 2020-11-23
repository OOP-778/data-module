package com.oop.datamodule.mongodb.model;

import com.oop.datamodule.api.model.ModelBody;

public interface MongoModelBody extends ModelBody {
    /*
    Used as "primary key" in sql based databases
    It acts for finder of the data by the
    Better to use existing field inside the data
    Will save one useless key created :)
    */
    String getIdentifierKey();
}
