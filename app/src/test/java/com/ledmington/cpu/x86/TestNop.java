package com.ledmington.cpu.x86;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import com.ledmington.utils.BitUtils;
import com.ledmington.utils.ByteBuffer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Checked with https://defuse.ca/online-x86-assembler.htm
 */
public final class TestNop {

    private static Stream<Arguments> opcodes() {
        return Stream.of(Arguments.of("nop", "90"));
    }

    @ParameterizedTest
    @MethodSource("opcodes")
    void parsing(final String expected, final String hexCode) {
        final String[] parsed = hexCode.split(" ");
        final byte[] code = new byte[parsed.length];
        for (int i = 0; i < parsed.length; i++) {
            code[i] = BitUtils.parseByte(parsed[i]);
        }

        assertEquals(expected, new Instruction(new ByteBuffer(code)).toString());
    }
}
