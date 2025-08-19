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
package com.ledmington.mem.exc;

import java.io.Serial;

/** Exception thrown when an attempt is made to read from a memory region that does not have read permissions. */
public final class IllegalReadException extends InvalidPermissionsException {

	@Serial
	private static final long serialVersionUID = 923857280730460914L;

	/**
	 * Constructs a new IllegalReadException with the specified detail message.
	 *
	 * @param message The detail message explaining the reason for the exception.
	 */
	public IllegalReadException(final String message) {
		super(message);
	}
}
