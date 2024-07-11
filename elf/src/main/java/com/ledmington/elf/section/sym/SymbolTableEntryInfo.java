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
package com.ledmington.elf.section.sym;

import java.util.Objects;

import com.ledmington.utils.BitUtils;

/** The info field of a symbol table entry. */
public final class SymbolTableEntryInfo {

    private static final byte mask = (byte) 0x0f;

    /**
     * Creates a new STE info object from the given 8-bit code.
     *
     * @param info The code to be converted.
     * @return A non-null STE object.
     */
    public static SymbolTableEntryInfo fromByte(final byte info) {
        return new SymbolTableEntryInfo(
                SymbolTableEntryBinding.fromCode(BitUtils.and(BitUtils.shr(info, 4), mask)),
                SymbolTableEntryType.fromCode(BitUtils.and(info, mask)));
    }

    private final SymbolTableEntryBinding bind;
    private final SymbolTableEntryType type;

    private SymbolTableEntryInfo(final SymbolTableEntryBinding bind, final SymbolTableEntryType type) {
        this.bind = Objects.requireNonNull(bind);
        this.type = Objects.requireNonNull(type);
    }

    /**
     * Converts this STE info object back to the original 8-bit value.
     *
     * @return The 8-bit value representing this STE info object.
     */
    public byte toByte() {
        return BitUtils.or(BitUtils.shl(bind.getCode(), 4), type.getCode());
    }

    /**
     * Returns the STE binding object of this info.
     *
     * @return The STE binding.
     */
    public SymbolTableEntryBinding getBinding() {
        return bind;
    }

    /**
     * Returns the STE type object of this info.
     *
     * @return The STE type.
     */
    public SymbolTableEntryType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "SymbolTableEntryInfo(binding=" + bind + ";type=" + type + ")";
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
