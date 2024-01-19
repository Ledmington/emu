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
public final class TestTest {

    private static Stream<Arguments> opcodes() {
        return Stream.of(
                Arguments.of("test rax,rax", "48 85 c0"),
                Arguments.of("test rcx,rax", "48 85 c1"),
                Arguments.of("test rdx,rax", "48 85 c2"),
                Arguments.of("test rbx,rax", "48 85 c3"),
                Arguments.of("test rsp,rax", "48 85 c4"),
                Arguments.of("test rbp,rax", "48 85 c5"),
                Arguments.of("test rsi,rax", "48 85 c6"),
                Arguments.of("test rdi,rax", "48 85 c7"),
                Arguments.of("test rax,rcx", "48 85 c8"),
                Arguments.of("test rcx,rcx", "48 85 c9"),
                Arguments.of("test rdx,rcx", "48 85 ca"),
                Arguments.of("test rbx,rcx", "48 85 cb"),
                Arguments.of("test rsp,rcx", "48 85 cc"),
                Arguments.of("test rbp,rcx", "48 85 cd"),
                Arguments.of("test rsi,rcx", "48 85 ce"),
                Arguments.of("test rdi,rcx", "48 85 cf"),
                Arguments.of("test rax,rdx", "48 85 d0"),
                Arguments.of("test rcx,rdx", "48 85 d1"),
                Arguments.of("test rdx,rdx", "48 85 d2"),
                Arguments.of("test rbx,rdx", "48 85 d3"),
                Arguments.of("test rsp,rdx", "48 85 d4"),
                Arguments.of("test rbp,rdx", "48 85 d5"),
                Arguments.of("test rsi,rdx", "48 85 d6"),
                Arguments.of("test rdi,rdx", "48 85 d7"),
                Arguments.of("test rax,rbx", "48 85 d8"),
                Arguments.of("test rcx,rbx", "48 85 d9"),
                Arguments.of("test rdx,rbx", "48 85 da"),
                Arguments.of("test rbx,rbx", "48 85 db"),
                Arguments.of("test rsp,rbx", "48 85 dc"),
                Arguments.of("test rbp,rbx", "48 85 dd"),
                Arguments.of("test rsi,rbx", "48 85 de"),
                Arguments.of("test rdi,rbx", "48 85 df"),
                Arguments.of("test rax,rsp", "48 85 e0"),
                Arguments.of("test rcx,rsp", "48 85 e1"),
                Arguments.of("test rdx,rsp", "48 85 e2"),
                Arguments.of("test rbx,rsp", "48 85 e3"),
                Arguments.of("test rsp,rsp", "48 85 e4"),
                Arguments.of("test rbp,rsp", "48 85 e5"),
                Arguments.of("test rsi,rsp", "48 85 e6"),
                Arguments.of("test rdi,rsp", "48 85 e7"),
                Arguments.of("test rax,rbp", "48 85 e8"),
                Arguments.of("test rcx,rbp", "48 85 e9"),
                Arguments.of("test rdx,rbp", "48 85 ea"),
                Arguments.of("test rbx,rbp", "48 85 eb"),
                Arguments.of("test rsp,rbp", "48 85 ec"),
                Arguments.of("test rbp,rbp", "48 85 ed"),
                Arguments.of("test rsi,rbp", "48 85 ee"),
                Arguments.of("test rdi,rbp", "48 85 ef"),
                Arguments.of("test rax,rsi", "48 85 f0"),
                Arguments.of("test rcx,rsi", "48 85 f1"),
                Arguments.of("test rdx,rsi", "48 85 f2"),
                Arguments.of("test rbx,rsi", "48 85 f3"),
                Arguments.of("test rsp,rsi", "48 85 f4"),
                Arguments.of("test rbp,rsi", "48 85 f5"),
                Arguments.of("test rsi,rsi", "48 85 f6"),
                Arguments.of("test rdi,rsi", "48 85 f7"),
                Arguments.of("test rax,rdi", "48 85 f8"),
                Arguments.of("test rcx,rdi", "48 85 f9"),
                Arguments.of("test rdx,rdi", "48 85 fa"),
                Arguments.of("test rbx,rdi", "48 85 fb"),
                Arguments.of("test rsp,rdi", "48 85 fc"),
                Arguments.of("test rbp,rdi", "48 85 fd"),
                Arguments.of("test rsi,rdi", "48 85 fe"),
                Arguments.of("test rdi,rdi", "48 85 ff"));
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
