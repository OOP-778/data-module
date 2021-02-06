package com.oop.datamodule.universal.model;

import com.oop.datamodule.api.model.ModelBody;
import com.oop.datamodule.commonsql.model.SqlModelBody;
import com.oop.datamodule.mongodb.model.MongoModelBody;

public interface UniversalBodyModel extends ModelBody, SqlModelBody, MongoModelBody {}
