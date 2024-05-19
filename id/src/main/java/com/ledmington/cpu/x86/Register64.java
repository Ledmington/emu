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

/** An x86 64-bit general-purpose register. */
public final class Register64 extends Register {

    /** The register RAX. */
    public static final Register64 RAX = new Register64("rax");

    /** The register RBX. */
    public static final Register64 RBX = new Register64("rbx");

    /** The register RCX. */
    public static final Register64 RCX = new Register64("rcx");

    /** The register RDX. */
    public static final Register64 RDX = new Register64("rdx");

    /** The register RSI. */
    public static final Register64 RSI = new Register64("rsi");

    /** The register RDI. */
    public static final Register64 RDI = new Register64("rdi");

    /** The register RSP. */
    public static final Register64 RSP = new Register64("rsp");

    /** The register RBP. */
    public static final Register64 RBP = new Register64("rbp");

    /** The register R8. */
    public static final Register64 R8 = new Register64("r8");

    /** The register R9. */
    public static final Register64 R9 = new Register64("r9");

    /** The register R10. */
    public static final Register64 R10 = new Register64("r10");

    /** The register R11. */
    public static final Register64 R11 = new Register64("r11");

    /** The register R12. */
    public static final Register64 R12 = new Register64("r12");

    /** The register R13. */
    public static final Register64 R13 = new Register64("r13");

    /** The register R14. */
    public static final Register64 R14 = new Register64("r14");

    /** The register R15. */
    public static final Register64 R15 = new Register64("r15");

    /** The instruction pointer register RIP. */
    public static final Register64 RIP = new Register64("rip");

    private Register64(final String mnemonic) {
        super(mnemonic);
    }

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
}
