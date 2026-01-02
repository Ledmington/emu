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
package com.ledmington.cpu.x86.exc;

import java.io.Serial;

/** An exception thrown when an invalid instruction is encountered. */
public final class InvalidInstruction extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 9195004233257266304L;

	/**
	 * Creates a new InvalidInstruction with the given message.
	 *
	 * @param message The message of the exception.
	 */
	public InvalidInstruction(final String message) {
		super(message);
	}
}
