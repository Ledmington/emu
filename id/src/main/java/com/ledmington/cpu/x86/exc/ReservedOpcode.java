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

/**
 * This exception is thrown when a reserved opcode is recognized. It doesn't mean that the instruction is invalid or
 * that the opcode is unknown.
 */
public final class ReservedOpcode extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 5003238481425708141L;

	/**
	 * Creates a ReversedOpcode runtime exception with a proper message. Since, usually, all reserved opcodes are made
	 * of two bytes, this constructors also needs two bytes.
	 *
	 * @param firstByte The first byte of the opcode.
	 * @param secondByte The second byte of the opcode.
	 */
	public ReservedOpcode(final byte firstByte, final byte secondByte) {
		super(String.format("Reserved opcode 0x%02x%02x", firstByte, secondByte));
	}
}
