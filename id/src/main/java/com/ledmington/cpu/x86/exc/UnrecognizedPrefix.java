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
 * This exception is thrown when a prefix is detected during opcode decoding, meaning that it was not properly
 * recognized by earlier decoding steps.
 */
public final class UnrecognizedPrefix extends RuntimeException {

	@Serial
	private static final long serialVersionUID = -5061777630768344350L;

	/**
	 * Creates a new UnrecognizedPrefix runtime exception with a proper message.
	 *
	 * @param type The type of the prefix.
	 * @param position The position of the unrecognized prefix.
	 */
	public UnrecognizedPrefix(final String type, final long position) {
		super(String.format("Found an unrecognized %s prefix at byte 0x%016x", type, position));
	}
}
