package com.ledmington.emu;

import com.ledmington.cpu.x86.Register64;
import com.ledmington.cpu.x86.Register8;
import com.ledmington.utils.BitUtils;

public final class X86RegisterFile {

    // General-purpose registers
    private final long[] gpr = new long[16];

    public X86RegisterFile() {}

    public byte get(final Register8 r) {
        return switch (r.toIntelSyntax()) {
            case "al" -> BitUtils.asByte(gpr[0]);
            case "bl" -> BitUtils.asByte(gpr[1]);
            case "cl" -> BitUtils.asByte(gpr[2]);
            case "dl" -> BitUtils.asByte(gpr[3]);
            case "ah" -> (byte) ((gpr[0] & 0x000000000000ff00L) >>> 8);
            case "bh" -> (byte) ((gpr[1] & 0x000000000000ff00L) >>> 8);
            case "ch" -> (byte) ((gpr[2] & 0x000000000000ff00L) >>> 8);
            case "dh" -> (byte) ((gpr[3] & 0x000000000000ff00L) >>> 8);
            case "spl" -> BitUtils.asByte(gpr[4]);
            case "bpl" -> BitUtils.asByte(gpr[5]);
            case "dil" -> BitUtils.asByte(gpr[6]);
            case "sil" -> BitUtils.asByte(gpr[7]);
            case "r8b" -> BitUtils.asByte(gpr[8]);
            case "r9b" -> BitUtils.asByte(gpr[9]);
            case "r10b" -> BitUtils.asByte(gpr[10]);
            case "r11b" -> BitUtils.asByte(gpr[11]);
            case "r12b" -> BitUtils.asByte(gpr[12]);
            case "r13b" -> BitUtils.asByte(gpr[13]);
            case "r14b" -> BitUtils.asByte(gpr[14]);
            case "r15b" -> BitUtils.asByte(gpr[15]);
            default -> throw new IllegalArgumentException(
                    String.format("Invalid 8-bit register '%s'", r.toIntelSyntax()));
        };
    }

    public void set(final Register8 r, final byte v) {
        switch (r.toIntelSyntax()) {
            case "al" -> gpr[0] = (gpr[0] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case "bl" -> gpr[1] = (gpr[1] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case "cl" -> gpr[2] = (gpr[2] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case "dl" -> gpr[3] = (gpr[3] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case "ah" -> gpr[0] = (gpr[0] & 0xffffffffffff00ffL) | (BitUtils.asLong(v) << 8);
            case "bh" -> gpr[1] = (gpr[1] & 0xffffffffffff00ffL) | (BitUtils.asLong(v) << 8);
            case "ch" -> gpr[2] = (gpr[2] & 0xffffffffffff00ffL) | (BitUtils.asLong(v) << 8);
            case "dh" -> gpr[3] = (gpr[3] & 0xffffffffffff00ffL) | (BitUtils.asLong(v) << 8);
            case "spl" -> gpr[4] = (gpr[4] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case "bpl" -> gpr[5] = (gpr[5] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case "dil" -> gpr[6] = (gpr[6] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case "sil" -> gpr[7] = (gpr[7] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case "r8b" -> gpr[8] = (gpr[8] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case "r9b" -> gpr[9] = (gpr[9] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case "r10b" -> gpr[10] = (gpr[10] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case "r11b" -> gpr[11] = (gpr[11] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case "r12b" -> gpr[12] = (gpr[12] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case "r13b" -> gpr[13] = (gpr[13] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case "r14b" -> gpr[14] = (gpr[14] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case "r15b" -> gpr[15] = (gpr[15] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            default -> throw new IllegalArgumentException(
                    String.format("Invalid 8-bit register '%s'", r.toIntelSyntax()));
        }
    }

    public long get(final Register64 r) {
        return switch (r.toIntelSyntax()) {
            case "rax" -> gpr[0];
            case "rbx" -> gpr[1];
            case "rcx" -> gpr[2];
            case "rdx" -> gpr[3];
            case "rsp" -> gpr[4];
            case "rbp" -> gpr[5];
            case "rsi" -> gpr[6];
            case "rdi" -> gpr[7];
            case "r8" -> gpr[8];
            case "r9" -> gpr[9];
            case "r10" -> gpr[10];
            case "r11" -> gpr[11];
            case "r12" -> gpr[12];
            case "r13" -> gpr[13];
            case "r14" -> gpr[14];
            case "r15" -> gpr[15];
            default -> throw new IllegalArgumentException(
                    String.format("Invalid 64-bit register '%s'", r.toIntelSyntax()));
        };
    }

    public void set(final Register64 r, final long v) {
        switch (r.toIntelSyntax()) {
            case "rax" -> gpr[0] = v;
            case "rbx" -> gpr[1] = v;
            case "rcx" -> gpr[2] = v;
            case "rdx" -> gpr[3] = v;
            case "rsp" -> gpr[4] = v;
            case "rbp" -> gpr[5] = v;
            case "rsi" -> gpr[6] = v;
            case "rdi" -> gpr[7] = v;
            case "r8" -> gpr[8] = v;
            case "r9" -> gpr[9] = v;
            case "r10" -> gpr[10] = v;
            case "r11" -> gpr[11] = v;
            case "r12" -> gpr[12] = v;
            case "r13" -> gpr[13] = v;
            case "r14" -> gpr[14] = v;
            case "r15" -> gpr[15] = v;
            default -> throw new IllegalArgumentException(
                    String.format("Invalid 64-bit register '%s'", r.toIntelSyntax()));
        }
    }
}
