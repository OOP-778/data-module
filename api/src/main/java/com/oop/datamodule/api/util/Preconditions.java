package com.oop.datamodule.api.util;

public class Preconditions {
    public static void checkArgument(boolean arg, String message) {
        if (!arg)
            throw new IllegalArgumentException(message);
    }
}
