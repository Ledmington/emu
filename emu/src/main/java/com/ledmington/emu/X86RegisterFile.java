/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2025 Filippo Barbari <filippo.barbari@gmail.com>
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

import static com.ledmington.utils.BitUtils.asByte;
import static com.ledmington.utils.BitUtils.asInt;
import static com.ledmington.utils.BitUtils.asLong;
import static com.ledmington.utils.BitUtils.asShort;
import static com.ledmington.utils.BitUtils.shl;
import static com.ledmington.utils.BitUtils.shr;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ledmington.cpu.x86.Register16;
import com.ledmington.cpu.x86.Register32;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.cpu.x86.Register8;
import com.ledmington.utils.HashUtils;

/** This class represents the set of registers used by an x86-64 processor during execution. */
public final class X86RegisterFile implements RegisterFile {

	// General-purpose registers
	private final long[] gpr = new long[16];

	// Segment registers
	private final short[] seg = new short[6];

	// instruction pointer
	private long rip;

	private long rflags = 0L;

	/** Creates the register file initializing every register to 0. */
	public X86RegisterFile() {}

	/**
	 * Creates a register file by copying the one given in input.
	 *
	 * @param regFile The register file to be copied.
	 */
	public X86RegisterFile(final ImmutableRegisterFile regFile) {
		Objects.requireNonNull(regFile);
		final X86RegisterFile regs = (X86RegisterFile) regFile;
		System.arraycopy(regs.gpr, 0, this.gpr, 0, 16);
		System.arraycopy(regs.seg, 0, this.seg, 0, 6);
		this.rip = regs.rip;
		this.rflags = regs.rflags;
	}

	@Override
	public byte get(final Register8 r) {
		return switch (r) {
			case AL -> asByte(gpr[0]);
			case BL -> asByte(gpr[1]);
			case CL -> asByte(gpr[2]);
			case DL -> asByte(gpr[3]);
			case AH -> asByte(shr(gpr[0], 8));
			case BH -> asByte(shr(gpr[1], 8));
			case CH -> asByte(shr(gpr[2], 8));
			case DH -> asByte(shr(gpr[3], 8));
			case SPL -> asByte(gpr[4]);
			case BPL -> asByte(gpr[5]);
			case DIL -> asByte(gpr[6]);
			case SIL -> asByte(gpr[7]);
			case R8B -> asByte(gpr[8]);
			case R9B -> asByte(gpr[9]);
			case R10B -> asByte(gpr[10]);
			case R11B -> asByte(gpr[11]);
			case R12B -> asByte(gpr[12]);
			case R13B -> asByte(gpr[13]);
			case R14B -> asByte(gpr[14]);
			case R15B -> asByte(gpr[15]);
		};
	}

	@Override
	public void set(final Register8 r, final byte v) {
		switch (r) {
			case AL -> gpr[0] = (gpr[0] & 0xffffffffffffff00L) | asLong(v);
			case BL -> gpr[1] = (gpr[1] & 0xffffffffffffff00L) | asLong(v);
			case CL -> gpr[2] = (gpr[2] & 0xffffffffffffff00L) | asLong(v);
			case DL -> gpr[3] = (gpr[3] & 0xffffffffffffff00L) | asLong(v);
			case AH -> gpr[0] = (gpr[0] & 0xffffffffffff00ffL) | shl(asLong(v), 8);
			case BH -> gpr[1] = (gpr[1] & 0xffffffffffff00ffL) | shl(asLong(v), 8);
			case CH -> gpr[2] = (gpr[2] & 0xffffffffffff00ffL) | shl(asLong(v), 8);
			case DH -> gpr[3] = (gpr[3] & 0xffffffffffff00ffL) | shl(asLong(v), 8);
			case SPL -> gpr[4] = (gpr[4] & 0xffffffffffffff00L) | asLong(v);
			case BPL -> gpr[5] = (gpr[5] & 0xffffffffffffff00L) | asLong(v);
			case DIL -> gpr[6] = (gpr[6] & 0xffffffffffffff00L) | asLong(v);
			case SIL -> gpr[7] = (gpr[7] & 0xffffffffffffff00L) | asLong(v);
			case R8B -> gpr[8] = (gpr[8] & 0xffffffffffffff00L) | asLong(v);
			case R9B -> gpr[9] = (gpr[9] & 0xffffffffffffff00L) | asLong(v);
			case R10B -> gpr[10] = (gpr[10] & 0xffffffffffffff00L) | asLong(v);
			case R11B -> gpr[11] = (gpr[11] & 0xffffffffffffff00L) | asLong(v);
			case R12B -> gpr[12] = (gpr[12] & 0xffffffffffffff00L) | asLong(v);
			case R13B -> gpr[13] = (gpr[13] & 0xffffffffffffff00L) | asLong(v);
			case R14B -> gpr[14] = (gpr[14] & 0xffffffffffffff00L) | asLong(v);
			case R15B -> gpr[15] = (gpr[15] & 0xffffffffffffff00L) | asLong(v);
		}
	}

