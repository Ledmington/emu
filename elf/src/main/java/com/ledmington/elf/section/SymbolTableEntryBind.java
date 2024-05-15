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
            if (code >= STB_LOOS.getCode() && code <= STB_HIOS.getCode()) {
                logger.warning("Unknown Symbol table entry bind found: 0x%02x", code);
                return new SymbolTableEntryBind(code, "OS-specific", false);
            }
            if (code >= STB_LOPROC.getCode() && code <= STB_HIPROC.getCode()) {
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
