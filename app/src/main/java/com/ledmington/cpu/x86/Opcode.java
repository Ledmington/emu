package com.ledmington.cpu.x86;

import java.util.Objects;

public enum Opcode {
    ADD("add"),
    CALL("call"),
    CMOVE("cmove"),
    CMP("cmp"),
    JE("je"),
    JMP("jmp"),
    LEA("lea"),
    MOV("mov"),
    NOP("nop"),
    PUSH("push"),
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
