package com.ledmington.cpu.x86;

import com.ledmington.utils.BitUtils;

public final class ModRM {

    private final byte mod;
    private final byte reg;
    private final byte rm;

    public static byte extractMod(final byte m) {
        final byte MODRM_MOD_MASK = (byte) 0xc0; // 11000000
        return BitUtils.shr(BitUtils.and(m, MODRM_MOD_MASK), 6);
    }

    public static byte extractReg(final byte m) {
        final byte MODRM_REG_MASK = (byte) 0x38; // 00111000
        return BitUtils.shr(BitUtils.and(m, MODRM_REG_MASK), 3);
    }

    public static byte extractRM(final byte m) {
        final byte MODRM_RM_MASK = (byte) 0x07; // 00000111
        return BitUtils.and(m, MODRM_RM_MASK);
    }

    public ModRM(final byte m) {
        this.mod = extractMod(m);
        this.reg = extractReg(m);
        this.rm = extractRM(m);
    }

    public byte mod() {
        return mod;
    }

    public byte reg() {
        return reg;
    }

    public byte rm() {
        return rm;
    }

    public int hashCode() {
        int h = 17;
        h = 31 * h + BitUtils.asInt(mod);
        h = 31 * h + BitUtils.asInt(reg);
        h = 31 * h + BitUtils.asInt(rm);
        return h;
    }

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
        final ModRM m = (ModRM) other;
        return this.mod == m.mod && this.reg == m.reg && this.rm == m.rm;
    }

    public String toString() {
        return "mod:" + BitUtils.toBinaryString(mod).substring(0, 2) + " reg:"
                + BitUtils.toBinaryString(reg).substring(0, 3) + " r/m:"
                + BitUtils.toBinaryString(rm).substring(0, 3);
    }
}
