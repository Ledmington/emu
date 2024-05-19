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

public enum SectionHeaderFlags {
    SHF_WRITE(0x00000000000001L, "Writable", 'W'),
    SHF_ALLOC(0x00000000000002L, "Occupies memory during execution", 'A'),
    SHT_EXECINSTR(0x00000000000004L, "Executable", 'X'),
    SHF_MERGE(0x00000000000010L, "Might be merged", 'M'),
    SHF_STRINGS(0x00000000000020L, "Contains null-terminated strings", 'S'),
    SHF_INFO_LINK(0x00000000000040L, "'sh_info' contains SHT index", 'I'),
    SHF_LINK_ORDER(0x00000000000080L, "Preserve order after combining", 'L'),
    SHF_OS_NONCONFORMING(0x00000000000100L, "Non-standard OS specific handling required", 'O'),
    SHF_GROUP(0x00000000000200L, "Section is member of a group", 'G'),
    SHF_TLS(0x00000000000400L, "Section hold thread-local data", 'T'),
    SHF_MASKOS(0x000000000ff00000L, "OS specific", 'o'),
    SHF_MASKPROC(0x00000000f0000000L, "Processor specific", 'p'),
    SHF_ORDERED(0x0000000004000000L, "Special ordering requirement (Solaris)", 'x'),
    SHF_EXCLUDE(0x0000000008000000L, "Section is excluded unless referenced or allocated (Solaris)", 'E');

    public static boolean isValid(long flags) {
        for (final SectionHeaderFlags f : SectionHeaderFlags.values()) {
            flags = flags & (~f.getCode());
        }
        return flags == 0L;
    }

    private final long code;
    private final String description;
    private final char id;

    SectionHeaderFlags(final long code, final String description, final char id) {
        this.code = code;
        this.description = description;
        this.id = id;
    }

    public long getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public char getId() {
        return id;
    }
}
