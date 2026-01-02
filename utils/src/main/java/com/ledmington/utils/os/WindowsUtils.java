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
package com.ledmington.utils.os;

/** Windows-specific implementation of OSUtils. */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class WindowsUtils implements OSUtils {

	/* default */ WindowsUtils() {}

	@Override
	public int getUserID() {
		throw new UnsupportedOperationException("Not implemented.");
	}

	@Override
	public int getEffectiveUserID() {
		throw new UnsupportedOperationException("Not implemented.");
	}

	@Override
	public int getGroupID() {
		throw new UnsupportedOperationException("Not implemented.");
	}

	@Override
	public int getEffectiveGroupID() {
		throw new UnsupportedOperationException("Not implemented.");
	}
}
