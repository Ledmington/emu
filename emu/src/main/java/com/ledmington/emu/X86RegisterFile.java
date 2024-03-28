package com.ledmington.emu;

import com.ledmington.cpu.x86.Register8;
import com.ledmington.utils.BitUtils;

public final class X86RegisterFile {

    // General-purpose registers
    private final long[] gpr = new long[16];

    public X86RegisterFile() {}

    public byte get(final Register8 r) {
        return switch (r.toIntelSyntax()) {
            case "al" -> BitUtils.asByte(gpr[0]);
            default -> throw new IllegalArgumentException(
                    String.format("Invalid 8-bit register '%s'", r.toIntelSyntax()));
        };
    }

    public void write(final Register8 r, final byte v) {
        switch (r.toIntelSyntax()) {
            case "al" -> gpr[0] = (gpr[0] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            default -> throw new IllegalArgumentException(
                    String.format("Invalid 8-bit register '%s'", r.toIntelSyntax()));
        }
        ;
    }
}
