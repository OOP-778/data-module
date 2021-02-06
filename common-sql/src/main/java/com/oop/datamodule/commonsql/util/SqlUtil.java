package com.oop.datamodule.commonsql.util;

import com.oop.datamodule.commonsql.database.SQLDatabase;

public class SqlUtil {
  public static String escapeColumn(String column, SQLDatabase database) {
    return database.columnEscaper() + column + database.columnEscaper();
  }
}
