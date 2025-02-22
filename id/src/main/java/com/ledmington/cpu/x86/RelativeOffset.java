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

import com.ledmington.utils.HashUtils;

/**
 * An offset relative to the current (at run-time) value of the Instruction Pointer register. Equivalent to an
 * IndirectOperand with only the instruction pointer as the base.
 */
public final class RelativeOffset implements Operand {

	private final long value;
	private final DisplacementType type;

	/**
	 * Creates a RelativeOffset by sign-extending the given byte.
	 *
	 * @param x The byte representing the offset.
	 * @return A sign-extended RelativeOffset.
	 */
	public static RelativeOffset of(final byte x) {
		return new RelativeOffset(x, DisplacementType.BYTE);
	}

	/**
	 * Creates a RelativeOffset by sign-extending the given int.
	 *
	 * @param x The int representing the offset.
	 * @return A sign-extended RelativeOffset.
	 */
	public static RelativeOffset of(final int x) {
		return new RelativeOffset(x, DisplacementType.INT);
	}

	private RelativeOffset(final long value, final DisplacementType type) {
		this.value = value;
		this.type = Objects.requireNonNull(type);
	}

	@Override
	public String toIntelSyntax() {
		return String.format("0x%x", value);
	}

	@Override
	public int bits() {
		return switch (type) {
			case BYTE -> 8;
			case INT -> 32;
		};
	}

	/**
	 * Returns the value of this RelativeOffset.
	 *
	 * @return The value.
	 */
	public long getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "RelativeOffset(value=" + value + ";type=" + type + ")";
	}

	@Override
	public int hashCode() {
		int h = 17;
		h = 31 * h + HashUtils.hash(value);
		h = 31 * h + type.hashCode();
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
		if (!(other instanceof RelativeOffset ro)) {
			return false;
		}
		return this.value == ro.value && this.type.equals(ro.type);
	}
}
