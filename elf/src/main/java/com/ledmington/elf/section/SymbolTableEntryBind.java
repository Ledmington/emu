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

public final class SymbolTableEntryBind {

    private static final MiniLogger logger = MiniLogger.getLogger("symtab-bind");
    private static final Map<Byte, SymbolTableEntryBind> codeToBind = new HashMap<>();

    public static final SymbolTableEntryBind STB_LOCAL = new SymbolTableEntryBind((byte) 0x00, "LOCAL");
    public static final SymbolTableEntryBind STB_GLOBAL = new SymbolTableEntryBind((byte) 0x01, "GLOBAL");
    public static final SymbolTableEntryBind STB_WEAK = new SymbolTableEntryBind((byte) 0x02, "WEAK");
    public static final SymbolTableEntryBind STB_LOOS = new SymbolTableEntryBind((byte) 0x0a, "OS-specific", false);
    public static final SymbolTableEntryBind STB_HIOS = new SymbolTableEntryBind((byte) 0x0c, "OS-specific", false);
    public static final SymbolTableEntryBind STB_LOPROC =
            new SymbolTableEntryBind((byte) 0x0d, "Processor-specific", false);
    public static final SymbolTableEntryBind STB_HIPROC =
            new SymbolTableEntryBind((byte) 0x0f, "Processor-specific", false);

    public static SymbolTableEntryBind fromCode(final byte code) {
        if (!codeToBind.containsKey(code)) {
            if (code >= STB_LOOS.code && code <= STB_HIOS.code) {
                logger.warning("Unknown Symbol table entry bind found: 0x%02x", code);
                return new SymbolTableEntryBind(code, "OS-specific", false);
            }
            if (code >= STB_LOPROC.code && code <= STB_HIPROC.code) {
                logger.warning("Unknown Symbol table entry bind found: 0x%02x", code);
                return new SymbolTableEntryBind(code, "Processor-specific", false);
            }
            throw new IllegalArgumentException(
                    String.format("Unknown Symbol table entry bind identifier: 0x%02x", code));
        }
        return codeToBind.get(code);
    }

    private final byte code;
    private final String name;

    private SymbolTableEntryBind(final byte code, final String name, final boolean addToMap) {
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

    private SymbolTableEntryBind(final byte code, final String name) {
        this(code, name, true);
    }

    public byte getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
