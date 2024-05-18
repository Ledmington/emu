package com.ledmington.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

final class TestReadOnlyByteBufferV1 {

    private static final RandomGenerator rng =
            RandomGeneratorFactory.getDefault().create(System.nanoTime());
    private byte[] arr;

    @BeforeEach
    void setup() {
        this.arr = new byte[200];
        for (int i = 0; i < arr.length; i++) {
            this.arr[i] = BitUtils.asByte(rng.nextInt());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void bytes(final boolean endianness) {
        final ReadOnlyByteBuffer bb = new ReadOnlyByteBufferV1(arr, endianness);
        for (int i = 0; i < arr.length; i++) {
            final int finalI = i;
            final byte expected = arr[i];
            final byte actual = bb.read1();
            assertEquals(
                    expected,
                    actual,
                    () -> String.format(
                            "Expected to read 0x%02x but was 0x%02x, at position %,d", expected, actual, finalI));
        }
    }

    @Test
    void wordsLE() {
        final ReadOnlyByteBuffer bb = new ReadOnlyByteBufferV1(arr, true);
        for (int i = 0; i < arr.length; i += 2) {
            final short expected = BitUtils.asShort((BitUtils.asShort(arr[i + 1]) << 8) | BitUtils.asShort(arr[i]));
            final short actual = bb.read2();
            assertEquals(
                    expected, actual, () -> String.format("Expected to read 0x%04x but was 0x%04x", expected, actual));
        }
    }

    @Test
    void wordsBE() {
        final ReadOnlyByteBuffer bb = new ReadOnlyByteBufferV1(arr, false);
        for (int i = 0; i < arr.length; i += 2) {
            final short expected = BitUtils.asShort((BitUtils.asShort(arr[i]) << 8) | BitUtils.asShort(arr[i + 1]));
            final short actual = bb.read2();
            assertEquals(
                    expected, actual, () -> String.format("Expected to read 0x%04x but was 0x%04x", expected, actual));
        }
    }

    @Test
    void doubleWordsLE() {
        final ReadOnlyByteBuffer bb = new ReadOnlyByteBufferV1(arr, false);
        for (int i = 0; i < arr.length; i += 4) {
            final int expected = BitUtils.asInt(arr[i])
                    | (BitUtils.asInt(arr[i + 1]) << 8)
                    | (BitUtils.asInt(arr[i + 2]) << 16)
                    | (BitUtils.asInt(arr[i + 3]) << 24);
            final int actual = bb.read4();
            assertEquals(
                    expected, actual, () -> String.format("Expected to read 0x%08x but was 0x%08x", expected, actual));
        }
    }

    @Test
    void doubleWordsBE() {
        final ReadOnlyByteBuffer bb = new ReadOnlyByteBufferV1(arr, true);
        for (int i = 0; i < arr.length; i += 4) {
            final int expected = (BitUtils.asInt(arr[i]) << 24)
                    | (BitUtils.asInt(arr[i + 1]) << 16)
                    | (BitUtils.asInt(arr[i + 2]) << 8)
                    | BitUtils.asInt(arr[i + 3]);
            final int actual = bb.read4();
            assertEquals(
                    expected, actual, () -> String.format("Expected to read 0x%08x but was 0x%08x", expected, actual));
        }
    }

    @Test
    void quadWordsLE() {
        final ReadOnlyByteBuffer bb = new ReadOnlyByteBufferV1(arr, false);
        for (int i = 0; i < arr.length; i += 8) {
            final long expected = (BitUtils.asLong(arr[i + 7]) << 56)
                    | (BitUtils.asLong(arr[i + 6]) << 48)
                    | (BitUtils.asLong(arr[i + 5]) << 40)
                    | (BitUtils.asLong(arr[i + 4]) << 32)
                    | (BitUtils.asLong(arr[i + 3]) << 24)
                    | (BitUtils.asLong(arr[i + 2]) << 16)
                    | (BitUtils.asLong(arr[i + 1]) << 8)
                    | BitUtils.asLong(arr[i]);
            final long actual = bb.read8();
            assertEquals(
                    expected,
                    actual,
                    () -> String.format("Expected to read 0x%016x but was 0x%016x", expected, actual));
        }
    }

    @Test
    void quadWordsBE() {
        final ReadOnlyByteBuffer bb = new ReadOnlyByteBufferV1(arr, true);
        for (int i = 0; i < arr.length; i += 8) {
            final long expected = (BitUtils.asLong(arr[i]) << 56)
                    | (BitUtils.asLong(arr[i + 1]) << 48)
                    | (BitUtils.asLong(arr[i + 2]) << 40)
                    | (BitUtils.asLong(arr[i + 3]) << 32)
                    | (BitUtils.asLong(arr[i + 4]) << 24)
                    | (BitUtils.asLong(arr[i + 5]) << 16)
                    | (BitUtils.asLong(arr[i + 6]) << 8)
                    | BitUtils.asLong(arr[i + 7]);
            final long actual = bb.read8();
            assertEquals(
                    expected,
                    actual,
                    () -> String.format("Expected to read 0x%016x but was 0x%016x", expected, actual));
        }
    }
}
