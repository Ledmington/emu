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
package com.ledmington.elf;

import java.io.Serial;

/** This class represents a generic error occurred during parsing of an ELF file. */
public final class ELFParsingException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1267945930400050378L;

	/**
	 * Craetes a new ElfParsingException with the given message.
	 *
	 * @param message The message of this exception.
	 */
	public ELFParsingException(final String message) {
		super(message);
	}

	/**
	 * Creates a new ElfParsingException with the given Throwable as cause.
	 *
	 * @param cause The cause of this exception being thrown.
	 */
	public ELFParsingException(final Throwable cause) {
		super(cause);
	}
}
