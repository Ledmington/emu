package com.ledmington.cpu.x86;

import java.util.Objects;

public abstract class Register implements Operand {

    protected final String mnemonic;

    protected Register(final String mnemonic) {
        this.mnemonic = Objects.requireNonNull(mnemonic);
    }

    public abstract int bits();

    public String toIntelSyntax() {
        return mnemonic;
    }

    public String toString() {
        return mnemonic;
    }
}
