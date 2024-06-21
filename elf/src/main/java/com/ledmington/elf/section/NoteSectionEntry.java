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

import java.util.stream.Collectors;

import com.ledmington.utils.BitUtils;

/**
 * An entry of an ELF section of type SHT_NOTE (.note*).
 *
 * @param name The name of the entry.
 * @param description The description/content of the entry.
 * @param type The 4-byte type of this entry (meaning of this field varies between note section).
 * @param is32Bit Used for alignment.
 */
public record NoteSectionEntry(String name, String description, NoteSectionEntryType type, boolean is32Bit) {

    /**
     * Returns the number of bytes occupied by the actual data.
     *
     * @return The number of bytes occupied by the actual data.
     */
    public int getSize() {
        return 4 + 4 + 4 + name.length() + description.length();
    }

    /**
     * Returns the number of bytes occupied by this entry, aligned to a 4-byte or 8-byte boundary (depending on the
     * is32Bit value).
     *
     * @return The number of bytes occupied by this structure, accounting for alignment.
     */
    public int getAlignedSize() {
        final int bytes = is32Bit ? 4 : 8;
        final int size = getSize();
        return size % bytes == 0 ? size : (((size / bytes) + 1) * bytes);
    }

    @Override
    public String toString() {
        return "NoteSectionEntry[name='" + name + "';description="
                + description
                        .chars()
                        .mapToObj(x -> String.format("%02x", BitUtils.asByte(x)))
                        .collect(Collectors.joining())
                + "]";
    }
}
