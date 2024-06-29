/*
* emu - Processor Emulator
* Copyright (C) 2023-2024 Filippo Barbari <filippo.barbari@gmail.com>
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.ledmington.emu;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.ledmington.cpu.x86.Register16;
import com.ledmington.cpu.x86.Register32;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.cpu.x86.Register8;
import com.ledmington.utils.BitUtils;
import com.ledmington.utils.HashUtils;

/** This class represents the set of registers used by an x86-64 processor during execution. */
public final class X86RegisterFile {

    // General-purpose registers
    private final long[] gpr = new long[16];

    // Segment registers
    private final short[] seg = new short[6];

    // instruction pointer
    private long rip;

    /** Creates the register file initializing every register to 0. */
    public X86RegisterFile() {}

    /**
     * Returns the value of the given 8-bit register as a byte.
     *
     * @param r The register to be read.
     * @return The value in the register.
     */
    public byte get(final Register8 r) {
        return switch (r) {
            case AL -> BitUtils.asByte(gpr[0]);
            case BL -> BitUtils.asByte(gpr[1]);
            case CL -> BitUtils.asByte(gpr[2]);
            case DL -> BitUtils.asByte(gpr[3]);
            case AH -> (byte) ((gpr[0] & 0x000000000000ff00L) >>> 8);
            case BH -> (byte) ((gpr[1] & 0x000000000000ff00L) >>> 8);
            case CH -> (byte) ((gpr[2] & 0x000000000000ff00L) >>> 8);
            case DH -> (byte) ((gpr[3] & 0x000000000000ff00L) >>> 8);
            case SPL -> BitUtils.asByte(gpr[4]);
            case BPL -> BitUtils.asByte(gpr[5]);
            case DIL -> BitUtils.asByte(gpr[6]);
            case SIL -> BitUtils.asByte(gpr[7]);
            case R8B -> BitUtils.asByte(gpr[8]);
            case R9B -> BitUtils.asByte(gpr[9]);
            case R10B -> BitUtils.asByte(gpr[10]);
            case R11B -> BitUtils.asByte(gpr[11]);
            case R12B -> BitUtils.asByte(gpr[12]);
            case R13B -> BitUtils.asByte(gpr[13]);
            case R14B -> BitUtils.asByte(gpr[14]);
            case R15B -> BitUtils.asByte(gpr[15]);
        };
    }

