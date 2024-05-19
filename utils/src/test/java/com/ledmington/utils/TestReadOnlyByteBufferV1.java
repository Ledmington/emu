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
