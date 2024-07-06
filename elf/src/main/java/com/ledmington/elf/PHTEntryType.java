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
import java.util.Objects;

import com.ledmington.utils.MiniLogger;

/** The type of an ELF Program Header Table entry. */
public final class PHTEntryType {

    private static final MiniLogger logger = MiniLogger.getLogger("pht-entry-type");
    private static final Map<Integer, PHTEntryType> codeToType = new HashMap<>();

    /**
     * The array element is unused; other members' values are undefined. This type lets the program header table have
     * ignored entries.
     */
    public static final PHTEntryType PT_NULL = new PHTEntryType(0x00000000, "NULL", "Unused");

    /**
     * The array element specifies a loadable segment, described by p_filesz and p_memsz. The bytes from the file are
     * mapped to the beginning of the memory segment. If the segment's memory size (p_memsz) is larger than the file
     * size (p_filesz), the "extra" bytes are defined to hold the value 0 and to follow the segment's initialized area.
     * The file size may not be larger than the memory size. Loadable segment entries in the program header table appear
     * in ascending order, sorted on the p_vaddr member.The array element specifies a loadable segment, described by
     * p_filesz and p_memsz. The bytes from the file are mapped to the beginning of the memory segment. If the segment's
     * memory size (p_memsz) is larger than the file size (p_filesz), the "extra" bytes are defined to hold the value 0
     * and to follow the segment's initialized area. The file size may not be larger than the memory size. Loadable
     * segment entries in the program header table appear in ascending order, sorted on the p_vaddr member.
     */
    public static final PHTEntryType PT_LOAD = new PHTEntryType(0x00000001, "LOAD", "Loadable");

    /** The array element specifies dynamic linking information. */
    public static final PHTEntryType PT_DYNAMIC = new PHTEntryType(0x00000002, "DYNAMIC", "Dynamic linking info");

    /** The array element specifies the location and size of a null-terminated path name to invoke as an interpreter. */
    public static final PHTEntryType PT_INTERP = new PHTEntryType(0x00000003, "INTERP", "Interpreter info");

    /** The array element specifies the location and size of auxiliary information. */
    public static final PHTEntryType PT_NOTE = new PHTEntryType(0x00000004, "NOTE", "Auxiliary info");

    /** This segment type is reserved but has unspecified semantics. */
    public static final PHTEntryType PT_SHLIB = new PHTEntryType(0x00000005, "SHLIB", "Reserved");

    /**
     * The array element, if present, specifies the location and size of the program header table itself, both in the
     * file and in the memory image of the program. This segment type may not occur more than once in a file. Moreover,
     * it may occur only if the program header table is part of the memory image of the program. If it is present, it
     * must precede any loadable segment entry.
     */
    public static final PHTEntryType PT_PHDR = new PHTEntryType(0x00000006, "PHDR", "Program header table");

    /** Specifies a thread-local storage template. */
    public static final PHTEntryType PT_TLS = new PHTEntryType(0x00000007, "TLS", "Thread-Local Storage template");

    /** Values in the inclusive range from this one to PT_HIOS are reserved for OS-specific semantics. */
    public static final PHTEntryType PT_LOOS =
            new PHTEntryType(0x60000000, "OS-specific", "Unknown (OS specific)", false);

    /**
     * The array element specifies the location and size of the exception handling information as defined by the
     * .eh_frame_hdr section.
     */
    public static final PHTEntryType PT_GNU_EH_FRAME =
            new PHTEntryType(0x6474e550, "GNU_EH_FRAME", "Exception handling");

    /**
     * The p_flags member specifies the permissions on the segment containing the stack and is used to indicate wether
     * the stack should be executable. The absense of this header indicates that the stack will be executable.
     */
    public static final PHTEntryType PT_GNU_STACK = new PHTEntryType(0x6474e551, "GNU_STACK", "Stack executablity");

    /**
     * This array element specifies the location and size of a segment which may be made read-only after relocations
     * have been processed.
     */
    public static final PHTEntryType PT_GNU_RELRO =
            new PHTEntryType(0x6474e552, "GNU_RELRO", "Read-only after relocation");

