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
package com.ledmington.elf.section;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.ledmington.utils.HashUtils;

public final class SymbolTableEntryVisibility {

    private static final Map<Byte, SymbolTableEntryVisibility> codeToVisibility = new HashMap<>();

    public static final SymbolTableEntryVisibility STV_DEFAULT = new SymbolTableEntryVisibility((byte) 0, "DEFAULT");
    public static final SymbolTableEntryVisibility STV_INTERNAL = new SymbolTableEntryVisibility((byte) 1, "INTERNAL");
    public static final SymbolTableEntryVisibility STV_HIDDEN = new SymbolTableEntryVisibility((byte) 2, "HIDDEN");
    public static final SymbolTableEntryVisibility STV_PROTECTED =
            new SymbolTableEntryVisibility((byte) 3, "PROTECTED");

    public static SymbolTableEntryVisibility fromByte(final byte code) {
        if (!codeToVisibility.containsKey(code)) {
            throw new IllegalArgumentException(
                    String.format("Unknown Symbol table entry visibility identifier: 0x%02x", code));
        }
        return codeToVisibility.get(code);
    }

    private final byte code;
    private final String name;

    private SymbolTableEntryVisibility(final byte code, final String name) {
        this.code = code;
        this.name = Objects.requireNonNull(name);

        if (codeToVisibility.containsKey(code)) {
            throw new IllegalStateException(String.format(
                    "Symbol table entry visibility value with code %d (0x%02x) already exists", code, code));
        }
        codeToVisibility.put(code, this);
    }

    public byte getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + HashUtils.hash(code);
        h = 31 * h + name.hashCode();
        return h;
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (!this.getClass().equals(other.getClass())) {
            return false;
        }
        final SymbolTableEntryVisibility stev = (SymbolTableEntryVisibility) other;
        return this.code == stev.code && this.name.equals(stev.name);
    }
}
