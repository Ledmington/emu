package com.ledmington.cpu.x86;

public enum InstructionPrefix {
    LOCK((byte) 0xf0),

    /**
     * REPNE / REPNZ
     */
    REPNZ((byte) 0xf2),

    /**
     * REP / REPE / REPZ
     */
    REP((byte) 0xf3);

    public final byte code;

    InstructionPrefix(final byte code) {
        this.code = code;
    }

    public static InstructionPrefix fromByte(final byte x) {
        return switch (x) {
            case (byte) 0xf0 -> LOCK;
            case (byte) 0xf2 -> REPNZ;
            case (byte) 0xf3 -> REP;
            default -> throw new IllegalArgumentException();
        };
    }
}
