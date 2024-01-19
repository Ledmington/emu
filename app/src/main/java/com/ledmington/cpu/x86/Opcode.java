package com.ledmington.cpu.x86;

import java.util.Objects;

public enum Opcode {
    XOR("xor"),
    JMP("jmp"),
    JE("je"),
    CALL("call"),
    MOV("mov"),
    TEST("test"),
    NOP("nop");

    private final String mnemonic;

    Opcode(final String mnemonic) {
        this.mnemonic = Objects.requireNonNull(mnemonic);
    }

    public String mnemonic() {
        return mnemonic;
    }
}
