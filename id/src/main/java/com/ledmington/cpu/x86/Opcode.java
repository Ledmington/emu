package com.ledmington.cpu.x86;

import java.util.Objects;

public enum Opcode {
    ADD("add"),
    AND("and"),
    CALL("call"),
    CDQ("cdq"),
    CMOVE("cmove"),
    CMP("cmp"),
    INT3("int3"),
    JA("ja"),
    JE("je"),
    JG("jg"),
    JMP("jmp"),
    LEA("lea"),
    LEAVE("leave"),
    MOV("mov"),
    NOP("nop"),
    POP("pop"),
    PUSH("push"),
    RET("ret"),
    SHL("shl"),
    SUB("sub"),
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
