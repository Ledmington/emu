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

/** This class represents an invalid legacy opcode which was encountered during instruction decoding. */
public final class InvalidLegacyOpcode extends DecodingException {

	@Serial
	private static final long serialVersionUID = 721365862585867652L;

	/**
	 * Creates a new InvalidLegacyOpcode with the given byte and the corresponding legacy instruction.
	 *
	 * @param legacyOpcode The byte representing the legacy opcode.
	 * @param legacyInstruction The legacy instruction which was encountered.
	 */
	public InvalidLegacyOpcode(final byte legacyOpcode, final String legacyInstruction) {
		super(String.format(
				"Byte 0x%02x (legacy '%s' instruction) is invalid in x86_64.", legacyOpcode, legacyInstruction));
	}
}
