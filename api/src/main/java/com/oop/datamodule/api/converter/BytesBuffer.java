package com.oop.datamodule.api.converter;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class BytesBuffer {

    private static final int SIZE_OF_INT = 4;
    private int currentIndex;

    @Getter
    private byte[] data;

    public BytesBuffer(@NonNull byte[] data) {
        this.data = data;
    }

    public BytesBuffer() {
        data = new byte[0];
    }

    public int readInt() {
        byte[] intBytes = Arrays.copyOfRange(data, currentIndex, currentIndex + SIZE_OF_INT);
        currentIndex += SIZE_OF_INT;
        return ByteBuffer.wrap(intBytes).getInt();
    }

    public void writeInt(int value) {
        data = mergeBytes(data, ByteBuffer.allocate(SIZE_OF_INT).putInt(value).array());
    }

    @SneakyThrows
    public void writeString(String value) {
        writeObject(value.getBytes(StandardCharsets.UTF_8));
    }

    public String readString() {
        return new String(readObject(), StandardCharsets.UTF_8);
    }

    public void writeObject(byte[] value) {
        writeInt(value.length);
        data = mergeBytes(data, value);
    }

    public byte[] readObject() {
        int objectSize = readInt();
        byte[] objectBytes = Arrays.copyOfRange(data, currentIndex, currentIndex + objectSize);
        currentIndex += objectSize;

        return objectBytes;
    }

    public <T> void writeList(List<T> data, Consumer<T> writer) {
        writeInt(data.size());
        for (T datum : data)
            writer.accept(datum);
    }

    public <T> void readList(List<T> data, Supplier<T> reader) {
        int listSize = readInt();
        for (int i = 0; i < listSize; i++) {
            data.add(reader.get());
        }
    }

    private static byte[] mergeBytes(byte[]... values) {
        int len = Stream.of(values).mapToInt(bytes -> bytes.length).sum();

        ByteBuffer byteBuffer = ByteBuffer.allocate(len);
        for (byte[] value : values)
            byteBuffer.put(Objects.requireNonNull(value, "Merging bytes cannot be null!"));

        return byteBuffer.array();
    }

    public void merge(BytesBuffer bytesBuffer) {
        data = mergeBytes(data, bytesBuffer.data);
    }

    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(data);
    }

    public void clear() {
        data = new byte[0];
        currentIndex = 0;
    }

    @SneakyThrows
    public void append(OutputStream outputStream) {
        // Merge the size of this buffer & data
        byte[] merged = mergeBytes(ByteBuffer.allocate(SIZE_OF_INT).putInt(data.length).array(), data);
        System.out.println("size: " + data.length);
        outputStream.write(merged);
        merged = new byte[0];
    }

    @SneakyThrows
    public static BytesBuffer fromStream(InputStream stream, byte[] size) {
        byte[] data = new byte[ByteBuffer.wrap(size).getInt()];
        stream.read(data, 0, data.length);

        return new BytesBuffer(data);
    }
}
