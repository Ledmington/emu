package com.ledmington.elf;

import java.util.HashMap;
import java.util.Map;

public final class SymbolTableEntryBind {

    private static final Map<Byte, SymbolTableEntryBind> codeToBind = new HashMap<>();

    public static final SymbolTableEntryBind STB_LOCAL = new SymbolTableEntryBind((byte) 0x00);
    public static final SymbolTableEntryBind STB_GLOBAL = new SymbolTableEntryBind((byte) 0x01);
    public static final SymbolTableEntryBind STB_WEAK = new SymbolTableEntryBind((byte) 0x02);
    public static final SymbolTableEntryBind STB_LOOS = new SymbolTableEntryBind((byte) 0x0a, false);
    public static final SymbolTableEntryBind STB_HIOS = new SymbolTableEntryBind((byte) 0x0c, false);
    public static final SymbolTableEntryBind STB_LOPROC = new SymbolTableEntryBind((byte) 0x0d, false);
    public static final SymbolTableEntryBind STB_HIPROC = new SymbolTableEntryBind((byte) 0x0f, false);

    public static SymbolTableEntryBind fromCode(final byte code) {
        if (!codeToBind.containsKey(code)) {
            throw new IllegalArgumentException(
                    String.format("Unknown Symbol table entry bind identifier: 0x%08x", code));
        }
        return codeToBind.get(code);
    }

    private final byte code;

    private SymbolTableEntryBind(final byte code, final boolean addToMap) {
        this.code = code;

        if (addToMap) {
            if (codeToBind.containsKey(code)) {
                throw new IllegalStateException(String.format(
                        "Symbol table entry bind value with code %d (0x%02x) already exists", code, code));
            }
            codeToBind.put(code, this);
        }
    }

    private SymbolTableEntryBind(final byte code) {
        this(code, true);
    }

    public byte code() {
        return code;
    }
}
