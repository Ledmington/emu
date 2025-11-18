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
package com.ledmington.cpu.x86;

import java.util.Objects;

/** A collection of common utilities for Instructions. */
public final class Instructions {

	private Instructions() {}

	/**
	 * Returns true if the given instructions are <i>effectively equal</i>, meaning that the type of the class
	 * implementing {@link Instruction} may not be the same. This method is, therefore, slightly different from
	 * {@link Instruction#equals(Object)}.
	 *
	 * @param a An Instruction.
	 * @param b An Instruction to be compared with a for equality.
	 * @return true if the arguments are equal to each other and false otherwise
	 */
	public static boolean equals(final Instruction a, final Instruction b) {
		Objects.requireNonNull(a);
		Objects.requireNonNull(b);

		if (a.hasPrefix() != b.hasPrefix()) {
			return false;
		}
		if (a.hasPrefix() && !a.getPrefix().equals(b.getPrefix())) {
			return false;
		}

		if (a.hasZeroDestinationMask() != b.hasZeroDestinationMask()) {
			return false;
		}
		if (a.hasDestinationMask() != b.hasDestinationMask()) {
			return false;
		}
		if (a.hasDestinationMask() && !a.getDestinationMask().equals(b.getDestinationMask())) {
			return false;
		}

		for (int i = 0; i < 4; i++) {
			if (a.hasOperand(i) != b.hasOperand(i)) {
				return false;
			}
			if (a.hasOperand(i) && !a.operand(i).equals(b.operand(i))) {
				return false;
			}
		}

		return a.opcode() == b.opcode();
	}
}
