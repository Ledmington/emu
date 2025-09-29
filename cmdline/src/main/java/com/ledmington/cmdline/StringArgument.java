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
package com.ledmington.cmdline;

import java.util.Objects;

public record StringArgument(String shortName, String longName, String description, String defaultValue)
		implements CommandLineArgument {
	public StringArgument {
		if (shortName == null && longName == null) {
			throw new IllegalArgumentException("At least one of shortName and longName must not be null.");
		}
		Objects.requireNonNull(description, "Null description.");
	}

	@Override
	public boolean hasShortName() {
		return shortName != null;
	}

	@Override
	public String shortName() {
		if (!hasShortName()) {
			throw new IllegalStateException("This BooleanArgument has no shortName.");
		}
		return shortName;
	}

	@Override
	public boolean hasLongName() {
		return longName != null;
	}

	@Override
	public String longName() {
		if (!hasLongName()) {
			throw new IllegalStateException("This BooleanArgument has no longName.");
		}
		return longName;
	}
}
