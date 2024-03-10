package com.ledmington.cpu.x86;

import java.util.Objects;

/**
 * X86 opcode mnemonics.
 */
public enum Opcode {

    /**
     * Add.
     */
    ADD("add"),

    /**
     * Logical AND.
     */
    AND("and"),

    /**
     * Call procedure.
     */
    CALL("call"),

    /**
     * Convert doubleword to quadword.
     */
    CDQ("cdq"),

    /**
     * Convert doubleword to quadword.
     */
    CDQE("cdqe"),

    /**
     * Convert word to doubleword.
     */
    CWDE("cwde"),

    /**
     * Conditional move if equal.
     */
    CMOVE("cmove"),

    /**
     * Compare two operands.
     */
    CMP("cmp"),

    /**
     * Signed multiply.
     */
    IMUL("imul"),

    /**
     * Call to interrupt procedure.
     */
    INT3("int3"),

    /**
     * Unsigned conditional jump if above.
     */
    JA("ja"),

    /**
     * Unsigned conditional jump if below or equal.
     */
    JBE("jbe"),

    /**
     * Unsigned conditional jump if equal.
     */
    JE("je"),

    /**
     * Signed conditional jump if greater.
     */
    JG("jg"),

    /**
     * Signed conditional jump if less or equal.
     */
    JLE("jle"),

    /**
     * Unconditional jump.
     */
    JMP("jmp"),

    /**
     * Unsigned conditional jump if not equal.
     */
    JNE("jne"),

    /**
     * Conditional jump if not signed.
     */
    JNS("jns"),

    /**
     * Conditional jump if signed.
     */
    JS("js"),

    /**
     * Load effective address.
     */
    LEA("lea"),

    /**
     * High-level procedure exit.
     */
    LEAVE("leave"),

    /**
     * Move to/from registers and memory.
     */
    MOV("mov"),

    /**
     * Move aligned double-quadword.
     */
    MOVDQA("movdqa"),

    /**
     * Move string.
     */
    MOVS("movs"),

    /**
     * Move and sign-extend.
     */
    MOVSX("movsx"),

    /**
     * Move and zero-extend.
     */
    MOVZX("movzx"),

    /**
     * No operation.
     */
    NOP("nop"),

    /**
     * Logical OR.
     */
    OR("or"),

    /**
     * Pop a value from the stack.
     */
    POP("pop"),

    /**
     * Push word, doubleword or quadword onto the stack.
     */
    PUSH("push"),

    /**
     * Return from procedure.
     */
    RET("ret"),

    /**
     * Integer subtraction with borrow.
     */
    SBB("sbb"),

    /**
     * Logical shift left.
     */
    SHL("shl"),

    /**
     * Logical shift right.
     */
    SHR("shr"),

    /**
     * Subtract.
     */
    SUB("sub"),

    /**
     * Logical compare.
     */
    TEST("test"),

    /**
     * Undefined instruction.
     */
    UD2("ud2"),

    /**
     * Logical XOR.
     */
    XOR("xor");

    private final String mnemonic;

    Opcode(final String mnemonic) {
        this.mnemonic = Objects.requireNonNull(mnemonic);
    }

    public String mnemonic() {
        return mnemonic;
    }
}
