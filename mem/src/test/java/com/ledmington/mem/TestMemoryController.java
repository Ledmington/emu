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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class TestMemoryController {
	@Test
	void multiByteRead() {
		final MemoryController mem =
				new MemoryController(new RandomAccessMemory(MemoryInitializer.random()), true, true);
		final long start = 0x5a5a5a5a00000000L;
		mem.setPermissions(start, start + 7L, true, true, false);
		mem.write(start, (byte) 0x00);
		mem.write(start + 1L, (byte) 0x01);
		mem.write(start + 2L, (byte) 0x02);
		mem.write(start + 3L, (byte) 0x03);
		mem.write(start + 4L, (byte) 0x04);
		mem.write(start + 5L, (byte) 0x05);
		mem.write(start + 6L, (byte) 0x06);
		mem.write(start + 7L, (byte) 0x07);
		assertEquals(0x0706050403020100L, mem.read8(start));
	}
}
