package com.ledmington.elf;

import java.util.HashMap;
import java.util.Map;

/**
 * The OS Application Binary Interface used for creating the ELF file.
 */
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
    Stratus_Technologies_OpenVOS((byte) 0x12, "Stratus Technologies OpenVOS"),
    STANDALONE((byte) 0xff, "Standalone (embedded) application");

    private static final Map<Byte, OSABI> codeToABI = new HashMap<>();

    static {
        for (final OSABI x : OSABI.values()) {
            if (codeToABI.containsKey(x.getCode())) {
                throw new IllegalStateException(String.format(
                        "OSABI enum value with code %d (0x%02x) and name '%s' already exists",
                        x.getCode(), x.getCode(), x.getName()));
            }
            codeToABI.put(x.getCode(), x);
        }
    }

    /**
     * Checks whether the given code corresponds to an existing OS ABI object.
     *
     * @param code
     *      The code to look for.
     * @return
     *      True if an OSABI object exists, false otherwise.
     */
    public static boolean isValid(final byte code) {
        return codeToABI.containsKey(code);
    }

    /**
     * Finds the OSABI object corresponding to the given code.
     *
     * @param code
     *      The code to look for.
     * @return
     *      The OSABI object.
     */
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

    /**
     * Hexadecimal 1-byte code.
     *
     * @return
     *      The code of this OSABI object.
     */
    public byte getCode() {
        return this.code;
    }

    /**
     * Name of the OS ABI.
     *
     * @return
     *      A String representation of this OS ABI object.
     */
    public String getName() {
        return this.OSName;
    }
}
