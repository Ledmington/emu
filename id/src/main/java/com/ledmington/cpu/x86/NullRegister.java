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

/**
 * This class does not represent an actual x86 register, instead it serves the purpose to fill the void in some
 * encodable (but invalid instructions) such as '0x8c 0x30', '0x8c 0x38', '0x8e 0x30' and '0x8e 0x38'.
 */
public final class NullRegister implements Register {

	private static final NullRegister instance = new NullRegister();

	/**
	 * Returns the only instance of NullRegister.
	 *
	 * @return The only instance of NullRegister.
	 */
	public static NullRegister getInstance() {
		return instance;
	}

	private NullRegister() {}

	@Override
	public String toIntelSyntax() {
		return "?";
	}

	@Override
	public int bits() {
		throw new UnsupportedOperationException();
	}
}
