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
public final class TestMov {

    private static Stream<Arguments> opcodes() {
        return Stream.of(
                Arguments.of("mov r8d,r8d", "45 89 c0"),
                Arguments.of("mov r9d,r8d", "45 89 c1"),
                Arguments.of("mov r10d,r8d", "45 89 c2"),
                Arguments.of("mov r11d,r8d", "45 89 c3"),
                Arguments.of("mov r12d,r8d", "45 89 c4"),
                Arguments.of("mov r13d,r8d", "45 89 c5"),
                Arguments.of("mov r14d,r8d", "45 89 c6"),
                Arguments.of("mov r15d,r8d", "45 89 c7"),
                Arguments.of("mov r8d,r9d", "45 89 c8"),
                Arguments.of("mov r9d,r9d", "45 89 c9"),
                Arguments.of("mov r10d,r9d", "45 89 ca"),
                Arguments.of("mov r11d,r9d", "45 89 cb"),
                Arguments.of("mov r12d,r9d", "45 89 cc"),
                Arguments.of("mov r13d,r9d", "45 89 cd"),
                Arguments.of("mov r14d,r9d", "45 89 ce"),
                Arguments.of("mov r15d,r9d", "45 89 cf"),
                Arguments.of("mov r8d,r10d", "45 89 d0"),
                Arguments.of("mov r9d,r10d", "45 89 d1"),
                Arguments.of("mov r10d,r10d", "45 89 d2"),
                Arguments.of("mov r11d,r10d", "45 89 d3"),
                Arguments.of("mov r12d,r10d", "45 89 d4"),
                Arguments.of("mov r13d,r10d", "45 89 d5"),
                Arguments.of("mov r14d,r10d", "45 89 d6"),
                Arguments.of("mov r15d,r10d", "45 89 d7"),
                Arguments.of("mov r8d,r11d", "45 89 d8"),
                Arguments.of("mov r9d,r11d", "45 89 d9"),
                Arguments.of("mov r10d,r11d", "45 89 da"),
                Arguments.of("mov r11d,r11d", "45 89 db"),
                Arguments.of("mov r12d,r11d", "45 89 dc"),
                Arguments.of("mov r13d,r11d", "45 89 dd"),
                Arguments.of("mov r14d,r11d", "45 89 de"),
                Arguments.of("mov r15d,r11d", "45 89 df"),
                Arguments.of("mov r8d,r12d", "45 89 e0"),
                Arguments.of("mov r9d,r12d", "45 89 e1"),
                Arguments.of("mov r10d,r12d", "45 89 e2"),
                Arguments.of("mov r11d,r12d", "45 89 e3"),
                Arguments.of("mov r12d,r12d", "45 89 e4"),
                Arguments.of("mov r13d,r12d", "45 89 e5"),
                Arguments.of("mov r14d,r12d", "45 89 e6"),
                Arguments.of("mov r15d,r12d", "45 89 e7"),
                Arguments.of("mov r8d,r13d", "45 89 e8"),
                Arguments.of("mov r9d,r13d", "45 89 e9"),
                Arguments.of("mov r10d,r13d", "45 89 ea"),
                Arguments.of("mov r11d,r13d", "45 89 eb"),
                Arguments.of("mov r12d,r13d", "45 89 ec"),
                Arguments.of("mov r13d,r13d", "45 89 ed"),
                Arguments.of("mov r14d,r13d", "45 89 ee"),
                Arguments.of("mov r15d,r13d", "45 89 ef"),
                Arguments.of("mov r8d,r14d", "45 89 f0"),
                Arguments.of("mov r9d,r14d", "45 89 f1"),
                Arguments.of("mov r10d,r14d", "45 89 f2"),
                Arguments.of("mov r11d,r14d", "45 89 f3"),
                Arguments.of("mov r12d,r14d", "45 89 f4"),
                Arguments.of("mov r13d,r14d", "45 89 f5"),
                Arguments.of("mov r14d,r14d", "45 89 f6"),
                Arguments.of("mov r15d,r14d", "45 89 f7"),
                Arguments.of("mov r8d,r15d", "45 89 f8"),
                Arguments.of("mov r9d,r15d", "45 89 f9"),
                Arguments.of("mov r10d,r15d", "45 89 fa"),
                Arguments.of("mov r11d,r15d", "45 89 fb"),
                Arguments.of("mov r12d,r15d", "45 89 fc"),
                Arguments.of("mov r13d,r15d", "45 89 fd"),
                Arguments.of("mov r14d,r15d", "45 89 fe"),
                Arguments.of("mov r15d,r15d", "45 89 ff"),
                //
                Arguments.of("mov eax,eax", "89 c0"),
                Arguments.of("mov ecx,eax", "89 c1"),
                Arguments.of("mov edx,eax", "89 c2"),
                Arguments.of("mov ebx,eax", "89 c3"),
                Arguments.of("mov esp,eax", "89 c4"),
                Arguments.of("mov ebp,eax", "89 c5"),
                Arguments.of("mov esi,eax", "89 c6"),
                Arguments.of("mov edi,eax", "89 c7"),
                Arguments.of("mov eax,ecx", "89 c8"),
                Arguments.of("mov ecx,ecx", "89 c9"),
                Arguments.of("mov edx,ecx", "89 ca"),
                Arguments.of("mov ebx,ecx", "89 cb"),
                Arguments.of("mov esp,ecx", "89 cc"),
                Arguments.of("mov ebp,ecx", "89 cd"),
                Arguments.of("mov esi,ecx", "89 ce"),
                Arguments.of("mov edi,ecx", "89 cf"),
                Arguments.of("mov eax,edx", "89 d0"),
                Arguments.of("mov ecx,edx", "89 d1"),
                Arguments.of("mov edx,edx", "89 d2"),
                Arguments.of("mov ebx,edx", "89 d3"),
                Arguments.of("mov esp,edx", "89 d4"),
                Arguments.of("mov ebp,edx", "89 d5"),
                Arguments.of("mov esi,edx", "89 d6"),
                Arguments.of("mov edi,edx", "89 d7"),
                Arguments.of("mov eax,ebx", "89 d8"),
                Arguments.of("mov ecx,ebx", "89 d9"),
                Arguments.of("mov edx,ebx", "89 da"),
                Arguments.of("mov ebx,ebx", "89 db"),
                Arguments.of("mov esp,ebx", "89 dc"),
                Arguments.of("mov ebp,ebx", "89 dd"),
                Arguments.of("mov esi,ebx", "89 de"),
                Arguments.of("mov edi,ebx", "89 df"),
                Arguments.of("mov eax,esp", "89 e0"),
                Arguments.of("mov ecx,esp", "89 e1"),
                Arguments.of("mov edx,esp", "89 e2"),
                Arguments.of("mov ebx,esp", "89 e3"),
                Arguments.of("mov esp,esp", "89 e4"),
                Arguments.of("mov ebp,esp", "89 e5"),
                Arguments.of("mov esi,esp", "89 e6"),
                Arguments.of("mov edi,esp", "89 e7"),
                Arguments.of("mov eax,ebp", "89 e8"),
                Arguments.of("mov ecx,ebp", "89 e9"),
                Arguments.of("mov edx,ebp", "89 ea"),
                Arguments.of("mov ebx,ebp", "89 eb"),
                Arguments.of("mov esp,ebp", "89 ec"),
                Arguments.of("mov ebp,ebp", "89 ed"),
                Arguments.of("mov esi,ebp", "89 ee"),
                Arguments.of("mov edi,ebp", "89 ef"),
                Arguments.of("mov eax,esi", "89 f0"),
                Arguments.of("mov ecx,esi", "89 f1"),
                Arguments.of("mov edx,esi", "89 f2"),
                Arguments.of("mov ebx,esi", "89 f3"),
                Arguments.of("mov esp,esi", "89 f4"),
                Arguments.of("mov ebp,esi", "89 f5"),
                Arguments.of("mov esi,esi", "89 f6"),
                Arguments.of("mov edi,esi", "89 f7"),
                Arguments.of("mov eax,edi", "89 f8"),
                Arguments.of("mov ecx,edi", "89 f9"),
                Arguments.of("mov edx,edi", "89 fa"),
                Arguments.of("mov ebx,edi", "89 fb"),
                Arguments.of("mov esp,edi", "89 fc"),
                Arguments.of("mov ebp,edi", "89 fd"),
                Arguments.of("mov esi,edi", "89 fe"),
                Arguments.of("mov edi,edi", "89 ff"),
                //
                // here we expect to find the values in the same order as the input since
                // ByteBuffer is, by default, big-endian
                Arguments.of("mov eax,0x12345678", "b8 12 34 56 78"),
                Arguments.of("mov ecx,0x12345678", "b9 12 34 56 78"),
                Arguments.of("mov edx,0x12345678", "ba 12 34 56 78"),
                Arguments.of("mov ebx,0x12345678", "bb 12 34 56 78"),
                Arguments.of("mov esp,0x12345678", "bc 12 34 56 78"),
                Arguments.of("mov ebp,0x12345678", "bd 12 34 56 78"),
                Arguments.of("mov esi,0x12345678", "be 12 34 56 78"),
                Arguments.of("mov edi,0x12345678", "bf 12 34 56 78"),
                //
                Arguments.of("mov QWORD PTR [rax-0xd8],rax", "48 89 80 28 ff ff ff"));
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
