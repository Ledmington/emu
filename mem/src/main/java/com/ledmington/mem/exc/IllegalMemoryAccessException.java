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

/**
 * Base exception for illegal memory access operations. This exception is thrown when a memory access violates memory
 * safety rules, such as accessing a region without the necessary permissions or accessing uninitialized memory.
 */
public sealed class IllegalMemoryAccessException extends RuntimeException
		permits InvalidPermissionsException, AccessToUninitializedMemoryException {

	@Serial
	private static final long serialVersionUID = -8908344966805555310L;

	/**
	 * Constructs a new IllegalMemoryAccessException with the specified detail message.
	 *
	 * @param message The detail message explaining the reason for the exception.
	 */
	public IllegalMemoryAccessException(final String message) {
		super(message);
	}
}
