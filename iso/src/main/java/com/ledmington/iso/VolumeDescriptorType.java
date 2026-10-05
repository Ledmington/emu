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

import com.ledmington.utils.BitUtils;

public enum VolumeDescriptorType {
	BOOT_RECORD,
	PRIMARY,
	SUPPLEMENTARY,
	PARTITION,
	SET_TERMINATOR;

	public static VolumeDescriptorType fromByte(final byte x) {
		return switch (BitUtils.asInt(x)) {
			case 0 -> BOOT_RECORD;
			case 1 -> PRIMARY;
			case 2 -> SUPPLEMENTARY;
			case 3 -> PARTITION;
			case 255 -> SET_TERMINATOR;
			default ->
				throw new IllegalArgumentException(String.format("Unknown volume descriptor byte %d (0x%02x).", x, x));
		};
	}
}
