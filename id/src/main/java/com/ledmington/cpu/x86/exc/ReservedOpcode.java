package com.ledmington.cpu.x86.exc;

public final class ReservedOpcode extends RuntimeException {
    public ReservedOpcode(final byte b1, final byte b2) {
        super(String.format("Reserved opcode 0x%02x%02x", b1, b1));
    }
}
