package com.ledmington.cpu.x86;

import java.util.Objects;

public enum Opcode {
    CDQ("cdq"),
    INT3("int3"),
    LEA("lea"),
    LEAVE("leave"),
    MOV("mov"),
    NOP("nop"),
    POP("pop"),
    PUSH("push"),
    RET("ret"),
    TEST("test"),
    XOR("xor");

    private final String mnemonic;

    Opcode(final String mnemonic) {
        this.mnemonic = Objects.requireNonNull(mnemonic);
    }

    public String mnemonic() {
        return mnemonic;
    }
}
