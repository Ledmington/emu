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

/** An entry for the Dynamic section (.dyn). */
public final class DynamicTableEntry {

    private final DynamicTableEntryTag tag;
    private final long content;

    public DynamicTableEntry(final long tag, final long content) {
        this.tag = DynamicTableEntryTag.fromCode(tag);
        this.content = content;
    }

    public DynamicTableEntryTag getTag() {
        return tag;
    }

    public long getContent() {
        return content;
    }

    @Override
    public String toString() {
        return String.format("0x%016x %-12s 0x%016x", tag.getCode(), tag.getName(), content);
    }
}
