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

import com.ledmington.utils.MiniLogger;

public final class SectionHeaderType {

    private static final MiniLogger logger = MiniLogger.getLogger("sht-entry-type");
    private static final Map<Integer, SectionHeaderType> codeToType = new HashMap<>();

    public static final SectionHeaderType SHT_NULL = new SectionHeaderType(0x00000000, "NULL", "Entry unused");
    public static final SectionHeaderType SHT_PROGBITS = new SectionHeaderType(0x00000001, "PROGBITS", "Program data");
    public static final SectionHeaderType SHT_SYMTAB = new SectionHeaderType(0x00000002, "SYMTAB", "Symbol table");
    public static final SectionHeaderType SHT_STRTAB = new SectionHeaderType(0x00000003, "STRTAB", "String table");
    public static final SectionHeaderType SHT_RELA =
            new SectionHeaderType(0x00000004, "RELA", "Relocation entries with addends");
    public static final SectionHeaderType SHT_HASH = new SectionHeaderType(0x00000005, "HASH", "Symbol Hash table");
    public static final SectionHeaderType SHT_DYNAMIC =
            new SectionHeaderType(0x00000006, "DYNAMIC", "Dynamic linking info");
    public static final SectionHeaderType SHT_NOTE = new SectionHeaderType(0x00000007, "NOTE", "Notes");
    public static final SectionHeaderType SHT_NOBITS =
            new SectionHeaderType(0x00000008, "NOBITS", "Program space with no data (bss)");
    public static final SectionHeaderType SHT_REL =
            new SectionHeaderType(0x00000009, "REL", "Relocation entries (no addends)");
    public static final SectionHeaderType SHT_SHLIB = new SectionHeaderType(0x0000000a, "SHLIB", "Reserved");
    public static final SectionHeaderType SHT_DYNSYM =
            new SectionHeaderType(0x0000000b, "DYNSYM", "Dynamic linker symbol table");
    public static final SectionHeaderType SHT_INIT_ARRAY =
            new SectionHeaderType(0x0000000e, "INIT_ARRAY", "Array of constructors");
    public static final SectionHeaderType SHT_FINI_ARRAY =
            new SectionHeaderType(0x0000000f, "FINI_ARRAY", "Array of destructors");
    public static final SectionHeaderType SHT_PREINIT_ARRAY =
            new SectionHeaderType(0x00000010, "PREINIT_ARRAY", "Array of pre-constructors");
    public static final SectionHeaderType SHT_GROUP = new SectionHeaderType(0x00000011, "GROUP", "Section group");
    public static final SectionHeaderType SHT_SYMTAB_SHNDX =
            new SectionHeaderType(0x00000012, "SYMTAB_SHNDX", "Extended section indices");
    public static final SectionHeaderType SHT_NUM = new SectionHeaderType(0x00000013, "NUM", "Number of defined types");

    public static final SectionHeaderType SHT_LOOS =
            new SectionHeaderType(0x60000000, "SHT_LOOS", "Unknown (OS specific)", false);
    public static final SectionHeaderType SHT_GNU_HASH =
            new SectionHeaderType(0x6ffffff6, "GNU_HASH", "GNU Hash table");
    public static final SectionHeaderType SHT_GNU_verdef =
            new SectionHeaderType(0x6ffffffd, "GNU_verdef", "GNU version symbol definitions");
    public static final SectionHeaderType SHT_GNU_verneed =
            new SectionHeaderType(0x6ffffffe, "VERNEED", "GNU version symbol needed elements");
    public static final SectionHeaderType SHT_GNU_versym =
            new SectionHeaderType(0x6fffffff, "VERSYM", "GNU version symbol table");
    public static final SectionHeaderType SHT_HIOS =
            new SectionHeaderType(0x6fffffff, "SHT_HIOS", "Unknown (OS specific)", false);

    public static final SectionHeaderType SHT_LOPROC =
            new SectionHeaderType(0x70000000, "SHT_LOPROC", "Unknown (Processor specific)", false);
    public static final SectionHeaderType SHT_HIPROC =
            new SectionHeaderType(0x7fffffff, "SHT_HIPROC", "Unknown (Processor specific)", false);

    public static final SectionHeaderType SHT_LOUSER =
            new SectionHeaderType(0x80000000, "SHT_LOUSER", "Unknown (Application specific)", false);
    public static final SectionHeaderType SHT_HIUSER =
            new SectionHeaderType(0xffffffff, "SHT_HIUSER", "Unknown (Application specific)", false);

    public static boolean isValid(final int code) {
        return codeToType.containsKey(code) || code >= SHT_LOOS.getCode();
    }

    public static SectionHeaderType fromCode(final int code) {
        if (!codeToType.containsKey(code)) {
            if (code >= SHT_LOOS.getCode() && code <= SHT_HIOS.getCode()) {
                logger.warning("Unknown SHT entry type found: 0x%08x", code);
                return new SectionHeaderType(code, "Unknown", String.format("0x%08x (OS specific)", code), false);
            }
            if (code >= SHT_LOPROC.getCode() && code <= SHT_HIPROC.getCode()) {
                logger.warning("Unknown SHT entry type found: 0x%08x", code);
                return new SectionHeaderType(
                        code, "Unknown", String.format("0x%08x (Processor specific)", code), false);
            }
            if (code >= SHT_LOUSER.getCode() && code <= SHT_HIUSER.getCode()) {
                logger.warning("Unknown SHT entry type found: 0x%08x", code);
                return new SectionHeaderType(
                        code, "Unknown", String.format("0x%08x (Application specific)", code), false);
            }
            throw new IllegalArgumentException(String.format("Unknown SHT entry identifier: 0x%08x", code));
        }
        return codeToType.get(code);
    }

    private final int code;
    private final String name;
    private final String description;

    private SectionHeaderType(final int code, final String name, final String description, final boolean addToMap) {
        this.code = code;
        this.name = name;
        this.description = description;

        if (addToMap) {
            if (codeToType.containsKey(code)) {
                throw new IllegalStateException(String.format(
                        "SHT entry enum value with code %d (0x%02x) and description '%s' already exists",
                        code, code, description));
            }
            codeToType.put(code, this);
        }
    }

    private SectionHeaderType(final int code, final String name, final String description) {
        this(code, name, description, true);
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
