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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

final class TestWriteOnlyByteBufferV1 {

    private static final RandomGenerator rng =
            RandomGeneratorFactory.getDefault().create(System.nanoTime());

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void bytes(final boolean endianness) {
        final int length = 200;
        final WriteOnlyByteBuffer bb = new WriteOnlyByteBufferV1(length, endianness);
        final ByteBuffer ref = ByteBuffer.allocate(length);
        ref.order(endianness ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        for (int i = 0; i < length; i++) {
            final byte b = BitUtils.asByte(rng.nextInt());
            ref.put(b);
            bb.write(b);
        }
        assertArrayEquals(
                ref.array(),
                bb.array(),
                () -> String.format(
                        "Expected byte array to be %s but was %s",
                        Arrays.toString(ref.array()), Arrays.toString(bb.array())));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void words(final boolean endianness) {
        final int length = 200;
        final WriteOnlyByteBuffer bb = new WriteOnlyByteBufferV1(length, endianness);
        final ByteBuffer ref = ByteBuffer.allocate(length);
        ref.order(endianness ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        for (int i = 0; i < length; i += 2) {
            final short s = BitUtils.asShort(rng.nextInt());
            ref.putShort(s);
            bb.write(s);
        }
        assertArrayEquals(
                ref.array(),
                bb.array(),
                () -> String.format(
                        "Expected word array to be %s but was %s",
                        Arrays.toString(ref.array()), Arrays.toString(bb.array())));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void doubleWords(final boolean endianness) {
        final int length = 200;
        final WriteOnlyByteBuffer bb = new WriteOnlyByteBufferV1(length, endianness);
        final ByteBuffer ref = ByteBuffer.allocate(length);
        ref.order(endianness ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        for (int i = 0; i < length; i += 4) {
            final int x = rng.nextInt();
            ref.putInt(x);
            bb.write(x);
        }
        assertArrayEquals(
                ref.array(),
                bb.array(),
                () -> String.format(
                        "Expected doubleword array to be %s but was %s",
                        Arrays.toString(ref.array()), Arrays.toString(bb.array())));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void quadWords(final boolean endianness) {
        final int length = 200;
        final WriteOnlyByteBuffer bb = new WriteOnlyByteBufferV1(length, endianness);
        final ByteBuffer ref = ByteBuffer.allocate(length);
        ref.order(endianness ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        for (int i = 0; i < length; i += 8) {
            final long x = rng.nextLong();
            ref.putLong(x);
            bb.write(x);
        }
        assertArrayEquals(
                ref.array(),
                bb.array(),
                () -> String.format(
                        "Expected quadword array to be %s but was %s",
                        Arrays.toString(ref.array()), Arrays.toString(bb.array())));
    }
}
