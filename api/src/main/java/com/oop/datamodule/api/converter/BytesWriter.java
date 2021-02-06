package com.oop.datamodule.api.converter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.List;
import java.util.function.Consumer;
import lombok.SneakyThrows;

public class BytesWriter {
  private ByteArrayOutputStream arrayOutputStream;
  private DataOutputStream stream;

  public BytesWriter() {
    arrayOutputStream = new ByteArrayOutputStream();
    stream = new DataOutputStream(arrayOutputStream);
  }

  @SneakyThrows
  public void writeString(String value) {
    stream.writeUTF(value);
  }

  @SneakyThrows
  public <T> void writeList(List<T> data, Consumer<T> writer) {
    stream.writeInt(data.size());
    for (T datum : data) writer.accept(datum);
  }

  @SneakyThrows
  public byte[] done() {
    byte[] bytes = arrayOutputStream.toByteArray();

    arrayOutputStream = new ByteArrayOutputStream();
    stream = new DataOutputStream(arrayOutputStream);
    return bytes;
  }
}