    /**
     * Sets the value of the given 8-bit register to given byte. This operation does not modify the other registers.
     *
     * @param r The Register to be overwritten.
     * @param v The value to be written.
     */
    public void set(final Register8 r, final byte v) {
        switch (r) {
            case AL -> gpr[0] = (gpr[0] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case BL -> gpr[1] = (gpr[1] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case CL -> gpr[2] = (gpr[2] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case DL -> gpr[3] = (gpr[3] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case AH -> gpr[0] = (gpr[0] & 0xffffffffffff00ffL) | (BitUtils.asLong(v) << 8);
            case BH -> gpr[1] = (gpr[1] & 0xffffffffffff00ffL) | (BitUtils.asLong(v) << 8);
            case CH -> gpr[2] = (gpr[2] & 0xffffffffffff00ffL) | (BitUtils.asLong(v) << 8);
            case DH -> gpr[3] = (gpr[3] & 0xffffffffffff00ffL) | (BitUtils.asLong(v) << 8);
            case SPL -> gpr[4] = (gpr[4] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case BPL -> gpr[5] = (gpr[5] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case DIL -> gpr[6] = (gpr[6] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case SIL -> gpr[7] = (gpr[7] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case R8B -> gpr[8] = (gpr[8] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case R9B -> gpr[9] = (gpr[9] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case R10B -> gpr[10] = (gpr[10] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case R11B -> gpr[11] = (gpr[11] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case R12B -> gpr[12] = (gpr[12] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case R13B -> gpr[13] = (gpr[13] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case R14B -> gpr[14] = (gpr[14] & 0xffffffffffffff00L) | BitUtils.asLong(v);
            case R15B -> gpr[15] = (gpr[15] & 0xffffffffffffff00L) | BitUtils.asLong(v);
        }
    }

    public short get(final Register16 r) {
        return switch (r) {
            case AX -> BitUtils.asShort(gpr[0]);
            case BX -> BitUtils.asShort(gpr[1]);
            case CX -> BitUtils.asShort(gpr[2]);
            case DX -> BitUtils.asShort(gpr[3]);
            case SP -> BitUtils.asShort(gpr[4]);
            case BP -> BitUtils.asShort(gpr[5]);
            case SI -> BitUtils.asShort(gpr[6]);
            case DI -> BitUtils.asShort(gpr[7]);
            case R8W -> BitUtils.asShort(gpr[8]);
            case R9W -> BitUtils.asShort(gpr[9]);
            case R10W -> BitUtils.asShort(gpr[10]);
            case R11W -> BitUtils.asShort(gpr[11]);
            case R12W -> BitUtils.asShort(gpr[12]);
            case R13W -> BitUtils.asShort(gpr[13]);
            case R14W -> BitUtils.asShort(gpr[14]);
            case R15W -> BitUtils.asShort(gpr[15]);
            case CS -> seg[0];
            case DS -> seg[1];
            case ES -> seg[2];
            case FS -> seg[3];
            case GS -> seg[4];
            case SS -> seg[5];
        };
    }

    public void set(final Register16 r, final short v) {
        switch (r) {
            case AX -> gpr[0] = (gpr[0] & 0xffffffffff0000L) | BitUtils.asLong(v);
            case BX -> gpr[1] = (gpr[1] & 0xffffffffff0000L) | BitUtils.asLong(v);
            case CX -> gpr[2] = (gpr[2] & 0xffffffffff0000L) | BitUtils.asLong(v);
            case DX -> gpr[3] = (gpr[3] & 0xffffffffff0000L) | BitUtils.asLong(v);
            case SP -> gpr[4] = (gpr[4] & 0xffffffffff0000L) | BitUtils.asLong(v);
            case BP -> gpr[5] = (gpr[5] & 0xffffffffff0000L) | BitUtils.asLong(v);
            case SI -> gpr[6] = (gpr[6] & 0xffffffffff0000L) | BitUtils.asLong(v);
            case DI -> gpr[7] = (gpr[7] & 0xffffffffff0000L) | BitUtils.asLong(v);
            case R8W -> gpr[8] = (gpr[8] & 0xffffffffff0000L) | BitUtils.asLong(v);
            case R9W -> gpr[9] = (gpr[9] & 0xffffffffff0000L) | BitUtils.asLong(v);
            case R10W -> gpr[10] = (gpr[10] & 0xffffffffff0000L) | BitUtils.asLong(v);
            case R11W -> gpr[11] = (gpr[11] & 0xffffffffff0000L) | BitUtils.asLong(v);
            case R12W -> gpr[12] = (gpr[12] & 0xffffffffff0000L) | BitUtils.asLong(v);
            case R13W -> gpr[13] = (gpr[13] & 0xffffffffff0000L) | BitUtils.asLong(v);
            case R14W -> gpr[14] = (gpr[14] & 0xffffffffff0000L) | BitUtils.asLong(v);
            case R15W -> gpr[15] = (gpr[15] & 0xffffffffff0000L) | BitUtils.asLong(v);
            case CS -> seg[0] = v;
            case DS -> seg[1] = v;
            case ES -> seg[2] = v;
            case FS -> seg[3] = v;
            case GS -> seg[4] = v;
            case SS -> seg[5] = v;
        }
    }

    /**
     * Returns the value of the given 32-bit register as an int.
     *
     * @param r The Register to be read.
     * @return The value of the register.
     */
    public int get(final Register32 r) {
        return switch (r) {
            case EAX -> BitUtils.asInt(gpr[0]);
            case EBX -> BitUtils.asInt(gpr[1]);
            case ECX -> BitUtils.asInt(gpr[2]);
            case EDX -> BitUtils.asInt(gpr[3]);
            case ESP -> BitUtils.asInt(gpr[4]);
            case EBP -> BitUtils.asInt(gpr[5]);
            case ESI -> BitUtils.asInt(gpr[6]);
            case EDI -> BitUtils.asInt(gpr[7]);
            case R8D -> BitUtils.asInt(gpr[8]);
            case R9D -> BitUtils.asInt(gpr[9]);
            case R10D -> BitUtils.asInt(gpr[10]);
            case R11D -> BitUtils.asInt(gpr[11]);
            case R12D -> BitUtils.asInt(gpr[12]);
            case R13D -> BitUtils.asInt(gpr[13]);
            case R14D -> BitUtils.asInt(gpr[14]);
            case R15D -> BitUtils.asInt(gpr[15]);
            case EIP -> BitUtils.asInt(rip);
        };
    }

    /**
     * Sets the value of the given 32-bit register to given int. This operation does not modify the other registers.
     *
     * @param r The Register to be overwritten.
     * @param v The value to be written.
     */
    public void set(final Register32 r, final int v) {
        switch (r) {
            case EAX -> gpr[0] = (gpr[0] & 0xffffffff00000000L) | BitUtils.asLong(v);
            case EBX -> gpr[1] = (gpr[1] & 0xffffffff00000000L) | BitUtils.asLong(v);
            case ECX -> gpr[2] = (gpr[2] & 0xffffffff00000000L) | BitUtils.asLong(v);
            case EDX -> gpr[3] = (gpr[3] & 0xffffffff00000000L) | BitUtils.asLong(v);
            case ESP -> gpr[4] = (gpr[4] & 0xffffffff00000000L) | BitUtils.asLong(v);
            case EBP -> gpr[5] = (gpr[5] & 0xffffffff00000000L) | BitUtils.asLong(v);
            case ESI -> gpr[6] = (gpr[6] & 0xffffffff00000000L) | BitUtils.asLong(v);
            case EDI -> gpr[7] = (gpr[7] & 0xffffffff00000000L) | BitUtils.asLong(v);
            case R8D -> gpr[8] = (gpr[8] & 0xffffffff00000000L) | BitUtils.asLong(v);
            case R9D -> gpr[9] = (gpr[9] & 0xffffffff00000000L) | BitUtils.asLong(v);
            case R10D -> gpr[10] = (gpr[10] & 0xffffffff00000000L) | BitUtils.asLong(v);
            case R11D -> gpr[11] = (gpr[11] & 0xffffffff00000000L) | BitUtils.asLong(v);
            case R12D -> gpr[12] = (gpr[12] & 0xffffffff00000000L) | BitUtils.asLong(v);
            case R13D -> gpr[13] = (gpr[13] & 0xffffffff00000000L) | BitUtils.asLong(v);
            case R14D -> gpr[14] = (gpr[14] & 0xffffffff00000000L) | BitUtils.asLong(v);
            case R15D -> gpr[15] = (gpr[15] & 0xffffffff00000000L) | BitUtils.asLong(v);
            case EIP -> rip = (rip & 0xffffffff00000000L) | BitUtils.asLong(v);
        }
    }

    /**
     * Returns the value of the given 64-bit register as a long.
     *
     * @param r The Register to be read.
     * @return The value of the register.
     */
    public long get(final Register64 r) {
        return switch (r) {
            case RAX -> gpr[0];
            case RBX -> gpr[1];
            case RCX -> gpr[2];
            case RDX -> gpr[3];
            case RSP -> gpr[4];
            case RBP -> gpr[5];
            case RSI -> gpr[6];
            case RDI -> gpr[7];
            case R8 -> gpr[8];
            case R9 -> gpr[9];
            case R10 -> gpr[10];
            case R11 -> gpr[11];
            case R12 -> gpr[12];
            case R13 -> gpr[13];
            case R14 -> gpr[14];
            case R15 -> gpr[15];
            case RIP -> rip;
        };
    }

    /**
     * Sets the value of the given 64-bit register to given long. This operation does not modify the other registers.
     *
     * @param r The Register to be overwritten.
     * @param v The value to be written.
     */
    public void set(final Register64 r, final long v) {
        switch (r) {
            case RAX -> gpr[0] = v;
            case RBX -> gpr[1] = v;
            case RCX -> gpr[2] = v;
            case RDX -> gpr[3] = v;
            case RSP -> gpr[4] = v;
            case RBP -> gpr[5] = v;
            case RSI -> gpr[6] = v;
            case RDI -> gpr[7] = v;
            case R8 -> gpr[8] = v;
            case R9 -> gpr[9] = v;
            case R10 -> gpr[10] = v;
            case R11 -> gpr[11] = v;
            case R12 -> gpr[12] = v;
            case R13 -> gpr[13] = v;
            case R14 -> gpr[14] = v;
            case R15 -> gpr[15] = v;
            case RIP -> rip = v;
        }
    }

    @Override
    public String toString() {
        return "X86RegisterFile("
                + Arrays.stream(Register64.values())
                        .map(r -> String.format("%s=0x%016x", r.toIntelSyntax(), get(r)))
                        .collect(Collectors.joining(";"))
                + ")";
    }

    @Override
    public int hashCode() {
        int h = 17;
        for (final long r : gpr) {
            h = 31 * h + HashUtils.hash(r);
        }
        h = 31 * h + HashUtils.hash(rip);
        return h;
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (!this.getClass().equals(other.getClass())) {
            return false;
        }
        final X86RegisterFile regs = (X86RegisterFile) other;
        return Arrays.equals(this.gpr, regs.gpr) && this.rip == regs.rip;
    }
}
