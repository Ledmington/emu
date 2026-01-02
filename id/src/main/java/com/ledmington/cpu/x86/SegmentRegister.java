/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2026 Filippo Barbari <filippo.barbari@gmail.com>
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

import java.util.Locale;

/** Represents the six segment registers available in the x86 architecture. */
public enum SegmentRegister implements Register {

	/** The segment register CS (Code Segment). */
	CS,

	/** The segment register SS (Stack Segment). */
	SS,

	/** The segment register DS (Data Segment). */
	DS,

	/** The segment register ES (Extra Segment). */
	ES,

	/** The segment register FS. */
	FS,

	/** The segment register GS. */
	GS;

	private final String mnemonic = name().toLowerCase(Locale.US);

	/**
	 * Returns the numeric encoding of the given {@link SegmentRegister}.
	 *
	 * @param r The segment register to convert.
	 * @return The byte value representing the given segment register.
	 */
	public static byte toByte(final SegmentRegister r) {
		return switch (r) {
			case ES -> (byte) 0x00;
			case CS -> (byte) 0x01;
			case SS -> (byte) 0x02;
			case DS -> (byte) 0x03;
			case FS -> (byte) 0x04;
			case GS -> (byte) 0x05;
		};
	}

	@Override
	public String toIntelSyntax() {
		return mnemonic;
	}

	@Override
	public int bits() {
		throw new UnsupportedOperationException("This is a segment register.");
	}

	@Override
	public String toString() {
		return "SegmentRegister(mnemonic=" + mnemonic + ")";
	}
}