	@Override
	public short get(final Register16 r) {
		return switch (r) {
			case AX -> asShort(gpr[0]);
			case BX -> asShort(gpr[1]);
			case CX -> asShort(gpr[2]);
			case DX -> asShort(gpr[3]);
			case SP -> asShort(gpr[4]);
			case BP -> asShort(gpr[5]);
			case SI -> asShort(gpr[6]);
			case DI -> asShort(gpr[7]);
			case R8W -> asShort(gpr[8]);
			case R9W -> asShort(gpr[9]);
			case R10W -> asShort(gpr[10]);
			case R11W -> asShort(gpr[11]);
			case R12W -> asShort(gpr[12]);
			case R13W -> asShort(gpr[13]);
			case R14W -> asShort(gpr[14]);
			case R15W -> asShort(gpr[15]);
			case CS -> seg[0];
			case DS -> seg[1];
			case ES -> seg[2];
			case FS -> seg[3];
			case GS -> seg[4];
			case SS -> seg[5];
		};
	}

	@Override
	public void set(final Register16 r, final short v) {
		switch (r) {
			case AX -> gpr[0] = (gpr[0] & 0xffffffffff0000L) | asLong(v);
			case BX -> gpr[1] = (gpr[1] & 0xffffffffff0000L) | asLong(v);
			case CX -> gpr[2] = (gpr[2] & 0xffffffffff0000L) | asLong(v);
			case DX -> gpr[3] = (gpr[3] & 0xffffffffff0000L) | asLong(v);
			case SP -> gpr[4] = (gpr[4] & 0xffffffffff0000L) | asLong(v);
			case BP -> gpr[5] = (gpr[5] & 0xffffffffff0000L) | asLong(v);
			case SI -> gpr[6] = (gpr[6] & 0xffffffffff0000L) | asLong(v);
			case DI -> gpr[7] = (gpr[7] & 0xffffffffff0000L) | asLong(v);
			case R8W -> gpr[8] = (gpr[8] & 0xffffffffff0000L) | asLong(v);
			case R9W -> gpr[9] = (gpr[9] & 0xffffffffff0000L) | asLong(v);
			case R10W -> gpr[10] = (gpr[10] & 0xffffffffff0000L) | asLong(v);
			case R11W -> gpr[11] = (gpr[11] & 0xffffffffff0000L) | asLong(v);
			case R12W -> gpr[12] = (gpr[12] & 0xffffffffff0000L) | asLong(v);
			case R13W -> gpr[13] = (gpr[13] & 0xffffffffff0000L) | asLong(v);
			case R14W -> gpr[14] = (gpr[14] & 0xffffffffff0000L) | asLong(v);
			case R15W -> gpr[15] = (gpr[15] & 0xffffffffff0000L) | asLong(v);
			case CS -> seg[0] = v;
			case DS -> seg[1] = v;
			case ES -> seg[2] = v;
			case FS -> seg[3] = v;
			case GS -> seg[4] = v;
			case SS -> seg[5] = v;
		}
	}

