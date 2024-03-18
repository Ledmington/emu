package com.ledmington.cpu.x86.exc;

import java.io.Serial;

public final class ReservedOpcode extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 5003238481425708141L;

    public ReservedOpcode(final byte b1, final byte b2) {
        super(String.format("Reserved opcode 0x%02x%02x", b1, b2));
    }
}
