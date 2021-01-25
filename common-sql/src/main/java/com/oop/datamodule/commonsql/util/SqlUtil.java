package com.oop.datamodule.commonsql.util;

public class SqlUtil {
    public static String escapeColumn(String column) {
        return "\"" + column + "\"";
    }
}
