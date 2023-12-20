package com.ledmington.elf;

import java.util.HashMap;
import java.util.Map;

public enum OSABI {
    SYSTEM_V((byte) 0x00, "System V"),
    HP_UX((byte) 0x01, "HP-UX"),
    NetBSD((byte) 0x02, "NetBSD"),
    Linux((byte) 0x03, "Linux"),
    GNU_Hurd((byte) 0x04, "GNU Hurd"),
    Solaris((byte) 0x06, "Solaris"),
    AIX_Monterey((byte) 0x07, "AIX (Monterey)"),
    IRIX((byte) 0x08, "IRIX"),
    FreeBSD((byte) 0x09, "FreeBSD"),
    Tru64((byte) 0x0a, "Tru64"),
    Novell_Modesto((byte) 0x0b, "Novell Modesto"),
    OpenBSD((byte) 0x0c, "OpenBSD"),
    OpenVMS((byte) 0x0d, "OpenVMS"),
    NonStop_Kernel((byte) 0x0e, "NonStop Kernel"),
    AROS((byte) 0x0f, "AROS"),
    Fenix_OS((byte) 0x10, "Fenix OS"),
    Nuxi_CloudABI((byte) 0x11, "Nuxi CloudABI"),
    Stratus_Technologies_OpenVOS((byte) 0x12, "Stratus Technologies OpenVOS");

    private static final Map<Byte, OSABI> codeToABI = new HashMap<>();

    static {
        for (final OSABI x : OSABI.values()) {
            if (codeToABI.containsKey(x.code())) {
                throw new IllegalStateException(String.format(
                        "OSABI enum value with code %d (0x%02x) and name '%s' already exists",
                        x.code(), x.code(), x.OSName()));
            }
            codeToABI.put(x.code(), x);
        }
    }

    public static boolean isValid(final byte code) {
        return codeToABI.containsKey(code);
    }

    public static OSABI fromCode(final byte code) {
        if (!codeToABI.containsKey(code)) {
            throw new IllegalArgumentException(String.format("Unknown ELF OS/ABI identifier: 0x%02x", code));
        }
        return codeToABI.get(code);
    }

    private final byte code;
    private final String OSName;

    OSABI(final byte code, final String OSName) {
        this.code = code;
        this.OSName = OSName;
    }

    public byte code() {
        return this.code;
    }

    public String OSName() {
        return this.OSName;
    }
}
