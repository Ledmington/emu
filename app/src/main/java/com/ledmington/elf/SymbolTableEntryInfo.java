package com.ledmington.elf;

import java.util.Objects;

public final class SymbolTableEntryInfo {
    public static SymbolTableEntryInfo fromByte(final byte info) {
        return new SymbolTableEntryInfo(
                SymbolTableEntryBind.fromCode((byte) ((info >>> 4) & 0x0f)),
                SymbolTableEntryType.fromCode((byte) (info & 0x0f)));
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

    public String toString() {
        return bind + " " + type;
    }
}
