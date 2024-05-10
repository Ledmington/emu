package com.ledmington.cpu.x86;

import java.util.Objects;

public final class SegmentRegister extends Register {

    private final Register16 seg;
    private final Register reg;

    public SegmentRegister(final Register16 segment, final Register register) {
        super(Objects.requireNonNull(register).toIntelSyntax());
        this.seg = Objects.requireNonNull(segment);
        this.reg = register;
    }

    public Register16 segment() {
        return seg;
    }

    public Register register() {
        return reg;
    }

    @Override
    public int bits() {
        // FIXME
        throw new Error("Not implemented");
    }
}
