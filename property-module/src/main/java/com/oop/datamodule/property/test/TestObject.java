package com.oop.datamodule.property.test;

import com.oop.datamodule.property.api.Property;
import com.oop.datamodule.property.api.PropertyHolder;
import com.oop.datamodule.property.impl.PropertyHelper;

public class TestObject {

    private Property<Integer> property = null;

    public TestObject() {
        PropertyHolder propertyHolder = PropertyHelper.makePropertyHolderWithReflection(this);

    }

}
