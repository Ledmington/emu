package com.ledmington.cpu.x86;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import com.ledmington.utils.BitUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public final class TestDecoding extends X86Test {

    @Test
    void debug() {
        final String expected = "lea ecx,[rdx+rdx*8]";
        final String hexCode = "8d 0c d2";
        final String[] parsed = hexCode.split(" ");
        final byte[] code = new byte[parsed.length];
        for (int i = 0; i < parsed.length; i++) {
            code[i] = BitUtils.parseByte(parsed[i]);
        }

        final InstructionDecoder id = new InstructionDecoder();
        final List<Instruction> instructions = id.decode(code);
        assertNotNull(instructions, "InstructionDecoder returned a null List");
        assertEquals(
                1,
                instructions.size(),
                () -> String.format("Expected 1 instruction but %,d were found", instructions.size()));
        final String decoded = instructions.get(0).toString();
        assertEquals(expected, decoded, () -> String.format("Expected '%s' but '%s' was decoded", expected, decoded));
    }

    @ParameterizedTest
    @MethodSource("instructions")
    void parsing(final String expected, final String hexCode) {
        final String[] parsed = hexCode.split(" ");
        final byte[] code = new byte[parsed.length];
        for (int i = 0; i < parsed.length; i++) {
            code[i] = BitUtils.parseByte(parsed[i]);
        }

        final InstructionDecoder id = new InstructionDecoder();
        final List<Instruction> instructions = id.decode(code);
        assertNotNull(instructions, "InstructionDecoder returned a null List");
        assertEquals(
                1,
                instructions.size(),
                () -> String.format("Expected 1 instruction but %,d were found", instructions.size()));
        final String decoded = instructions.get(0).toString();
        assertEquals(expected, decoded, () -> String.format("Expected '%s' but '%s' was decoded", expected, decoded));
    }
}
