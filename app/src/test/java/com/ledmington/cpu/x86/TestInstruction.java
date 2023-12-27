package com.ledmington.cpu.x86;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ledmington.elf.ByteBuffer;

import org.junit.jupiter.api.Test;

public final class TestInstruction {
    @Test
    void endbr64() {
        assertEquals(
                null, new Instruction(new ByteBuffer(new byte[] {(byte) 0xf3, (byte) 0x0f, (byte) 0x1e, (byte) 0xfa})));
    }
}
