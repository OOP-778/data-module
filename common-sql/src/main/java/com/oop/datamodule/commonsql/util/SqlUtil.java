package com.oop.datamodule.commonsql.util;

import com.oop.datamodule.commonsql.database.SQLDatabase;

import java.util.Optional;

public class SqlUtil {
  public static String escapeColumn(String column, SQLDatabase database) {
    return database.columnEscaper() + column + database.columnEscaper();
  }

  public static String formatSQL(String sql, Object... values) {
    return format(sql, values);
  }

  protected static String format(String message, Object... args) {
    if (message.contains("{}") && args.length > 0) {
      int currentObjectIndex = 0;
      int currentCharIndex = 0;
      char[] messageArray = message.toCharArray();

      StringBuilder builder = new StringBuilder();

      while (currentCharIndex < message.length()) {
        char currentChar = messageArray[currentCharIndex];
        if (currentChar == '{' && messageArray.length > currentCharIndex + 1) {
          char nextChar = messageArray[currentCharIndex + 1];
          if (nextChar == '}') {
            currentCharIndex += 2;
            if (args.length > currentObjectIndex) {
              builder.append(
                  Optional.ofNullable(args[currentObjectIndex])
                      .map(Object::toString)
                      .orElse("null"));
              currentObjectIndex += 1;
            }
            continue;
          }
        }

        builder.append(currentChar);
        currentCharIndex++;
      }
      message = builder.toString();
    }

    return message;
  }
}
