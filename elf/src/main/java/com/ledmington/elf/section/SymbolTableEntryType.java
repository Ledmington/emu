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

public final class SymbolTableEntryType {

    private static final MiniLogger logger = MiniLogger.getLogger("symtab-type");
    private static final Map<Byte, SymbolTableEntryType> codeToType = new HashMap<>();

    public static final SymbolTableEntryType STT_NOTYPE = new SymbolTableEntryType((byte) 0x00, "NOTYPE");
    public static final SymbolTableEntryType STT_OBJECT = new SymbolTableEntryType((byte) 0x01, "OBJECT");
    public static final SymbolTableEntryType STT_FUNC = new SymbolTableEntryType((byte) 0x02, "FUNC");
    public static final SymbolTableEntryType STT_SECTION = new SymbolTableEntryType((byte) 0x03, "SECTION");
    public static final SymbolTableEntryType STT_FILE = new SymbolTableEntryType((byte) 0x04, "FILE");
    public static final SymbolTableEntryType STT_COMMON = new SymbolTableEntryType((byte) 0x05, "COMMON");
    public static final SymbolTableEntryType STT_TLS = new SymbolTableEntryType((byte) 0x06, "TLS");
    public static final SymbolTableEntryType STT_LOOS = new SymbolTableEntryType((byte) 0x0a, "OS-specific", false);
    public static final SymbolTableEntryType STT_HIOS = new SymbolTableEntryType((byte) 0x0c, "OS-specific", false);
    public static final SymbolTableEntryType STT_LOPROC =
            new SymbolTableEntryType((byte) 0x0d, "Processor-specific", false);
    public static final SymbolTableEntryType STT_SPARC_REGISTER =
            new SymbolTableEntryType((byte) 0x0d, "SPARC_REGISTER");
    public static final SymbolTableEntryType STT_HIPROC =
            new SymbolTableEntryType((byte) 0x0f, "Processor-specific", false);

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

    public byte getCode() {
        return code;
    }

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
