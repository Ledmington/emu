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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.ledmington.utils.BitUtils;

/**
 * The type of an ELF symbol table entry. A symbol's type provides a general classification for the associated entity.
 */
public enum SymbolTableEntryType {

    /** The symbol's type is not specified. */
    STT_NOTYPE((byte) 0x00, "NOTYPE"),

    /** The symbol is associated with a data object, such as a variable, an array, and so on. */
    STT_OBJECT((byte) 0x01, "OBJECT"),

    /** The symbol is associated with a function or other executable code. */
    STT_FUNC((byte) 0x02, "FUNC"),

    /**
     * The symbol is associated with a section. Symbol table entries of this type exist primarily for relocation and
     * normally have STB_LOCAL binding.
     */
    STT_SECTION((byte) 0x03, "SECTION"),

    /**
     * A file symbol has STB_LOCAL binding, its section index is SHN_ABS, and it precedes the other STB_LOCAL symbols
     * for the file, if it is present.
     */
    STT_FILE((byte) 0x04, "FILE"),

    /** This symbol labels an uninitialized common block. This symbol is treated exactly the same as STT_OBJECT. */
    STT_COMMON((byte) 0x05, "COMMON"),

    /**
     * The symbol specifies a thread-local storage entity. When defined, this symbol gives the assigned offset for the
     * symbol, not the actual address.
     *
     * <p>Thread-local storage relocations can only reference symbols with type STT_TLS. A reference to a symbol of type
     * STT_TLS from an allocatable section, can only be achieved by using special thread-local storage relocations. A
     * reference to a symbol of type STT_TLS from a non-allocatable section does not have this restriction.
     */
    STT_TLS((byte) 0x06, "TLS");

    private static final Map<Byte, SymbolTableEntryType> codeToType = new HashMap<>();

    static {
        for (final SymbolTableEntryType type : values()) {
            codeToType.put(type.getCode(), type);
        }
    }

    private static boolean isOSSpecific(final byte code) {
        return BitUtils.and(code, (byte) 0x0a) == (byte) 0x0a;
    }

    private static boolean isCPUSpecific(final byte code) {
        return BitUtils.and(code, (byte) 0x0d) == (byte) 0x0d;
    }

    /**
     * Returns the STT object corresponding to the given code.
     *
     * @param code The code representing the STT object.
     * @return The STT object.
     */
    public static SymbolTableEntryType fromCode(final byte code) {
        if (!codeToType.containsKey(code)) {
            if (isOSSpecific(code)) {
                throw new IllegalArgumentException(
                        String.format("Unknown OS-specific Symbol table entry type identifier: 0x%02x", code));
            }
            if (isCPUSpecific(code)) {
                throw new IllegalArgumentException(
                        String.format("Unknown CPU-specific Symbol table entry type identifier: 0x%02x", code));
            }
            throw new IllegalArgumentException(
                    String.format("Unknown Symbol table entry type identifier: 0x%02x", code));
        }
        return codeToType.get(code);
    }

    private final byte code;
    private final String name;

    private SymbolTableEntryType(final byte code, final String name) {
        this.code = code;
        this.name = Objects.requireNonNull(name);
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
}
