package com.ledmington.cpu.x86;

import java.util.Objects;

public enum Opcode {
    XOR("xor"),
    JMP("jmp"),
    JE("je"),
    CALL("call"),
    MOV("mov"),
    TEST("test"),
    NOP("nop"),
    LEA("lea"),
    CMOVE("cmove"),
    ADD("add"),
    CMP("cmp");

    private final String mnemonic;

    Opcode(final String mnemonic) {
        this.mnemonic = Objects.requireNonNull(mnemonic);
    }

    public String mnemonic() {
        return mnemonic;
    }
}
