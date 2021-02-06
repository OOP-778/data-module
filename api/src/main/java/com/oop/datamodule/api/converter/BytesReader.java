package com.oop.datamodule.api.converter;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.List;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

@Getter
public class BytesReader {

  private ByteArrayInputStream arrayInputStream;
  private DataInputStream inputStream;

  public BytesReader(@NonNull byte[] data) {
    this.arrayInputStream = new ByteArrayInputStream(data);
    this.inputStream = new DataInputStream(arrayInputStream);
  }

  @SneakyThrows
  public int readInt() {
    return inputStream.readInt();
  }

  @SneakyThrows
  public boolean isEmpty() {
    return inputStream.available() == 0;
  }

  @SneakyThrows
  public String readString() {
    return inputStream.readUTF();
  }

  public <T> void readList(List<T> data, Supplier<T> reader) {
    int listSize = readInt();
    for (int i = 0; i < listSize; i++) {
      data.add(reader.get());
    }
  }
}
