/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2024 Filippo Barbari <filippo.barbari@gmail.com>
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
package com.ledmington.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to disable specific SpotBugs warnings without importing a new dependency. Source: <a
 * href="https://sourceforge.net/p/findbugs/feature-requests/298/#5e88">here</a>.
 */
@Retention(RetentionPolicy.CLASS)
public @interface SuppressFBWarnings {
	/**
	 * The set of SpotBugs warnings that are to be suppressed in annotated element. The value can be a bug category,
	 * kind or pattern.
	 *
	 * @return The set of SpotBugs warnings to be suppressed.
	 */
	String[] value();

	/**
	 * Optional documentation of the reason why the warning is suppressed.
	 *
	 * @return The reason why the warning is suppressed.
	 */
	String justification();
}
