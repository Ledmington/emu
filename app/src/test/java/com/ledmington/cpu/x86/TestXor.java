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
public final class TestXor {

    private static Stream<Arguments> opcodes() {
        return Stream.of(
                Arguments.of("xor r8d,r8d", "45 31 c0"),
                Arguments.of("xor r9d,r8d", "45 31 c1"),
                Arguments.of("xor r10d,r8d", "45 31 c2"),
                Arguments.of("xor r11d,r8d", "45 31 c3"),
                Arguments.of("xor r12d,r8d", "45 31 c4"),
                Arguments.of("xor r13d,r8d", "45 31 c5"),
                Arguments.of("xor r14d,r8d", "45 31 c6"),
                Arguments.of("xor r15d,r8d", "45 31 c7"),
                Arguments.of("xor r8d,r9d", "45 31 c8"),
                Arguments.of("xor r9d,r9d", "45 31 c9"),
                Arguments.of("xor r10d,r9d", "45 31 ca"),
                Arguments.of("xor r11d,r9d", "45 31 cb"),
                Arguments.of("xor r12d,r9d", "45 31 cc"),
                Arguments.of("xor r13d,r9d", "45 31 cd"),
                Arguments.of("xor r14d,r9d", "45 31 ce"),
                Arguments.of("xor r15d,r9d", "45 31 cf"),
                Arguments.of("xor r8d,r10d", "45 31 d0"),
                Arguments.of("xor r9d,r10d", "45 31 d1"),
                Arguments.of("xor r10d,r10d", "45 31 d2"),
                Arguments.of("xor r11d,r10d", "45 31 d3"),
                Arguments.of("xor r12d,r10d", "45 31 d4"),
                Arguments.of("xor r13d,r10d", "45 31 d5"),
                Arguments.of("xor r14d,r10d", "45 31 d6"),
                Arguments.of("xor r15d,r10d", "45 31 d7"),
                Arguments.of("xor r8d,r11d", "45 31 d8"),
                Arguments.of("xor r9d,r11d", "45 31 d9"),
                Arguments.of("xor r10d,r11d", "45 31 da"),
                Arguments.of("xor r11d,r11d", "45 31 db"),
                Arguments.of("xor r12d,r11d", "45 31 dc"),
                Arguments.of("xor r13d,r11d", "45 31 dd"),
                Arguments.of("xor r14d,r11d", "45 31 de"),
                Arguments.of("xor r15d,r11d", "45 31 df"),
                Arguments.of("xor r8d,r12d", "45 31 e0"),
                Arguments.of("xor r9d,r12d", "45 31 e1"),
                Arguments.of("xor r10d,r12d", "45 31 e2"),
                Arguments.of("xor r11d,r12d", "45 31 e3"),
                Arguments.of("xor r12d,r12d", "45 31 e4"),
                Arguments.of("xor r13d,r12d", "45 31 e5"),
                Arguments.of("xor r14d,r12d", "45 31 e6"),
                Arguments.of("xor r15d,r12d", "45 31 e7"),
                Arguments.of("xor r8d,r13d", "45 31 e8"),
                Arguments.of("xor r9d,r13d", "45 31 e9"),
                Arguments.of("xor r10d,r13d", "45 31 ea"),
                Arguments.of("xor r11d,r13d", "45 31 eb"),
                Arguments.of("xor r12d,r13d", "45 31 ec"),
                Arguments.of("xor r13d,r13d", "45 31 ed"),
                Arguments.of("xor r14d,r13d", "45 31 ee"),
                Arguments.of("xor r15d,r13d", "45 31 ef"),
                Arguments.of("xor r8d,r14d", "45 31 f0"),
                Arguments.of("xor r9d,r14d", "45 31 f1"),
                Arguments.of("xor r10d,r14d", "45 31 f2"),
                Arguments.of("xor r11d,r14d", "45 31 f3"),
                Arguments.of("xor r12d,r14d", "45 31 f4"),
                Arguments.of("xor r13d,r14d", "45 31 f5"),
                Arguments.of("xor r14d,r14d", "45 31 f6"),
                Arguments.of("xor r15d,r14d", "45 31 f7"),
                Arguments.of("xor r8d,r15d", "45 31 f8"),
                Arguments.of("xor r9d,r15d", "45 31 f9"),
                Arguments.of("xor r10d,r15d", "45 31 fa"),
                Arguments.of("xor r11d,r15d", "45 31 fb"),
                Arguments.of("xor r12d,r15d", "45 31 fc"),
                Arguments.of("xor r13d,r15d", "45 31 fd"),
                Arguments.of("xor r14d,r15d", "45 31 fe"),
                Arguments.of("xor r15d,r15d", "45 31 ff"),
                //
                Arguments.of("xor eax,eax", "31 c0"),
                Arguments.of("xor ecx,eax", "31 c1"),
                Arguments.of("xor edx,eax", "31 c2"),
                Arguments.of("xor ebx,eax", "31 c3"),
                Arguments.of("xor esp,eax", "31 c4"),
                Arguments.of("xor ebp,eax", "31 c5"),
                Arguments.of("xor esi,eax", "31 c6"),
                Arguments.of("xor edi,eax", "31 c7"),
                Arguments.of("xor eax,ecx", "31 c8"),
                Arguments.of("xor ecx,ecx", "31 c9"),
                Arguments.of("xor edx,ecx", "31 ca"),
                Arguments.of("xor ebx,ecx", "31 cb"),
                Arguments.of("xor esp,ecx", "31 cc"),
                Arguments.of("xor ebp,ecx", "31 cd"),
                Arguments.of("xor esi,ecx", "31 ce"),
                Arguments.of("xor edi,ecx", "31 cf"),
                Arguments.of("xor eax,edx", "31 d0"),
                Arguments.of("xor ecx,edx", "31 d1"),
                Arguments.of("xor edx,edx", "31 d2"),
                Arguments.of("xor ebx,edx", "31 d3"),
                Arguments.of("xor esp,edx", "31 d4"),
                Arguments.of("xor ebp,edx", "31 d5"),
                Arguments.of("xor esi,edx", "31 d6"),
                Arguments.of("xor edi,edx", "31 d7"),
                Arguments.of("xor eax,ebx", "31 d8"),
                Arguments.of("xor ecx,ebx", "31 d9"),
                Arguments.of("xor edx,ebx", "31 da"),
                Arguments.of("xor ebx,ebx", "31 db"),
                Arguments.of("xor esp,ebx", "31 dc"),
                Arguments.of("xor ebp,ebx", "31 dd"),
                Arguments.of("xor esi,ebx", "31 de"),
                Arguments.of("xor edi,ebx", "31 df"),
                Arguments.of("xor eax,esp", "31 e0"),
                Arguments.of("xor ecx,esp", "31 e1"),
                Arguments.of("xor edx,esp", "31 e2"),
                Arguments.of("xor ebx,esp", "31 e3"),
                Arguments.of("xor esp,esp", "31 e4"),
                Arguments.of("xor ebp,esp", "31 e5"),
                Arguments.of("xor esi,esp", "31 e6"),
                Arguments.of("xor edi,esp", "31 e7"),
                Arguments.of("xor eax,ebp", "31 e8"),
                Arguments.of("xor ecx,ebp", "31 e9"),
                Arguments.of("xor edx,ebp", "31 ea"),
                Arguments.of("xor ebx,ebp", "31 eb"),
                Arguments.of("xor esp,ebp", "31 ec"),
                Arguments.of("xor ebp,ebp", "31 ed"),
                Arguments.of("xor esi,ebp", "31 ee"),
                Arguments.of("xor edi,ebp", "31 ef"),
                Arguments.of("xor eax,esi", "31 f0"),
                Arguments.of("xor ecx,esi", "31 f1"),
                Arguments.of("xor edx,esi", "31 f2"),
                Arguments.of("xor ebx,esi", "31 f3"),
                Arguments.of("xor esp,esi", "31 f4"),
                Arguments.of("xor ebp,esi", "31 f5"),
                Arguments.of("xor esi,esi", "31 f6"),
                Arguments.of("xor edi,esi", "31 f7"),
                Arguments.of("xor eax,edi", "31 f8"),
                Arguments.of("xor ecx,edi", "31 f9"),
                Arguments.of("xor edx,edi", "31 fa"),
                Arguments.of("xor ebx,edi", "31 fb"),
                Arguments.of("xor esp,edi", "31 fc"),
                Arguments.of("xor ebp,edi", "31 fd"),
                Arguments.of("xor esi,edi", "31 fe"),
                Arguments.of("xor edi,edi", "31 ff"));
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
