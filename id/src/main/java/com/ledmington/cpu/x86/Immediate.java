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
package com.ledmington.cpu.x86;

import java.util.Objects;

import com.ledmington.utils.BitUtils;

/** This class represents an immediate value in a x86 instruction. */
public final class Immediate implements Operand {

	private enum Type {
		BYTE,
		SHORT,
		INT,
		LONG
	}

	private final long value;
	private final Type type;

	private Immediate(final long value, final Type type) {
		this.value = value;
		this.type = Objects.requireNonNull(type);
	}

	/**
	 * Creates an immediate value of 1 byte.
	 *
	 * @param b The 1-byte immediate.
	 */
	public Immediate(final byte b) {
		this(b, Type.BYTE);
	}

	/**
	 * Creates an immediate value of 2 bytes.
	 *
	 * @param s The 2-bytes immediate.
	 */
	public Immediate(final short s) {
		this(s, Type.SHORT);
	}

	/**
	 * Creates an immediate value of 4 bytes.
	 *
	 * @param x The 4-bytes immediate.
	 */
	public Immediate(final int x) {
		this(x, Type.INT);
	}

	/**
	 * Creates an immediate value of 8 bytes.
	 *
	 * @param x The 8-bytes immediate.
	 */
	public Immediate(final long x) {
		this(x, Type.LONG);
	}

	@Override
	public int bits() {
		return switch (type) {
			case BYTE -> 8;
			case SHORT -> 16;
			case INT -> 32;
			case LONG -> 64;
		};
	}

	/**
	 * Returns the value of this Immediate as a 8-bit value.
	 *
	 * @return The 8-bit value of this Immediate.
	 */
	public byte asByte() {
		if (type != Type.BYTE) {
			throw new IllegalArgumentException(String.format("This immediate is not 8 bits but %,d.", bits()));
		}
		return BitUtils.asByte(value);
	}

	/**
	 * Returns the value of this Immediate as a 16-bit value.
	 *
	 * @return The 16-bit value of this Immediate.
	 */
	public short asShort() {
		if (type != Type.SHORT) {
			throw new IllegalArgumentException(String.format("This immediate is not 16 bits but %,d.", bits()));
		}
		return BitUtils.asShort(value);
	}

	/**
	 * Returns the value of this Immediate as a 32-bit value.
	 *
	 * @return The 32-bit value of this Immediate.
	 */
	public int asInt() {
		if (type != Type.INT) {
			throw new IllegalArgumentException(String.format("This immediate is not 32 bits but %,d.", bits()));
		}
		return BitUtils.asInt(value);
	}

	/**
	 * Returns the value of this Immediate as a 64-bit value.
	 *
	 * @return The 64-bit value of this Immediate.
	 */
	public long asLong() {
		if (type != Type.LONG) {
			throw new IllegalArgumentException(String.format("This immediate is not 64 bits but %,d.", bits()));
		}
		return value;
	}

	@Override
	public String toIntelSyntax() {
		return toIntelSyntax(false);
	}

	public String toIntelSyntax(final boolean shortHex) {
		return switch (type) {
			case BYTE -> String.format(shortHex ? "0x%x" : "0x%02x", BitUtils.asByte(value));
			case SHORT -> String.format(shortHex ? "0x%x" : "0x%04x", BitUtils.asShort(value));
			case INT -> String.format(shortHex ? "0x%x" : "0x%08x", BitUtils.asInt(value));
			case LONG -> String.format(shortHex ? "0x%x" : "0x%016x", value);
		};
	}

	@Override
	public String toString() {
		return "Immediate(value=" + value + ";type=" + type + ")";
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + (BitUtils.asInt(value) ^ BitUtils.asInt(value >>> 32));
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
		if (!(other instanceof final Immediate imm)) {
			return false;
		}
		return this.value == imm.value && this.type == imm.type;
	}
}
