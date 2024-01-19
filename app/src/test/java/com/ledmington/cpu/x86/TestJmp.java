package com.ledmington.cpu.x86;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.stream.Stream;

import com.ledmington.utils.BitUtils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Checked with <a href="https://defuse.ca/online-x86-assembler.htm">online x86/x64 assembler</a>.
 */
public final class TestJmp {

    private static Stream<Arguments> opcodes() {
        return Stream.of(Arguments.of("jmp 0x2e301", "e9 fc e2 02 00"));
    }

    @ParameterizedTest
    @MethodSource("opcodes")
    void parsing(final String expected, final String hexCode) {
        final String[] parsed = hexCode.split(" ");
        final byte[] code = new byte[parsed.length];
        for (int i = 0; i < parsed.length; i++) {
            code[i] = BitUtils.parseByte(parsed[i]);
        }

        final InstructionDecoder id = new InstructionDecoder();
        final List<Instruction> instructions = id.decode(code);
        assertNotNull(instructions);
        assertEquals(1, instructions.size());
        assertEquals(expected, instructions.get(0).toString());
    }
}
