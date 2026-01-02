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

/** An exception representing a generic error which happened during instruction decoding. */
public class DecodingException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = -9133924907493926505L;

	/**
	 * Creates a new {@link DecodingException} with the given message.
	 *
	 * @param message The message of the exception.
	 */
	public DecodingException(final String message) {
		super(message);
	}
}
