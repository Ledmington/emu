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

/** The extended VEX prefix (EVEX): 0x62 + 3 bytes. */
public final class EvexPrefix {

	public static boolean isEvexPrefix(final byte evexByte) {
		return evexByte == (byte) 0x62;
	}

	public static EvexPrefix of(final byte b1, byte b2, byte b3) {
		return new EvexPrefix();
	}

	private EvexPrefix() {}
}
