package com.oop.datamodule.commonsql.util;

public enum Column {
  INTEGER("INT"),
  FLOAT("FLOAT"),
  LONG("LONG"),
  TEXT("TEXT"),
  VARCHAR("VARCHAR(255)"),
  DOUBLE("DOUBLE"),
  BOOLEAN("BOOLEAN");

  private final String sql;

  Column(String sql) {
    this.sql = sql;
  }

  public String getSql() {
    return sql;
  }
}
