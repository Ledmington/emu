package com.ledmington.elf.section;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
            if (code >= STT_LOOS.code() && code <= STT_HIOS.code()) {
                logger.warning("Unknown Symbol table entry type found: 0x%02x", code);
                return new SymbolTableEntryType(code, "OS-specific", false);
            }
            if (code >= STT_LOPROC.code() && code <= STT_HIPROC.code()) {
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

    public byte code() {
        return code;
    }

    public String name() {
        return name;
    }

    public String toString() {
        return name;
    }
}
