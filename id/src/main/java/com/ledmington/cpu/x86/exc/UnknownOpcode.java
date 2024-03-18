package com.ledmington.cpu.x86.exc;

public final class UnknownOpcode extends RuntimeException {
    public UnknownOpcode(final byte b) {
        super(String.format("Unknown opcode 0x%02x", b));
    }

    public UnknownOpcode(final byte b1, final byte b2) {
        super(String.format("Unknown opcode 0x%02x%02x", b1, b2));
    }
}