    /** The section .note.gnu.property has this type. */
    public static final PHTEntryType PT_GNU_PROPERTY =
            new PHTEntryType(0x6474e553, "GNU_PROPERTY", ".note.gnu.property notes sections");

    /** Values in the inclusive range from PT_LOOS to this one are reserved for OS-specific semantics. */
    public static final PHTEntryType PT_HIOS =
            new PHTEntryType(0x6fffffff, "OS-specific", "Unknown (OS specific)", false);

    /** Values in the inclusive range from this one to PT_HIPROC are reserved for processor-specific semantics. */
    public static final PHTEntryType PT_LOPROC =
            new PHTEntryType(0x70000000, "CPU-specific", "Unknown (Processor specific)", false);

    /** Values in the inclusive range from PT_LOPROC to this one are reserved for processor-specific semantics. */
    public static final PHTEntryType PT_HIPROC =
            new PHTEntryType(0x7fffffff, "CPU-specific", "Unknown (Processor specific)", false);

    /** Values in the inclusive range from this one to PT_HIUSER are reserved for application programs. */
    public static final PHTEntryType PT_LOUSER =
            new PHTEntryType(0x80000000, "Application-specific", "Unknown (Application specific)", false);

    /** Values in the inclusive range from PT_LOUSER to this one are reserved for application programs. */
    public static final PHTEntryType PT_HIUSER =
            new PHTEntryType(0xffffffff, "Application-specific", "Unknown (Application specific)", false);

    /**
     * Checks whether the given code is a valid program header table entry type.
     *
     * @param code The code to be checked.
     * @return True if it is a valid code, false otherwise.
     */
    public static boolean isValid(final int code) {
        return codeToType.containsKey(code) || (code >= PT_LOOS.code);
    }

    /**
     * Returns the proper program header table entry type object corresponding to the given code.
     *
     * @param code The code to look for.
     * @return The proper type object.
     */
    public static PHTEntryType fromCode(final int code) {
        if (!codeToType.containsKey(code)) {
            if (code >= PT_LOOS.code && code <= PT_HIOS.code) {
                logger.warning("Unknown PHT entry type found: 0x%08x", code);
                return new PHTEntryType(code, "OS-specific", String.format("0x%08x (OS specific)", code), false);
            }
            if (code >= PT_LOPROC.code && code <= PT_HIPROC.code) {
                logger.warning("Unknown PHT entry type found: 0x%08x", code);
                return new PHTEntryType(
                        code, "CPU-specific", String.format("0x%08x (Processor specific)", code), false);
            }
            if (code >= PT_LOUSER.code && code <= PT_HIUSER.code) {
                logger.warning("Unknown PHT entry type found: 0x%08x", code);
                return new PHTEntryType(
                        code, "Application-specific", String.format("0x%08x (Application specific)", code), false);
            }
            throw new IllegalArgumentException(String.format("Unknown ELF PHT entry type identifier: 0x%02x", code));
        }
        return codeToType.get(code);
    }

    private final int code;
    private final String name;
    private final String description;

    private PHTEntryType(final int code, final String name, final String description, final boolean addToMap) {
        this.code = code;
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);

        if (addToMap) {
            if (codeToType.containsKey(code)) {
                throw new IllegalStateException(String.format(
                        "ELF PHT entry type enum value with code %d (0x%02x) and name '%s' already exists",
                        code, code, description));
            }
            codeToType.put(code, this);
        }
    }

    private PHTEntryType(final int code, final String name, final String description) {
        this(code, name, description, true);
    }

    /**
     * Returns the 32-bit code of this type object.
     *
     * @return The code of this typ object.
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the name of this object without the "PT_" prefix.
     *
     * @return The name of this type object.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a brief description of the meaning of this type object.
     *
     * @return A brief description of this type object.
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "PHTEntryType(code=" + code + ";name=" + name + ";description=" + description + ')';
    }
}
