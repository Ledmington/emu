package com.ledmington.cpu.x86;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public final class TestSIB {

    private static Stream<Arguments> scales() {
        return Stream.of(
                Arguments.of((byte) 0x00, (byte) 0x00),
                Arguments.of((byte) 0x40, (byte) 0x01),
                Arguments.of((byte) 0x80, (byte) 0x02),
                Arguments.of((byte) 0xc0, (byte) 0x03));
    }

    @ParameterizedTest
    @MethodSource("scales")
    void parseScale(final byte m, final byte expected) {
        assertEquals(expected, new SIB(m).scale());
    }

    private static Stream<Arguments> indexes() {
        return Stream.of(
                Arguments.of((byte) 0x00, (byte) 0x00),
                Arguments.of((byte) 0x08, (byte) 0x01),
                Arguments.of((byte) 0x10, (byte) 0x02),
                Arguments.of((byte) 0x18, (byte) 0x03),
                Arguments.of((byte) 0x20, (byte) 0x04),
                Arguments.of((byte) 0x28, (byte) 0x05),
                Arguments.of((byte) 0x30, (byte) 0x06),
                Arguments.of((byte) 0x38, (byte) 0x07));
    }

    @ParameterizedTest
    @MethodSource("indexes")
    void parseIndex(final byte m, final byte expected) {
        assertEquals(expected, new SIB(m).index());
    }

    private static Stream<Arguments> bases() {
        return Stream.of(
                Arguments.of((byte) 0x00, (byte) 0x00),
                Arguments.of((byte) 0x01, (byte) 0x01),
                Arguments.of((byte) 0x02, (byte) 0x02),
                Arguments.of((byte) 0x03, (byte) 0x03),
                Arguments.of((byte) 0x04, (byte) 0x04),
                Arguments.of((byte) 0x05, (byte) 0x05),
                Arguments.of((byte) 0x06, (byte) 0x06),
                Arguments.of((byte) 0x07, (byte) 0x07));
    }

    @ParameterizedTest
    @MethodSource("bases")
    void parseBases(final byte m, final byte expected) {
        assertEquals(expected, new SIB(m).base());
    }
}
