package com.ledmington.cpu.x86;

import java.util.Objects;

public final class SegmentRegister extends Register {

    private final Register16 segment;
    private final Register register;

    public SegmentRegister(final Register16 segment, final Register register) {
        super(Objects.requireNonNull(register).toIntelSyntax());
        this.segment = Objects.requireNonNull(segment);
        this.register = register;
    }

    public Register16 segment() {
        return segment;
    }

    public Register register() {
        return register;
    }

    @Override
    public int bits() {
        // FIXME
        return 0;
    }
}
