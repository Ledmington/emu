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

public final class PHTEntryType {

    private static final MiniLogger logger = MiniLogger.getLogger("pht-entry-type");
    private static final Map<Integer, PHTEntryType> codeToType = new HashMap<>();

    public static final PHTEntryType PT_NULL = new PHTEntryType(0x00000000, "NULL", "Unused");
    public static final PHTEntryType PT_LOAD = new PHTEntryType(0x00000001, "LOAD", "Loadable");
    public static final PHTEntryType PT_DYNAMIC = new PHTEntryType(0x00000002, "DYNAMIC", "Dynamic linking info");
    public static final PHTEntryType PT_INTERP = new PHTEntryType(0x00000003, "INTERP", "Interpreter info");
    public static final PHTEntryType PT_NOTE = new PHTEntryType(0x00000004, "NOTE", "Auxiliary info");
    public static final PHTEntryType PT_SHLIB = new PHTEntryType(0x00000005, "SHLIB", "Reserved");
    public static final PHTEntryType PT_PHDR = new PHTEntryType(0x00000006, "PHDR", "Program header table");
    public static final PHTEntryType PT_TLS = new PHTEntryType(0x00000007, "TLS", "Thread-Local Storage template");

    public static final PHTEntryType PT_LOOS =
            new PHTEntryType(0x60000000, "OS-specific", "Unknown (OS specific)", false);
    public static final PHTEntryType PT_GNU_EH_FRAME =
            new PHTEntryType(0x6474e550, "GNU_EH_FRAME", "Exception handling");
    public static final PHTEntryType PT_GNU_STACK = new PHTEntryType(0x6474e551, "GNU_STACK", "Stack executablity");
    public static final PHTEntryType PT_GNU_RELRO =
            new PHTEntryType(0x6474e552, "GNU_RELRO", "Read-only after relocation");
    public static final PHTEntryType PT_GNU_PROPERTY =
            new PHTEntryType(0x6474e553, "GNU_PROPERTY", ".note.gnu.property notes sections");
    public static final PHTEntryType PT_HIOS =
            new PHTEntryType(0x6fffffff, "OS-specific", "Unknown (OS specific)", false);

    public static final PHTEntryType PT_LOPROC =
            new PHTEntryType(0x70000000, "CPU-specific", "Unknown (Processor specific)", false);
    public static final PHTEntryType PT_HIPROC =
            new PHTEntryType(0x7fffffff, "CPU-specific", "Unknown (Processor specific)", false);

    public static final PHTEntryType PT_LOUSER =
            new PHTEntryType(0x80000000, "Application-specific", "Unknown (Application specific)", false);
    public static final PHTEntryType PT_HIUSER =
            new PHTEntryType(0xffffffff, "Application-specific", "Unknown (Application specific)", false);

    public static boolean isValid(final int code) {
        return codeToType.containsKey(code) || (code >= PT_LOOS.getCode());
    }

    public static PHTEntryType fromCode(final int code) {
        if (!codeToType.containsKey(code)) {
            if (code >= PT_LOOS.getCode() && code <= PT_HIOS.getCode()) {
                logger.warning("Unknown PHT entry type found: 0x%08x", code);
                return new PHTEntryType(code, "OS-specific", String.format("0x%08x (OS specific)", code), false);
            }
            if (code >= PT_LOPROC.getCode() && code <= PT_HIPROC.getCode()) {
                logger.warning("Unknown PHT entry type found: 0x%08x", code);
                return new PHTEntryType(
                        code, "CPU-specific", String.format("0x%08x (Processor specific)", code), false);
            }
            if (code >= PT_LOUSER.getCode() && code <= PT_HIUSER.getCode()) {
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

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
