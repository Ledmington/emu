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
package com.ledmington.emu;

import java.util.Objects;
import java.util.function.Supplier;

import com.ledmington.emu.mem.MemoryInitializer;

public final class EmulatorConstants {

    /** The default memory initializer */
    private static Supplier<MemoryInitializer> memoryInitializer = () -> MemoryInitializer.random();

    private EmulatorConstants() {}

    public static void setMemoryInitializer(final Supplier<MemoryInitializer> memInit) {
        memoryInitializer = Objects.requireNonNull(memInit);
    }

    public static MemoryInitializer getMemoryInitializer() {
        return memoryInitializer.get();
    }
}
