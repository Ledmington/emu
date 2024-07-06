/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2024 Filippo Barbari <filippo.barbari@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ledmington.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import org.junit.jupiter.api.BeforeEach;
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
        final ByteBuffer ref = ByteBuffer.wrap(arr);
        ref.order(endianness ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        for (int i = 0; i < arr.length; i++) {
            final int finalI = i;
            final byte expected = ref.get();
            final byte actual = bb.read1();
            assertEquals(
                    expected,
                    actual,
                    () -> String.format(
                            "Expected to read 0x%02x but was 0x%02x, at position %,d", expected, actual, finalI));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void words(final boolean endianness) {
        final ReadOnlyByteBuffer bb = new ReadOnlyByteBufferV1(arr, endianness);
        final ByteBuffer ref = ByteBuffer.wrap(arr);
        ref.order(endianness ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        for (int i = 0; i < arr.length; i += 2) {
            final short expected = ref.getShort();
            final short actual = bb.read2();
            assertEquals(
                    expected, actual, () -> String.format("Expected to read 0x%04x but was 0x%04x", expected, actual));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void doubleWords(final boolean endianness) {
        final ReadOnlyByteBuffer bb = new ReadOnlyByteBufferV1(arr, endianness);
        final ByteBuffer ref = ByteBuffer.wrap(arr);
        ref.order(endianness ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        for (int i = 0; i < arr.length; i += 4) {
            final int expected = ref.getInt();
            final int actual = bb.read4();
            assertEquals(
                    expected, actual, () -> String.format("Expected to read 0x%08x but was 0x%08x", expected, actual));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void quadWords(final boolean endianness) {
        final ReadOnlyByteBuffer bb = new ReadOnlyByteBufferV1(arr, endianness);
        final ByteBuffer ref = ByteBuffer.wrap(arr);
        ref.order(endianness ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        for (int i = 0; i < arr.length; i += 8) {
            final long expected = ref.getLong();
            final long actual = bb.read8();
            assertEquals(
                    expected,
                    actual,
                    () -> String.format("Expected to read 0x%016x but was 0x%016x", expected, actual));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4, 8})
    void byteAlignment(final int alignment) {
        final ReadOnlyByteBuffer bb = new ReadOnlyByteBufferV1(arr, false, alignment);
        while (bb.getPosition() < arr.length) {
            final long start = bb.getPosition();
            bb.read1();
            final long alignedPosition = Math.max(start + 1L, start + alignment);
            assertEquals(
                    alignedPosition,
                    bb.getPosition(),
                    () -> String.format(
                            "Expected position to be aligned to %d-bytes boundary at %,d after reading a byte, but was %,d",
                            alignment, alignedPosition, bb.getPosition()));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4, 8})
    void wordAlignment(final int alignment) {
        final ReadOnlyByteBuffer bb = new ReadOnlyByteBufferV1(arr, false, alignment);
        while (bb.getPosition() + 1 < arr.length) {
            final long start = bb.getPosition();
            bb.read2();
            final long alignedPosition = Math.max(start + 2L, start + alignment);
            assertEquals(
                    alignedPosition,
                    bb.getPosition(),
                    () -> String.format(
                            "Expected position to be aligned to %d-bytes boundary at %,d after reading a short, but was %,d",
                            alignment, alignedPosition, bb.getPosition()));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4, 8})
    void doubleWordAlignment(final int alignment) {
        final ReadOnlyByteBuffer bb = new ReadOnlyByteBufferV1(arr, false, alignment);
        while (bb.getPosition() + 3 < arr.length) {
            final long start = bb.getPosition();
            bb.read4();
            final long alignedPosition = Math.max(start + 4L, start + alignment);
            assertEquals(
                    alignedPosition,
                    bb.getPosition(),
                    () -> String.format(
                            "Expected position to be aligned to %d-bytes boundary at %,d after reading an int, but was %,d",
                            alignment, alignedPosition, bb.getPosition()));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4, 8})
    void quadWordAlignment(final int alignment) {
        final ReadOnlyByteBuffer bb = new ReadOnlyByteBufferV1(arr, false, alignment);
        while (bb.getPosition() + 7 < arr.length) {
            final long start = bb.getPosition();
            bb.read8();
            final long alignedPosition = Math.max(start + 8L, start + alignment);
            assertEquals(
                    alignedPosition,
                    bb.getPosition(),
                    () -> String.format(
                            "Expected position to be aligned to %d-bytes boundary at %,d after reading a long, but was %,d",
                            alignment, alignedPosition, bb.getPosition()));
        }
    }
}