	@Override
	public int get(final Register32 r) {
		return switch (r) {
			case EAX -> asInt(gpr[0]);
			case EBX -> asInt(gpr[1]);
			case ECX -> asInt(gpr[2]);
			case EDX -> asInt(gpr[3]);
			case ESP -> asInt(gpr[4]);
			case EBP -> asInt(gpr[5]);
			case ESI -> asInt(gpr[6]);
			case EDI -> asInt(gpr[7]);
			case R8D -> asInt(gpr[8]);
			case R9D -> asInt(gpr[9]);
			case R10D -> asInt(gpr[10]);
			case R11D -> asInt(gpr[11]);
			case R12D -> asInt(gpr[12]);
			case R13D -> asInt(gpr[13]);
			case R14D -> asInt(gpr[14]);
			case R15D -> asInt(gpr[15]);
			case EIP -> asInt(rip);
		};
	}

	@Override
	public void set(final Register32 r, final int v) {
		switch (r) {
			case EAX -> gpr[0] = (gpr[0] & 0xffffffff00000000L) | asLong(v);
			case EBX -> gpr[1] = (gpr[1] & 0xffffffff00000000L) | asLong(v);
			case ECX -> gpr[2] = (gpr[2] & 0xffffffff00000000L) | asLong(v);
			case EDX -> gpr[3] = (gpr[3] & 0xffffffff00000000L) | asLong(v);
			case ESP -> gpr[4] = (gpr[4] & 0xffffffff00000000L) | asLong(v);
			case EBP -> gpr[5] = (gpr[5] & 0xffffffff00000000L) | asLong(v);
			case ESI -> gpr[6] = (gpr[6] & 0xffffffff00000000L) | asLong(v);
			case EDI -> gpr[7] = (gpr[7] & 0xffffffff00000000L) | asLong(v);
			case R8D -> gpr[8] = (gpr[8] & 0xffffffff00000000L) | asLong(v);
			case R9D -> gpr[9] = (gpr[9] & 0xffffffff00000000L) | asLong(v);
			case R10D -> gpr[10] = (gpr[10] & 0xffffffff00000000L) | asLong(v);
			case R11D -> gpr[11] = (gpr[11] & 0xffffffff00000000L) | asLong(v);
			case R12D -> gpr[12] = (gpr[12] & 0xffffffff00000000L) | asLong(v);
			case R13D -> gpr[13] = (gpr[13] & 0xffffffff00000000L) | asLong(v);
			case R14D -> gpr[14] = (gpr[14] & 0xffffffff00000000L) | asLong(v);
			case R15D -> gpr[15] = (gpr[15] & 0xffffffff00000000L) | asLong(v);
			case EIP -> rip = (rip & 0xffffffff00000000L) | asLong(v);
		}
	}

	@Override
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

	@Override
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
	public boolean isSet(final RFlags f) {
		return (rflags & (1L << f.bit())) != 0L;
	}

	@Override
	public void set(final RFlags f, final boolean v) {
		if (v) {
			set(f);
		} else {
			reset(f);
		}
	}

	@Override
	public void resetFlags() {
		rflags = 0L;
	}

	private void set(final RFlags f) {
		rflags |= (1L << f.bit());
	}

	private void reset(final RFlags f) {
		rflags &= ~(1L << f.bit());
	}

	@Override
	public String toString() {
		return "X86RegisterFile("
				+ Arrays.stream(Register64.values())
						.map(r -> String.format("%s=0x%016x", r.name(), get(r)))
						.collect(Collectors.joining(","))
				+ ","
				+ Stream.of(Register16.CS, Register16.DS, Register16.ES, Register16.FS, Register16.GS, Register16.SS)
						.map(r -> String.format("%s=0x%016x", r.name(), get(r)))
						.collect(Collectors.joining(","))
				+ ",RFLAGS="
				+ String.format("0x%016x", rflags) + ")";
	}

	@Override
	public int hashCode() {
		int h = 17;
		for (final long r : gpr) {
			h = 31 * h + HashUtils.hash(r);
		}
		for (final short s : seg) {
			h = 31 * h + HashUtils.hash(s);
		}
		h = 31 * h + HashUtils.hash(rip);
		h = 31 * h + HashUtils.hash(rflags);
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
		return Arrays.equals(this.gpr, regs.gpr)
				&& Arrays.equals(this.seg, regs.seg)
				&& this.rip == regs.rip
				&& this.rflags == regs.rflags;
	}
}
