package com.ledmington.cpu.x86;

import java.util.Objects;

public enum Opcode {
    XOR("xor"),
    JMP("jmp"),
    CALL("call"),
    MOV("mov");

    private final String mnemonic;

    Opcode(final String mnemonic) {
        this.mnemonic = Objects.requireNonNull(mnemonic);
    }

    public String mnemonic() {
        return mnemonic;
    }
}
