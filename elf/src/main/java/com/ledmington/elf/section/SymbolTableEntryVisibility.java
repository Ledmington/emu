package com.ledmington.elf.section;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class SymbolTableEntryVisibility {

    private static final Map<Byte, SymbolTableEntryVisibility> codeToVisibility = new HashMap<>();

    public static final SymbolTableEntryVisibility STV_DEFAULT = new SymbolTableEntryVisibility((byte) 0, "DEFAULT");
    public static final SymbolTableEntryVisibility STV_INTERNAL = new SymbolTableEntryVisibility((byte) 1, "INTERNAL");
    public static final SymbolTableEntryVisibility STV_HIDDEN = new SymbolTableEntryVisibility((byte) 2, "HIDDEN");
    public static final SymbolTableEntryVisibility STV_PROTECTED =
            new SymbolTableEntryVisibility((byte) 3, "PROTECTED");

    public static SymbolTableEntryVisibility fromByte(final byte code) {
        if (!codeToVisibility.containsKey(code)) {
            throw new IllegalArgumentException(
                    String.format("Unknown Symbol table entry visibility identifier: 0x%02x", code));
        }
        return codeToVisibility.get(code);
    }

    private final byte code;
    private final String name;

    private SymbolTableEntryVisibility(final byte code, final String name) {
        this.code = code;
        this.name = Objects.requireNonNull(name);

        if (codeToVisibility.containsKey(code)) {
            throw new IllegalStateException(String.format(
                    "Symbol table entry visibility value with code %d (0x%02x) already exists", code, code));
        }
        codeToVisibility.put(code, this);
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
