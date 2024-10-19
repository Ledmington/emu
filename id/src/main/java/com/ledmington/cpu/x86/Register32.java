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

/** An x86 32-bit general-purpose register. */
public enum Register32 implements Register {

	/** The register EAX. */
	EAX("eax"),

	/** The register EBX. */
	EBX("ebx"),

	/** The register ECX. */
	ECX("ecx"),

	/** The register EDX. */
	EDX("edx"),

	/** The register ESI. */
	ESI("esi"),

	/** The register EDI. */
	EDI("edi"),

	/** The register ESP. */
	ESP("esp"),

	/** The register EBP. */
	EBP("ebp"),

	/** The register R8D. */
	R8D("r8d"),

	/** The register R9D. */
	R9D("r9d"),

	/** The register R10D. */
	R10D("r10d"),

	/** The register R11D. */
	R11D("r11d"),

	/** The register R12D. */
	R12D("r12d"),

	/** The register R13D. */
	R13D("r13d"),

	/** The register R14D. */
	R14D("r14d"),

	/** The register R15D. */
	R15D("r15d"),

	/** The instruction pointer register EIP. */
	EIP("eip");

	private final String mnemonic;

	Register32(final String mnemonic) {
		this.mnemonic = Objects.requireNonNull(mnemonic);
	}

	/**
	 * Returns the 32-bit register corresponding to the given byte.
	 *
	 * @param b The byte representing a 32-bit register.
	 * @return A 32-bit register.
	 */
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

	@Override
	public String toIntelSyntax() {
		return mnemonic;
	}

	@Override
	public String toString() {
		return "Register32(mnemonic=" + mnemonic + ")";
	}
}
