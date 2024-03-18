package com.ledmington.cpu.x86;

import java.util.Objects;

/**
 * X86 opcode mnemonics.
 */
public enum Opcode {

    /**
     * Add with carry.
     */
    ADC("adc"),

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
     * Conditional move if below or equal.
     */
    CMOVBE("cmovbe"),

    /**
     * Conditional move if equal.
     */
    CMOVE("cmove"),

    /**
     * Conditional move if not equal.
     */
    CMOVNE("cmovne"),

    /**
     * Conditional move if signed.
     */
    CMOVS("cmovs"),

    /**
     * Compare two operands.
     */
    CMP("cmp"),

    /**
     * Convert word to doubleword.
     */
    CWDE("cwde"),

    /**
     * Terminate an indirect branch in 32-bit mode.
     */
    ENDBR32("endbr32"),

    /**
     * Terminate an indirect branch in 32-bit mode.
     */
    ENDBR64("endbr64"),

    /**
     * Signed divide.
     */
    IDIV("idiv"),

    /**
     * Signed multiply.
     */
    IMUL("imul"),

    /**
     * Increment.
     */
    INC("inc"),

    /**
     * Call to interrupt procedure.
     */
    INT3("int3"),

    /**
     * Unsigned conditional jump if above.
     */
    JA("ja"),

    /**
     * Unsigned conditional jump if below.
     */
    JB("jb"),

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
     * Signed conditional jump if less.
     */
    JL("jl"),

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
     * Move a 64-bit immediate into a 64-bit register.
     */
    MOVABS("movabs"),

    /**
     * Move aligned packed single floating-point values.
     */
    MOVAPS("movaps"),

    /**
     * Move aligned double-quadword.
     */
    MOVDQA("movdqa"),

    /**
     * Move quadword.
     */
    MOVQ("movq"),

    /**
     * Move string.
     */
    MOVS("movs"),

    /**
     * Move double-word string.
     */
    MOVSD("movsd"),

    /**
     * Move and sign-extend.
     */
    MOVSX("movsx"),

    /**
     * Move and sign-extend doubleword.
     */
    MOVSXD("movsxd"),

    /**
     * Move unaligned packed single precision floating-point values into XMM register.
     */
    MOVUPS("movups"),

    /**
     * Move and zero-extend.
     */
    MOVZX("movzx"),

    /**
     * Two's complement negation.
     */
    NEG("neg"),

    /**
     * No operation.
     */
    NOP("nop"),

    /**
     * Logical NOT.
     */
    NOT("not"),

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
     * Interleave low-order quadword from xmm1 and xmm2/m128 into xmm1 register.
     */
    PUNPCKLQDQ("punpcklqdq"),

    /**
     * Shuffle packed doublewords.
     */
    PSHUFD("pshufd"),

    /**
     * Shuffle packed integer word in MMX register.
     */
    PSHUFW("pshufw"),

    /**
     * Return from procedure.
     */
    RET("ret"),

    /**
     * Integer subtraction with borrow.
     */
    SBB("sbb"),

    /**
     * Set byte if equal.
     */
    SETE("sete"),

    /**
     * Set byte if not equal.
     */
    SETNE("setne"),

    /**
     * Logical shift left.
     */
    SHL("shl"),

    /**
     * Logical shift right.
     */
    SHR("shr"),

    /**
     * Store string.
     */
    STOS("stos"),

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
