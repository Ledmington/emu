package com.ledmington.cpu.x86.exc;

import java.io.Serial;

public final class UnknownOpcode extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 2581758152120570603L;

    public UnknownOpcode(final byte b) {
        super(String.format("Unknown opcode 0x%02x", b));
    }

    public UnknownOpcode(final byte b1, final byte b2) {
        super(String.format("Unknown opcode 0x%02x%02x", b1, b2));
    }
}
