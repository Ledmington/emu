package com.ledmington.elf;

import java.util.HashMap;
import java.util.Map;

public final class SymbolTableEntryType {

    private static final Map<Byte, SymbolTableEntryType> codeToType = new HashMap<>();

    public static final SymbolTableEntryType STT_NOTYPE = new SymbolTableEntryType((byte) 0x00);
    public static final SymbolTableEntryType STT_OBJECT = new SymbolTableEntryType((byte) 0x01);
    public static final SymbolTableEntryType STT_FUNC = new SymbolTableEntryType((byte) 0x02);
    public static final SymbolTableEntryType STT_SECTION = new SymbolTableEntryType((byte) 0x03);
    public static final SymbolTableEntryType STT_FILE = new SymbolTableEntryType((byte) 0x04);
    public static final SymbolTableEntryType STT_COMMON = new SymbolTableEntryType((byte) 0x05);
    public static final SymbolTableEntryType STT_TLS = new SymbolTableEntryType((byte) 0x06);
    public static final SymbolTableEntryType STT_LOOS = new SymbolTableEntryType((byte) 0x0a, false);
    public static final SymbolTableEntryType STT_HIOS = new SymbolTableEntryType((byte) 0x0c, false);
    public static final SymbolTableEntryType STT_LOPROC = new SymbolTableEntryType((byte) 0x0d, false);
    public static final SymbolTableEntryType STT_SPARC_REGISTER = new SymbolTableEntryType((byte) 0x0d, false);
    public static final SymbolTableEntryType STT_HIPROC = new SymbolTableEntryType((byte) 0x0f, false);

    public static SymbolTableEntryType fromCode(final byte code) {
        if (!codeToType.containsKey(code)) {
            throw new IllegalArgumentException(
                    String.format("Unknown Symbol table entry type identifier: 0x%08x", code));
        }
        return codeToType.get(code);
    }

    private final byte code;

    private SymbolTableEntryType(final byte code, final boolean addToMap) {
        this.code = code;

        if (addToMap) {
            if (codeToType.containsKey(code)) {
                throw new IllegalStateException(String.format(
                        "Symbol table entry type value with code %d (0x%02x) already exists", code, code));
            }
            codeToType.put(code, this);
        }
    }

    private SymbolTableEntryType(final byte code) {
        this(code, true);
    }

    public byte code() {
        return code;
    }
}
