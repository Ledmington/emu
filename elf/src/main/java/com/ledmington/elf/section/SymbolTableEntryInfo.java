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

import java.util.Objects;

public final class SymbolTableEntryInfo {

    private static final byte mask = (byte) 0x0f;

    public static SymbolTableEntryInfo fromByte(final byte info) {
        return new SymbolTableEntryInfo(
                SymbolTableEntryBind.fromCode((byte) ((info >>> 4) & mask)),
                SymbolTableEntryType.fromCode((byte) (info & mask)));
    }

    private final SymbolTableEntryBind bind;

    private final SymbolTableEntryType type;

    private SymbolTableEntryInfo(final SymbolTableEntryBind bind, final SymbolTableEntryType type) {
        this.bind = Objects.requireNonNull(bind);
        this.type = Objects.requireNonNull(type);
    }

    public byte toByte() {
        return (byte) ((bind.getCode() << 4) | (type.getCode()));
    }

    public SymbolTableEntryType getType() {
        return type;
    }

    @Override
    public String toString() {
        return bind + " " + type;
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + bind.hashCode();
        h = 31 * h + type.hashCode();
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
        final SymbolTableEntryInfo stei = (SymbolTableEntryInfo) other;
        return this.bind.equals(stei.bind) && this.type.equals(stei.type);
    }
}
