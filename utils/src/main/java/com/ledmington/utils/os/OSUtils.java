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

import java.util.Locale;

/** A collection of common utilities about OS-specific information. */
// TODO: find a better name
public interface OSUtils {

	/** True if the OS this emulator is currently running on Windows, false otherwise. */
	boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase(Locale.US).contains("windows");

	/** The immutable singleton instance of OSUtils. */
	// TODO: add MacOS
	OSUtils INSTANCE = IS_WINDOWS ? new WindowsUtils() : new LinuxUtils();

	/**
	 * Returns the real user ID (UID) of the process owner.
	 *
	 * @return The real user ID (UID) of the process owner.
	 */
	int getUserID();

	/**
	 * Returns the effective user ID (EUID) of the process owner.
	 *
	 * @return The effective user ID (EUID) of the process owner.
	 */
	int getEffectiveUserID();

	/**
	 * Returns the real group ID (GID) of the process owner.
	 *
	 * @return The real group ID (GID) of the process owner.
	 */
	int getGroupID();

	/**
	 * Returns the effective group ID (EGID) of the process owner.
	 *
	 * @return The effective group ID (EGID) of the process owner.
	 */
	int getEffectiveGroupID();
}
