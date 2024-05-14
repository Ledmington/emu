package com.ledmington.cpu.x86;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import com.ledmington.utils.BitUtils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public final class TestDecoding extends X86Test {

    @ParameterizedTest
    @MethodSource("instructions")
    void parsing(final String expected, final String hexCode) {
        final String[] parsed = hexCode.split(" ");
        final byte[] code = new byte[parsed.length];
        for (int i = 0; i < parsed.length; i++) {
            code[i] = BitUtils.asByte(Integer.parseInt(parsed[i], 16));
        }

        final InstructionDecoder id = new InstructionDecoder(code);
        final List<Instruction> instructions = id.decodeAll(code.length);
        assertNotNull(instructions, "InstructionDecoder returned a null List");
        final int codeLen = instructions.size();
        assertEquals(1, codeLen, () -> String.format("Expected 1 instruction but %,d were found", codeLen));
        final String decoded = instructions.getFirst().toIntelSyntax();
        assertEquals(expected, decoded, () -> String.format("Expected '%s' but '%s' was decoded", expected, decoded));
    }
}
