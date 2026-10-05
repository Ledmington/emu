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
package com.ledmington.iso;

import java.util.Objects;

import com.ledmington.utils.BinaryReader;

public final class Sector {

	public static final int DEFAULT_SECTOR_SIZE = 2_048;

	private final byte content[] = new byte[DEFAULT_SECTOR_SIZE];

	public Sector(final byte[] content) {
		Objects.requireNonNull(content);
		if (content.length != this.content.length) {
			throw new IllegalArgumentException("Wrong sector length.");
		}
		System.arraycopy(content, 0, this.content, 0, this.content.length);
	}

	public static Sector read(final BinaryReader reader) {
		final byte[] content = new byte[DEFAULT_SECTOR_SIZE];
		for (int i = 0; i < content.length; i++) {
			content[i] = reader.read();
		}
		return new Sector(content);
	}
}
