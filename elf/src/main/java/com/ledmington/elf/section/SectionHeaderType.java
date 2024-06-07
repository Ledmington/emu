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

/** Type of an ELF Section. */
public final class SectionHeaderType {

    private static final MiniLogger logger = MiniLogger.getLogger("sht-entry-type");
    private static final Map<Integer, SectionHeaderType> codeToType = new HashMap<>();

    /**
     * This value marks the section header as inactive; it does not have an associated section. Other members of the
     * section header have undefined values.
     */
    public static final SectionHeaderType SHT_NULL = new SectionHeaderType(0x00000000, "NULL", "Entry unused");

    /**
     * This section holds information defined by the program, whose format and meaning are determined solely by the
     * program.
     */
    public static final SectionHeaderType SHT_PROGBITS = new SectionHeaderType(0x00000001, "PROGBITS", "Program data");

    /** This section holds a symbol table. */
    public static final SectionHeaderType SHT_SYMTAB = new SectionHeaderType(0x00000002, "SYMTAB", "Symbol table");

    /** This section holds a string table. */
    public static final SectionHeaderType SHT_STRTAB = new SectionHeaderType(0x00000003, "STRTAB", "String table");

    /**
     * This section holds relocation entries with explicit addends, such as type ElfXX_Rela. An object file may have
     * multiple relocation sections.
     */
    public static final SectionHeaderType SHT_RELA =
            new SectionHeaderType(0x00000004, "RELA", "Relocation entries with addends");

    /** This section holds a symbol hash table. */
    public static final SectionHeaderType SHT_HASH = new SectionHeaderType(0x00000005, "HASH", "Symbol Hash table");

    /** This section holds information for dynamic linking. */
    public static final SectionHeaderType SHT_DYNAMIC =
            new SectionHeaderType(0x00000006, "DYNAMIC", "Dynamic linking info");

    /** This section holds information that marks the file in some way. */
    public static final SectionHeaderType SHT_NOTE = new SectionHeaderType(0x00000007, "NOTE", "Notes");

    /**
     * A section of this type occupies no space in the file but otherwise resembles SHT_PROGBITS. Although this section
     * contains no bytes, the sh_offset member contains the conceptual file offset.
     */
    public static final SectionHeaderType SHT_NOBITS =
            new SectionHeaderType(0x00000008, "NOBITS", "Program space with no data (bss)");

    /**
     * This section holds relocation entries without explicit addends, such as type ElfXX_Rel. An object file may have
     * multiple relocation sections.
     */
    public static final SectionHeaderType SHT_REL =
            new SectionHeaderType(0x00000009, "REL", "Relocation entries (no addends)");

    /** This section type is reserved but has unspecified semantics. */
    public static final SectionHeaderType SHT_SHLIB = new SectionHeaderType(0x0000000a, "SHLIB", "Reserved");

    /** This section holds a symbol table. */
    public static final SectionHeaderType SHT_DYNSYM =
            new SectionHeaderType(0x0000000b, "DYNSYM", "Dynamic linker symbol table");

    /**
     * Identifies a section containing an array of pointers to initialization functions. Each pointer in the array is
     * taken as a parameterless procedure with a void return.
     */
    public static final SectionHeaderType SHT_INIT_ARRAY =
            new SectionHeaderType(0x0000000e, "INIT_ARRAY", "Array of constructors");

    /**
     * Identifies a section containing an array of pointers to termination functions. Each pointer in the array is taken
     * as a parameterless procedure with a void return.
     */
    public static final SectionHeaderType SHT_FINI_ARRAY =
            new SectionHeaderType(0x0000000f, "FINI_ARRAY", "Array of destructors");

    /**
     * Identifies a section containing an array of pointers to functions that are invoked before all other
     * initialization functions. Each pointer in the array is taken as a parameterless procedure with a void return.
     */
    public static final SectionHeaderType SHT_PREINIT_ARRAY =
            new SectionHeaderType(0x00000010, "PREINIT_ARRAY", "Array of pre-constructors");

    /**
     * Identifies a section group. A section group identifies a set of related sections that must be treated as a unit
     * by the link-editor. Sections of type SHT_GROUP may appear only in relocatable objects.
     */
    public static final SectionHeaderType SHT_GROUP = new SectionHeaderType(0x00000011, "GROUP", "Section group");

