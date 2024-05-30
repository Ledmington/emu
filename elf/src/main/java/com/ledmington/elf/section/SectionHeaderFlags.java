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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public enum SectionHeaderFlags {
    SHF_WRITE(0x00000000000001L, "WRITE", "Writable", 'W'),
    SHF_ALLOC(0x00000000000002L, "ALLOC", "Occupies memory during execution", 'A'),
    SHT_EXECINSTR(0x00000000000004L, "EXEC", "Executable", 'X'),
    SHF_MERGE(0x00000000000010L, "MERGE", "Might be merged", 'M'),
    SHF_STRINGS(0x00000000000020L, "STRINGS", "Contains null-terminated strings", 'S'),
    SHF_INFO_LINK(0x00000000000040L, "INFO LINK", "'sh_info' contains SHT index", 'I'),
    SHF_LINK_ORDER(0x00000000000080L, "LINK ORDER", "Preserve order after combining", 'L'),
    SHF_OS_NONCONFORMING(0x00000000000100L, "OS NONCONFORMING", "Non-standard OS specific handling required", 'O'),
    SHF_GROUP(0x00000000000200L, "GROUP", "Section is member of a group", 'G'),
    SHF_TLS(0x00000000000400L, "TLS", "Section hold thread-local data", 'T'),
    SHF_MASKOS(0x000000000ff00000L, "OS", "OS specific", 'o'),
    SHF_MASKPROC(0x00000000f0000000L, "PROC", "Processor specific", 'p'),
    SHF_ORDERED(0x0000000004000000L, "ORDERED", "Special ordering requirement (Solaris)", 'x'),
    SHF_EXCLUDE(0x0000000008000000L, "EXCLUDE", "Section is excluded unless referenced or allocated (Solaris)", 'E');

    private static final long SHF_MASK =
            Arrays.stream(SectionHeaderFlags.values()).map(shf -> shf.getCode()).reduce(0L, (a, b) -> a | b);

    public static boolean isValid(final long flags) {
        return (flags & (~SHF_MASK)) == 0L;
    }

    public static SectionHeaderFlags[] fromLong(final long flags) {
        if (!isValid(flags)) {
            throw new IllegalArgumentException(String.format("Invalid SHF flags 0x%016x", flags));
        }
        final List<SectionHeaderFlags> shf = new ArrayList<>();
        for (final SectionHeaderFlags f : SectionHeaderFlags.values()) {
            if ((flags & f.getCode()) != 0L) { // NOPMD
                shf.add(f);
            }
        }
        return shf.toArray(new SectionHeaderFlags[0]);
    }

    private final long code;
    private final String name;
    private final String description;
    private final char id;

    SectionHeaderFlags(final long code, final String name, final String description, final char id) {
        this.code = code;
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
        this.id = id;
    }

    public long getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public char getId() {
        return id;
    }
}
