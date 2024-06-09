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
package com.ledmington.elf;

import java.util.HashMap;
import java.util.Map;

/** The miscellaneous flags of a Program Header Table entry. */
public enum PHTEntryFlags {

    /** This segment is executable. */
    PF_X(0x00000001),

    /** This segment is writeable. */
    PF_W(0x00000002),

    /** This segment is readable. */
    PF_R(0x00000004);

    private static final Map<Integer, PHTEntryFlags> codeToFlags = new HashMap<>();

    static {
        for (final PHTEntryFlags x : PHTEntryFlags.values()) {
            if (codeToFlags.containsKey(x.code)) {
                throw new IllegalStateException(
                        String.format("PHT flags enum value with code %d (0x%02x) already exists", x.code, x.code));
            }
            codeToFlags.put(x.code, x);
        }
    }

    private final int code;

    PHTEntryFlags(final int code) {
        this.code = code;
    }

    /**
     * Returns the 64 bit code of this flag.
     *
     * @return The code of this flag.
     */
    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "PHTEntryFlags(code=" + code + ')';
    }
}
