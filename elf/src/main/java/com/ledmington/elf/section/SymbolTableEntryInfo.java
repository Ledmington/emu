package com.ledmington.elf.section;

import java.util.Objects;

public final class SymbolTableEntryInfo {

    private static final byte mask = (byte) 0x0f;

    public static SymbolTableEntryInfo fromByte(final byte info) {
        return new SymbolTableEntryInfo(
                SymbolTableEntryBind.fromCode((byte) ((info >>> 4) & mask)),
                SymbolTableEntryType.fromCode((byte) (info & mask)));
    }

    private final SymbolTableEntryBind bind;

    private final SymbolTableEntryType type;

    private SymbolTableEntryInfo(final SymbolTableEntryBind bind, final SymbolTableEntryType type) {
        this.bind = Objects.requireNonNull(bind);
        this.type = Objects.requireNonNull(type);
    }

    public byte toByte() {
        return (byte) ((bind.code() << 4) | (type.code()));
    }

    public SymbolTableEntryBind bind() {
        return bind;
    }

    public SymbolTableEntryType type() {
        return type;
    }

    public String toString() {
        return bind + " " + type;
    }
}
