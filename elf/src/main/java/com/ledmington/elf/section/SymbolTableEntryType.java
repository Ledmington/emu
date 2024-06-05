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
import java.util.Objects;

import com.ledmington.utils.HashUtils;
import com.ledmington.utils.MiniLogger;

/**
 * The type of an ELF symbol table entry. A symbol's type provides a general classification for the associated entity.
 */
public final class SymbolTableEntryType {

    private static final MiniLogger logger = MiniLogger.getLogger("symtab-type");
    private static final Map<Byte, SymbolTableEntryType> codeToType = new HashMap<>();

    /** The symbol's type is not specified. */
    public static final SymbolTableEntryType STT_NOTYPE = new SymbolTableEntryType((byte) 0x00, "NOTYPE");

    /** The symbol is associated with a data object, such as a variable, an array, and so on. */
    public static final SymbolTableEntryType STT_OBJECT = new SymbolTableEntryType((byte) 0x01, "OBJECT");

    /** The symbol is associated with a function or other executable code. */
    public static final SymbolTableEntryType STT_FUNC = new SymbolTableEntryType((byte) 0x02, "FUNC");

    /**
     * The symbol is associated with a section. Symbol table entries of this type exist primarily for relocation and
     * normally have STB_LOCAL binding.
     */
    public static final SymbolTableEntryType STT_SECTION = new SymbolTableEntryType((byte) 0x03, "SECTION");

    /**
     * A file symbol has STB_LOCAL binding, its section index is SHN_ABS, and it precedes the other STB_LOCAL symbols
     * for the file, if it is present.
     */
    public static final SymbolTableEntryType STT_FILE = new SymbolTableEntryType((byte) 0x04, "FILE");

    /** This symbol labels an uninitialized common block. This symbol is treated exactly the same as STT_OBJECT. */
    public static final SymbolTableEntryType STT_COMMON = new SymbolTableEntryType((byte) 0x05, "COMMON");

    /**
     * The symbol specifies a thread-local storage entity. When defined, this symbol gives the assigned offset for the
     * symbol, not the actual address.
     *
     * <p>Thread-local storage relocations can only reference symbols with type STT_TLS. A reference to a symbol of type
     * STT_TLS from an allocatable section, can only be achieved by using special thread-local storage relocations. A
     * reference to a symbol of type STT_TLS from a non-allocatable section does not have this restriction.
     */
    public static final SymbolTableEntryType STT_TLS = new SymbolTableEntryType((byte) 0x06, "TLS");

    /** Values in the inclusive range from this one to STT_HIOS are reserved for OS-specific semantics. */
    public static final SymbolTableEntryType STT_LOOS = new SymbolTableEntryType((byte) 0x0a, "OS-specific", false);

    /** Values in the inclusive range from STT_LOOS to this one are reserved for OS-specific semantics. */
    public static final SymbolTableEntryType STT_HIOS = new SymbolTableEntryType((byte) 0x0c, "OS-specific", false);

    /** Values in the inclusive range from this one to STT_HIPROC are reserved for processor-specific semantics. */
    public static final SymbolTableEntryType STT_LOPROC =
            new SymbolTableEntryType((byte) 0x0d, "Processor-specific", false);

    /** Values in the inclusive range from STT_LOPROC to this one are reserved for processor-specific semantics. */
    public static final SymbolTableEntryType STT_HIPROC =
            new SymbolTableEntryType((byte) 0x0f, "Processor-specific", false);

    /**
     * Returns the STT object corresponding to the given code.
     *
     * @param code The code representing the STT object.
     * @return The STT object.
     */
    public static SymbolTableEntryType fromCode(final byte code) {
        if (!codeToType.containsKey(code)) {
            if (code >= STT_LOOS.code && code <= STT_HIOS.code) {
                logger.warning("Unknown Symbol table entry type found: 0x%02x", code);
                return new SymbolTableEntryType(code, "OS-specific", false);
            }
            if (code >= STT_LOPROC.code && code <= STT_HIPROC.code) {
                logger.warning("Unknown Symbol table entry type found: 0x%02x", code);
                return new SymbolTableEntryType(code, "Processor-specific", false);
            }
            throw new IllegalArgumentException(
                    String.format("Unknown Symbol table entry type identifier: 0x%02x", code));
        }
        return codeToType.get(code);
    }

    private final byte code;
    private final String name;

    private SymbolTableEntryType(final byte code, final String name, final boolean addToMap) {
        this.code = code;
        this.name = Objects.requireNonNull(name);

        if (addToMap) {
            if (codeToType.containsKey(code)) {
                throw new IllegalStateException(String.format(
                        "Symbol table entry type value with code %d (0x%02x) already exists", code, code));
            }
            codeToType.put(code, this);
        }
    }

    private SymbolTableEntryType(final byte code, final String name) {
        this(code, name, true);
    }

    /**
     * Returns the 8-bit code of this STT object.
     *
     * @return The 8-bit code.
     */
    public byte getCode() {
        return code;
    }

    /**
     * Returns the name of this STT object without the "STT_" prefix.
     *
     * @return The name of this STT object.
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "SymbolTableEntryType(code=" + code + ";name=" + name + ')';
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + HashUtils.hash(code);
        h = 31 * h + name.hashCode();
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
        final SymbolTableEntryType stet = (SymbolTableEntryType) other;
        return this.code == stet.code && this.name.equals(stet.name);
    }
}
