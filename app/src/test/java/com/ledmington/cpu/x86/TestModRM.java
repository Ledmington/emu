package com.ledmington.cpu.x86;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public final class TestModRM {

    private static Stream<Arguments> mods() {
        return Stream.of(
                Arguments.of((byte) 0x00, (byte) 0x00),
                Arguments.of((byte) 0x40, (byte) 0x01),
                Arguments.of((byte) 0x80, (byte) 0x02),
                Arguments.of((byte) 0xc0, (byte) 0x03));
    }

    @ParameterizedTest
    @MethodSource("mods")
    void parseMod(final byte m, final byte expected) {
        assertEquals(expected, new ModRM(m).mod());
    }

    private static Stream<Arguments> regs() {
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
    @MethodSource("regs")
    void parseReg(final byte m, final byte expected) {
        assertEquals(expected, new ModRM(m).reg());
    }

    private static Stream<Arguments> rms() {
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
    @MethodSource("rms")
    void parseRM(final byte m, final byte expected) {
        assertEquals(expected, new ModRM(m).rm());
    }
}
