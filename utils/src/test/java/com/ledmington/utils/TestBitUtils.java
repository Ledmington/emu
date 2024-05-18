package com.ledmington.utils;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class TestBitUtils {

    private static Stream<Arguments> SHRbytes() {
        return Stream.of(
                Arguments.of((byte) 0xff, (byte) 0xff, 0),
                Arguments.of((byte) 0x7f, (byte) 0xff, 1),
                Arguments.of((byte) 0x3f, (byte) 0xff, 2),
                Arguments.of((byte) 0x1f, (byte) 0xff, 3),
                Arguments.of((byte) 0x0f, (byte) 0xff, 4),
                Arguments.of((byte) 0x07, (byte) 0xff, 5),
                Arguments.of((byte) 0x03, (byte) 0xff, 6),
                Arguments.of((byte) 0x01, (byte) 0xff, 7),
                Arguments.of((byte) 0x00, (byte) 0xff, 8),
                //
                Arguments.of((byte) 0x80, (byte) 0x80, 0),
                Arguments.of((byte) 0x40, (byte) 0x80, 1),
                Arguments.of((byte) 0x20, (byte) 0x80, 2),
                Arguments.of((byte) 0x10, (byte) 0x80, 3),
                Arguments.of((byte) 0x08, (byte) 0x80, 4),
                Arguments.of((byte) 0x04, (byte) 0x80, 5),
                Arguments.of((byte) 0x02, (byte) 0x80, 6),
                Arguments.of((byte) 0x01, (byte) 0x80, 7),
                Arguments.of((byte) 0x00, (byte) 0x80, 8));
    }

    @ParameterizedTest
    @MethodSource("SHRbytes")
    void shiftRight(final byte expected, final byte input, final int shift) {
        final byte result = BitUtils.shr(input, shift);
        Assertions.assertEquals(
                expected,
                result,
                () -> String.format(
                        "Expected 0x%02x >>> %,d to be 0x%02x but was 0x%02x", input, shift, expected, result));
    }

    private static Stream<Arguments> ANDbytes() {
        return Stream.of(
                Arguments.of((byte) 0xff, (byte) 0xff, (byte) 0xff),
                Arguments.of((byte) 0x7f, (byte) 0x7f, (byte) 0xff),
                Arguments.of((byte) 0x3f, (byte) 0x3f, (byte) 0xff),
                Arguments.of((byte) 0x1f, (byte) 0x1f, (byte) 0xff),
                Arguments.of((byte) 0x0f, (byte) 0x0f, (byte) 0xff),
                Arguments.of((byte) 0x07, (byte) 0x07, (byte) 0xff),
                Arguments.of((byte) 0x03, (byte) 0x03, (byte) 0xff),
                Arguments.of((byte) 0x01, (byte) 0x01, (byte) 0xff),
                Arguments.of((byte) 0x00, (byte) 0x00, (byte) 0xff));
    }

    @ParameterizedTest
    @MethodSource("ANDbytes")
    void bitwiseAnd(final byte expected, final byte input1, final byte input2) {
        final byte result = BitUtils.and(input1, input2);
        Assertions.assertEquals(
                expected,
                result,
                () -> String.format(
                        "Expected 0x%02x AND 0x%02x to be 0x%02x but was 0x%02x", input1, input2, expected, result));
    }

    private static Stream<Arguments> ORbytes() {
        return Stream.of(
                Arguments.of((byte) 0xff, (byte) 0x00, (byte) 0xff),
                Arguments.of((byte) 0x81, (byte) 0x80, (byte) 0x01),
                Arguments.of((byte) 0x82, (byte) 0x80, (byte) 0x02),
                Arguments.of((byte) 0x84, (byte) 0x80, (byte) 0x04),
                Arguments.of((byte) 0x88, (byte) 0x80, (byte) 0x08),
                Arguments.of((byte) 0x90, (byte) 0x80, (byte) 0x10),
                Arguments.of((byte) 0xa0, (byte) 0x80, (byte) 0x20),
                Arguments.of((byte) 0xc0, (byte) 0x80, (byte) 0x40),
                Arguments.of((byte) 0x80, (byte) 0x80, (byte) 0x80));
    }

    @ParameterizedTest
    @MethodSource("ORbytes")
    void bitwiseOr(final byte expected, final byte input1, final byte input2) {
        final byte result = BitUtils.or(input1, input2);
        Assertions.assertEquals(
                expected,
                result,
                () -> String.format(
                        "Expected 0x%02x OR 0x%02x to be 0x%02x but was 0x%02x", input1, input2, expected, result));
    }

    private static Stream<Arguments> XORbytes() {
        return Stream.of(
                Arguments.of((byte) 0xff, (byte) 0x00, (byte) 0xff),
                Arguments.of((byte) 0xff, (byte) 0xff, (byte) 0x00),
                Arguments.of((byte) 0x00, (byte) 0x00, (byte) 0x00),
                Arguments.of((byte) 0x00, (byte) 0xff, (byte) 0xff),
                Arguments.of((byte) 0x81, (byte) 0x80, (byte) 0x01),
                Arguments.of((byte) 0x01, (byte) 0x80, (byte) 0x81),
                Arguments.of((byte) 0x00, (byte) 0x81, (byte) 0x81));
    }

    @ParameterizedTest
    @MethodSource("XORbytes")
    void bitwiseXor(final byte expected, final byte input1, final byte input2) {
        final byte result = BitUtils.xor(input1, input2);
        Assertions.assertEquals(
                expected,
                result,
                () -> String.format(
                        "Expected 0x%02x XOR 0x%02x to be 0x%02x but was 0x%02x", input1, input2, expected, result));
    }
}
