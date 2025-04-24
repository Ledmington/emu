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
package com.ledmington.cpu.x86.exc;

import java.io.Serial;

/** This exception is thrown when an unknown opcode is detected during instruction decoding. */
public final class UnknownOpcode extends DecodingException {

	@Serial
	private static final long serialVersionUID = 2581758152120570603L;

	/**
	 * Creates a UnknownOpcode runtime exception with a proper message for a single byte opcode.
	 *
	 * @param opcodeByte The unknown opcode.
	 */
	public UnknownOpcode(final byte opcodeByte) {
		super(String.format("Unknown opcode 0x%02x.", opcodeByte));
	}

	/**
	 * Creates a UnknownOpcode runtime exception with a proper message for a 2-bytes opcode.
	 *
	 * @param firstByte The unknown opcode's first byte.
	 * @param secondByte The unknown opcode's second byte.
	 */
	public UnknownOpcode(final byte firstByte, final byte secondByte) {
		super(String.format("Unknown opcode 0x%02x%02x.", firstByte, secondByte));
	}
}
