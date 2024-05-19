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

/** An x86 32-bit general-purpose register. */
public final class Register32 extends Register {

    /** The register EAX. */
    public static final Register32 EAX = new Register32("eax");

    /** The register EBX. */
    public static final Register32 EBX = new Register32("ebx");

    /** The register ECX. */
    public static final Register32 ECX = new Register32("ecx");

    /** The register EDX. */
    public static final Register32 EDX = new Register32("edx");

    /** The register ESI. */
    public static final Register32 ESI = new Register32("esi");

    /** The register EDI. */
    public static final Register32 EDI = new Register32("edi");

    /** The register ESP. */
    public static final Register32 ESP = new Register32("esp");

    /** The register EBP. */
    public static final Register32 EBP = new Register32("ebp");

    /** The register R8D. */
    public static final Register32 R8D = new Register32("r8d");

    /** The register R9D. */
    public static final Register32 R9D = new Register32("r9d");

    /** The register R10D. */
    public static final Register32 R10D = new Register32("r10d");

    /** The register R11D. */
    public static final Register32 R11D = new Register32("r11d");

    /** The register R12D. */
    public static final Register32 R12D = new Register32("r12d");

    /** The register R13D. */
    public static final Register32 R13D = new Register32("r13d");

    /** The register R14D. */
    public static final Register32 R14D = new Register32("r14d");

    /** The register R15D. */
    public static final Register32 R15D = new Register32("r15d");

    /** The instruction pointer register EIP. */
    public static final Register32 EIP = new Register32("eip");

    private Register32(final String mnemonic) {
        super(mnemonic);
    }

    public static Register32 fromByte(final byte b) {
        return switch (b) {
            case 0x00 -> EAX;
            case 0x01 -> ECX;
            case 0x02 -> EDX;
            case 0x03 -> EBX;
            case 0x04 -> ESP;
            case 0x05 -> EBP;
            case 0x06 -> ESI;
            case 0x07 -> EDI;
            case 0x08 -> R8D;
            case 0x09 -> R9D;
            case 0x0a -> R10D;
            case 0x0b -> R11D;
            case 0x0c -> R12D;
            case 0x0d -> R13D;
            case 0x0e -> R14D;
            case 0x0f -> R15D;
            default -> throw new IllegalArgumentException(String.format("Unknown register byte 0x%02x", b));
        };
    }

    @Override
    public int bits() {
        return 32;
    }
}
