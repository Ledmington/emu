package com.ledmington.cpu.x86;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public final class TestIndirectOperand {
    private static Stream<Arguments> correctIndirectOperands() {
        return Stream.of(
                Arguments.of(IndirectOperand.builder().reg2(Register32.EAX), "[eax]"),
                Arguments.of(IndirectOperand.builder().reg2(Register32.EAX).displacement((byte) 0x12), "[eax+0x12]"),
                Arguments.of(
                        IndirectOperand.builder().reg2(Register32.EAX).displacement((short) 0x1234), "[eax+0x1234]"),
                Arguments.of(
                        IndirectOperand.builder().reg2(Register32.EAX).displacement(0x12345678), "[eax+0x12345678]"),
                Arguments.of(
                        IndirectOperand.builder().reg2(Register32.EAX).displacement(0x1123456789abcdefL),
                        "[eax+0x1123456789abcdef]"),
                Arguments.of(IndirectOperand.builder().reg2(Register32.EAX).displacement((byte) 0x82), "[eax-0x7e]"),
                Arguments.of(
                        IndirectOperand.builder().reg2(Register32.EAX).displacement((short) 0x8234), "[eax-0x7dcc]"),
                Arguments.of(
                        IndirectOperand.builder().reg2(Register32.EAX).displacement(0x82345678), "[eax-0x7dcba988]"),
                Arguments.of(
                        IndirectOperand.builder().reg2(Register32.EAX).displacement(0x8123456789abcdefL),
                        "[eax-0x7edcba9876543211]"),
                Arguments.of(IndirectOperand.builder().displacement((byte) 0x12), "[0x12]"),
                Arguments.of(IndirectOperand.builder().displacement((short) 0x1234), "[0x1234]"),
                Arguments.of(IndirectOperand.builder().displacement(0x12345678), "[0x12345678]"),
                Arguments.of(IndirectOperand.builder().displacement(0x1123456789abcdefL), "[0x1123456789abcdef]"),
                Arguments.of(IndirectOperand.builder().reg2(Register32.EBX).constant(2), "[ebx*2]"),
                Arguments.of(
                        IndirectOperand.builder()
                                .reg1(Register32.EAX)
                                .reg2(Register32.EBX)
                                .constant(2),
                        "[eax+ebx*2]"),
                Arguments.of(
                        IndirectOperand.builder()
                                .reg1(Register32.EAX)
                                .reg2(Register32.EBX)
                                .constant(2)
                                .displacement((byte) 0x12),
                        "[eax+ebx*2+0x12]"),
                Arguments.of(
                        IndirectOperand.builder()
                                .reg1(Register32.EAX)
                                .reg2(Register32.EBX)
                                .constant(2)
                                .displacement((short) 0x1234),
                        "[eax+ebx*2+0x1234]"),
                Arguments.of(
                        IndirectOperand.builder()
                                .reg1(Register32.EAX)
                                .reg2(Register32.EBX)
                                .constant(2)
                                .displacement(0x12345678),
                        "[eax+ebx*2+0x12345678]"),
                Arguments.of(
                        IndirectOperand.builder()
                                .reg1(Register32.EAX)
                                .reg2(Register32.EBX)
                                .constant(2)
                                .displacement(0x1123456789abcdefL),
                        "[eax+ebx*2+0x1123456789abcdef]"));
    }

    @ParameterizedTest
    @MethodSource("correctIndirectOperands")
    void correct(final IndirectOperand.IndirectOperandBuilder iob, final String expected) {
        final IndirectOperand io = iob.build();
        assertEquals(expected, io.toIntelSyntax());
    }

    private static Stream<Arguments> wrongIndirectOperands() {
        return Stream.of(
                        IndirectOperand.builder().constant(2),
                        IndirectOperand.builder()
                                .reg1(Register32.EAX)
                                .reg2(Register32.EBX), // should specify constant=1
                        IndirectOperand.builder().constant(2).displacement(0x12345678))
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("wrongIndirectOperands")
    void correct(final IndirectOperand.IndirectOperandBuilder iob) {
        assertThrows(IllegalStateException.class, iob::build);
    }
}