    /**
     * Identifies a section containing extended section indexes, that is associated with a symbol table. If any section
     * header indexes referenced by a symbol table, contain the escape value SHN_XINDEX, an associated SHT_SYMTAB_SHNDX
     * is required.
     *
     * <p>The SHT_SYMTAB_SHNDX section is an array of ElfXX_Word values. There is one value for every entry in the
     * associated symbol table entry. The values represent the section header indexes against which the symbol table
     * entries are defined. Only if corresponding symbol table entry's st_shndx field contains the escape value
     * SHN_XINDEX will the matching Elf32_Word hold the actual section header index. Otherwise, the entry must be
     * SHN_UNDEF (0).
     */
    public static final SectionHeaderType SHT_SYMTAB_SHNDX =
            new SectionHeaderType(0x00000012, "SYMTAB_SHNDX", "Extended section indices");

    /** Number of defined types. */
    public static final SectionHeaderType SHT_NUM = new SectionHeaderType(0x00000013, "NUM", "Number of defined types");

    /** Values in the inclusive range from this one to SHT_HIOS are reserved for OS-specific semantics. */
    public static final SectionHeaderType SHT_LOOS =
            new SectionHeaderType(0x60000000, "SHT_LOOS", "Unknown (OS specific)", false);

    /** GNU-style hash table (bloom filter?). */
    public static final SectionHeaderType SHT_GNU_HASH =
            new SectionHeaderType(0x6ffffff6, "GNU_HASH", "GNU Hash table");

    /** Version definition section. */
    public static final SectionHeaderType SHT_GNU_verdef =
            new SectionHeaderType(0x6ffffffd, "GNU_verdef", "GNU version symbol definitions");

    /** Version needs section. */
    public static final SectionHeaderType SHT_GNU_verneed =
            new SectionHeaderType(0x6ffffffe, "VERNEED", "GNU version symbol needed elements");

    /** Version symbol table. */
    public static final SectionHeaderType SHT_GNU_versym =
            new SectionHeaderType(0x6fffffff, "VERSYM", "GNU version symbol table");

    /** Values in the inclusive range from SHT_LOOS to this one are reserved for OS-specific semantics. */
    public static final SectionHeaderType SHT_HIOS =
            new SectionHeaderType(0x6fffffff, "SHT_HIOS", "Unknown (OS specific)", false);

    /** Values in the inclusive range from this one to SHT_HIPROC are reserved for processor-specific semantics. */
    public static final SectionHeaderType SHT_LOPROC =
            new SectionHeaderType(0x70000000, "SHT_LOPROC", "Unknown (Processor specific)", false);

    /** Values in the inclusive range from SHT_LOPROC to this one are reserved for processor-specific semantics. */
    public static final SectionHeaderType SHT_HIPROC =
            new SectionHeaderType(0x7fffffff, "SHT_HIPROC", "Unknown (Processor specific)", false);

    /** Values in the inclusive range from this one to SHT_HIUSER are reserved for application programs. */
    public static final SectionHeaderType SHT_LOUSER =
            new SectionHeaderType(0x80000000, "SHT_LOUSER", "Unknown (Application specific)", false);

    /** Values in the inclusive range from SHT_LOUSER to this one are reserved for application programs. */
    public static final SectionHeaderType SHT_HIUSER =
            new SectionHeaderType(0xffffffff, "SHT_HIUSER", "Unknown (Application specific)", false);

    public static boolean isValid(final int code) {
        return codeToType.containsKey(code) || code >= SHT_LOOS.code;
    }

    public static SectionHeaderType fromCode(final int code) {
        if (!codeToType.containsKey(code)) {
            if (code >= SHT_LOOS.code && code <= SHT_HIOS.code) {
                logger.warning("Unknown SHT entry type found: 0x%08x", code);
                return new SectionHeaderType(code, "Unknown", String.format("0x%08x (OS specific)", code), false);
            }
            if (code >= SHT_LOPROC.code && code <= SHT_HIPROC.code) {
                logger.warning("Unknown SHT entry type found: 0x%08x", code);
                return new SectionHeaderType(
                        code, "Unknown", String.format("0x%08x (Processor specific)", code), false);
            }
            if (code >= SHT_LOUSER.code && code <= SHT_HIUSER.code) {
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

    @Override
    public String toString() {
        return "SectionHeaderType(code=" + code + ";name=" + name + ";description=" + description + ")";
    }
}
