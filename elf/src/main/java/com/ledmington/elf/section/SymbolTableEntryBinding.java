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

import com.ledmington.utils.MiniLogger;

/** A symbol's binding determines the linkage visibility and behavior. */
public final class SymbolTableEntryBinding {

    private static final MiniLogger logger = MiniLogger.getLogger("symtab-bind");
    private static final Map<Byte, SymbolTableEntryBinding> codeToBind = new HashMap<>();

    /**
     * Local symbols are not visible outside the object file containing their definition. Local symbols of the same name
     * may exist in multiple files without interfering with each other.
     */
    public static final SymbolTableEntryBinding STB_LOCAL = new SymbolTableEntryBinding((byte) 0x00, "LOCAL");

    /**
     * Global symbols are visible to all object files being combined. One file's definition of a global symbol will
     * satisfy another file's undefined reference to the same global symbol.
     */
    public static final SymbolTableEntryBinding STB_GLOBAL = new SymbolTableEntryBinding((byte) 0x01, "GLOBAL");

    /** Weak symbols resemble global symbols, but their definitions have lower precedence. */
    public static final SymbolTableEntryBinding STB_WEAK = new SymbolTableEntryBinding((byte) 0x02, "WEAK");

    /** Values in the inclusive range from this one to STB_HIOS are reserved for OS-specific semantics. */
    public static final SymbolTableEntryBinding STB_LOOS =
            new SymbolTableEntryBinding((byte) 0x0a, "OS-specific", false);

    /** Values in the inclusive range from STB_LOOS to this one are reserved for OS-specific semantics. */
    public static final SymbolTableEntryBinding STB_HIOS =
            new SymbolTableEntryBinding((byte) 0x0c, "OS-specific", false);

    /** Values in the inclusive range from this one to STB_HIPROC are reserved for processor-specific semantics. */
    public static final SymbolTableEntryBinding STB_LOPROC =
            new SymbolTableEntryBinding((byte) 0x0d, "Processor-specific", false);

    /** Values in the inclusive range from STB_LOPROC to this one are reserved for processor-specific semantics. */
    public static final SymbolTableEntryBinding STB_HIPROC =
            new SymbolTableEntryBinding((byte) 0x0f, "Processor-specific", false);

    /**
     * Returns the proper STE binding object corresponding to the given code.
     *
     * @param code The code of the binding object.
     * @return A non-null STE binding object.
     */
    public static SymbolTableEntryBinding fromCode(final byte code) {
        if (!codeToBind.containsKey(code)) {
            if (code >= STB_LOOS.code && code <= STB_HIOS.code) {
                logger.warning("Unknown Symbol table entry bind found: 0x%02x", code);
                return new SymbolTableEntryBinding(code, "OS-specific", false);
            }
            if (code >= STB_LOPROC.code && code <= STB_HIPROC.code) {
                logger.warning("Unknown Symbol table entry bind found: 0x%02x", code);
                return new SymbolTableEntryBinding(code, "Processor-specific", false);
            }
            throw new IllegalArgumentException(
                    String.format("Unknown Symbol table entry bind identifier: 0x%02x", code));
        }
        return codeToBind.get(code);
    }

    private final byte code;
    private final String name;

    private SymbolTableEntryBinding(final byte code, final String name, final boolean addToMap) {
        this.code = code;
        this.name = Objects.requireNonNull(name);

        if (addToMap) {
            if (codeToBind.containsKey(code)) {
                throw new IllegalStateException(String.format(
                        "Symbol table entry bind value with code %d (0x%02x) already exists", code, code));
            }
            codeToBind.put(code, this);
        }
    }

    private SymbolTableEntryBinding(final byte code, final String name) {
        this(code, name, true);
    }

    /**
     * Returns the code of this object.
     *
     * @return The code of this object.
     */
    public byte getCode() {
        return code;
    }

    /**
     * Returns the name of this object without the "STB_" prefix.
     *
     * @return The name of this object.
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "SymbolTableEntryBinding(name=" + name + ";code=" + code + ")";
    }
}
