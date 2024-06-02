/*
* emu - Processor Emulator
* Copyright (C) 2023-2024 Filippo Barbari <filippo.barbari@gmail.com>
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.ledmington.cpu.x86;

import java.util.Objects;

/** An x86 64-bit general-purpose register. */
public enum Register64 implements Register {

    /** The register RAX. */
    RAX("rax"),

    /** The register RBX. */
    RBX("rbx"),

    /** The register RCX. */
    RCX("rcx"),

    /** The register RDX. */
    RDX("rdx"),

    /** The register RSI. */
    RSI("rsi"),

    /** The register RDI. */
    RDI("rdi"),

    /** The register RSP. */
    RSP("rsp"),

    /** The register RBP. */
    RBP("rbp"),

    /** The register R8. */
    R8("r8"),

    /** The register R9. */
    R9("r9"),

    /** The register R10. */
    R10("r10"),

    /** The register R11. */
    R11("r11"),

    /** The register R12. */
    R12("r12"),

    /** The register R13. */
    R13("r13"),

    /** The register R14. */
    R14("r14"),

    /** The register R15. */
    R15("r15"),

    /** The instruction pointer register RIP. */
    RIP("rip");

    private final String mnemonic;

    Register64(final String mnemonic) {
        this.mnemonic = Objects.requireNonNull(mnemonic);
    }

    /**
     * Returns the 64-bit register corresponding to the given byte.
     *
     * @param b The byte representing a 64-bit register.
     * @return A 64-bit register.
     */
    public static Register64 fromByte(final byte b) {
        return switch (b) {
            case 0x00 -> RAX;
            case 0x01 -> RCX;
            case 0x02 -> RDX;
            case 0x03 -> RBX;
            case 0x04 -> RSP;
            case 0x05 -> RBP;
            case 0x06 -> RSI;
            case 0x07 -> RDI;
            case 0x08 -> R8;
            case 0x09 -> R9;
            case 0x0a -> R10;
            case 0x0b -> R11;
            case 0x0c -> R12;
            case 0x0d -> R13;
            case 0x0e -> R14;
            case 0x0f -> R15;
            default -> throw new IllegalArgumentException(String.format("Unknown register byte 0x%02x", b));
        };
    }

    @Override
    public int bits() {
        return 64;
    }

    @Override
    public String toIntelSyntax() {
        return mnemonic;
    }

    @Override
    public String toString() {
        return "Register64(mnemonic=" + mnemonic + ")";
    }
}
