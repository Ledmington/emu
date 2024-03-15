package com.ledmington.cpu.x86;

import java.util.Objects;

/**
 * Base class for all x86 register types.
 */
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
