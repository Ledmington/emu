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
package com.ledmington.emu;

import java.io.Serial;

/** This class represents an error in which the stack was not handled correctly and a push was tried on a full stack. */
public final class StackOverflow extends RuntimeException {

	@Serial
	private static final long serialVersionUID = -5884319071749576077L;

	/** Creates a new StackOverflow exception with a default message. */
	public StackOverflow() {
		super("Tried to push over the top of the stack.");
	}
}
