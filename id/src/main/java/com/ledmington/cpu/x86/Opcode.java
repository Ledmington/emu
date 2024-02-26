package com.ledmington.cpu.x86;

import java.util.Objects;

public enum Opcode {
    CALL("call"),
    CDQ("cdq"),
    INT3("int3"),
    JE("je"),
    JMP("jmp"),
    LEA("lea"),
    LEAVE("leave"),
    MOV("mov"),
    NOP("nop"),
    POP("pop"),
    PUSH("push"),
    RET("ret"),
    SHL("shl"),
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
