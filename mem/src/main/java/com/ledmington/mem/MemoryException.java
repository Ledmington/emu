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
package com.ledmington.mem;

import java.io.Serial;

/**
 * A special exception which gets thrown when accessing the memory in the wrong way. For example, when trying to read
 * from a non-readable address.
 */
public final class MemoryException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = -8908344966805555310L;

	/**
	 * Creates a new MemoryException with the given message.
	 *
	 * @param message The message of the exception.
	 */
	public MemoryException(final String message) {
		super(message);
	}
}
