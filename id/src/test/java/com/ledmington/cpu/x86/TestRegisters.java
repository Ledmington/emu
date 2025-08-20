/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2025 Filippo Barbari <filippo.barbari@gmail.com>
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.ledmington.utils.BitUtils;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class TestRegisters {

	private static Stream<Arguments> registers() {
		return Stream.of(
				// 16 bits
				Arguments.of((byte) 0x00, false, false, true, Register16.AX),
				Arguments.of((byte) 0x01, false, false, true, Register16.CX),
				Arguments.of((byte) 0x02, false, false, true, Register16.DX),
				Arguments.of((byte) 0x03, false, false, true, Register16.BX),
				Arguments.of((byte) 0x04, false, false, true, Register16.SP),
				Arguments.of((byte) 0x05, false, false, true, Register16.BP),
				Arguments.of((byte) 0x06, false, false, true, Register16.SI),
				Arguments.of((byte) 0x07, false, false, true, Register16.DI),
				Arguments.of((byte) 0x00, false, true, true, Register16.R8W),
				Arguments.of((byte) 0x01, false, true, true, Register16.R9W),
				Arguments.of((byte) 0x02, false, true, true, Register16.R10W),
				Arguments.of((byte) 0x03, false, true, true, Register16.R11W),
				Arguments.of((byte) 0x04, false, true, true, Register16.R12W),
				Arguments.of((byte) 0x05, false, true, true, Register16.R13W),
				Arguments.of((byte) 0x06, false, true, true, Register16.R14W),
				Arguments.of((byte) 0x07, false, true, true, Register16.R15W),
				// 32 bits
				Arguments.of((byte) 0x00, false, false, false, Register32.EAX),
				Arguments.of((byte) 0x01, false, false, false, Register32.ECX),
				Arguments.of((byte) 0x02, false, false, false, Register32.EDX),
				Arguments.of((byte) 0x03, false, false, false, Register32.EBX),
				Arguments.of((byte) 0x04, false, false, false, Register32.ESP),
				Arguments.of((byte) 0x05, false, false, false, Register32.EBP),
				Arguments.of((byte) 0x06, false, false, false, Register32.ESI),
				Arguments.of((byte) 0x07, false, false, false, Register32.EDI),
				Arguments.of((byte) 0x00, false, true, false, Register32.R8D),
				Arguments.of((byte) 0x01, false, true, false, Register32.R9D),
				Arguments.of((byte) 0x02, false, true, false, Register32.R10D),
				Arguments.of((byte) 0x03, false, true, false, Register32.R11D),
				Arguments.of((byte) 0x04, false, true, false, Register32.R12D),
				Arguments.of((byte) 0x05, false, true, false, Register32.R13D),
				Arguments.of((byte) 0x06, false, true, false, Register32.R14D),
				Arguments.of((byte) 0x07, false, true, false, Register32.R15D),
				// 64 bits
				Arguments.of((byte) 0x00, true, false, false, Register64.RAX),
				Arguments.of((byte) 0x01, true, false, false, Register64.RCX),
				Arguments.of((byte) 0x02, true, false, false, Register64.RDX),
				Arguments.of((byte) 0x03, true, false, false, Register64.RBX),
				Arguments.of((byte) 0x04, true, false, false, Register64.RSP),
				Arguments.of((byte) 0x05, true, false, false, Register64.RBP),
				Arguments.of((byte) 0x06, true, false, false, Register64.RSI),
				Arguments.of((byte) 0x07, true, false, false, Register64.RDI),
				Arguments.of((byte) 0x00, true, true, false, Register64.R8),
				Arguments.of((byte) 0x01, true, true, false, Register64.R9),
				Arguments.of((byte) 0x02, true, true, false, Register64.R10),
				Arguments.of((byte) 0x03, true, true, false, Register64.R11),
				Arguments.of((byte) 0x04, true, true, false, Register64.R12),
				Arguments.of((byte) 0x05, true, true, false, Register64.R13),
				Arguments.of((byte) 0x06, true, true, false, Register64.R14),
				Arguments.of((byte) 0x07, true, true, false, Register64.R15));
	}

	@ParameterizedTest
	@MethodSource("registers")
	void decodeRegisters(
			final byte registerCode,
			final boolean is64Bit,
			final boolean extension,
			final boolean hasOperandSizeOverridePrefix,
			final Register expected) {
		final Register actual = Registers.fromCode(registerCode, is64Bit, extension, hasOperandSizeOverridePrefix);
		final boolean actualExtension = Registers.requiresExtension(expected);
		assertEquals(
				extension,
				actualExtension,
				() -> String.format(
						"Expected %s to%s require an extension but it did%s.",
						expected, extension ? "" : " not", actualExtension ? " not" : ""));
		assertEquals(
				expected,
				actual,
				() -> String.format(
						"Decoding 0x%02x, is64Bit=%s, extension=%s, hasOperandSizeOverridePrefix=%s: expected %s but was %s.",
						registerCode, is64Bit, extension, hasOperandSizeOverridePrefix, expected, actual));
	}

	private static Stream<Arguments> registers8Bits() {
		return Stream.of(
				Arguments.of((byte) 0x00, false, false, Register8.AL),
				Arguments.of((byte) 0x04, false, false, Register8.AH),
				Arguments.of((byte) 0x01, false, false, Register8.CL),
				Arguments.of((byte) 0x05, false, false, Register8.CH),
				Arguments.of((byte) 0x02, false, false, Register8.DL),
				Arguments.of((byte) 0x06, false, false, Register8.DH),
				Arguments.of((byte) 0x03, false, false, Register8.BL),
				Arguments.of((byte) 0x07, false, false, Register8.BH),
				Arguments.of((byte) 0x04, true, false, Register8.SPL),
				Arguments.of((byte) 0x05, true, false, Register8.BPL),
				Arguments.of((byte) 0x06, true, false, Register8.SIL),
				Arguments.of((byte) 0x07, true, false, Register8.DIL),
				Arguments.of((byte) 0x00, false, true, Register8.R8B),
				Arguments.of((byte) 0x01, false, true, Register8.R9B),
				Arguments.of((byte) 0x02, false, true, Register8.R10B),
				Arguments.of((byte) 0x03, false, true, Register8.R11B),
				Arguments.of((byte) 0x04, false, true, Register8.R12B),
				Arguments.of((byte) 0x05, false, true, Register8.R13B),
				Arguments.of((byte) 0x06, false, true, Register8.R14B),
				Arguments.of((byte) 0x07, false, true, Register8.R15B));
	}

	@ParameterizedTest
	@MethodSource("registers8Bits")
	void decodeRegisters8Bits(
			final byte registerCode,
			final boolean needsRexPrefix,
			final boolean needsExtension,
			final Register8 expected) {
		final boolean actualRexPrefix = Register8.requiresRexPrefix(expected);
		assertEquals(
				needsRexPrefix,
				actualRexPrefix,
				() -> String.format(
						"Expected %s to%s require a REX prefix but it did%s.",
						expected, needsRexPrefix ? "" : " not", actualRexPrefix ? " not" : ""));
		final boolean actualExtension = Register8.requiresExtension(expected);
		assertEquals(
				needsExtension,
				actualExtension,
				() -> String.format(
						"Expected %s to%s require an extension but it did%s.",
						expected, needsExtension ? "" : " not", actualExtension ? " not" : ""));
		final byte actualByte = BitUtils.or(registerCode, needsExtension ? (byte) 0x08 : 0);
		final Register8 decoded = Register8.fromByte(actualByte, needsRexPrefix);
		assertEquals(
				expected,
				decoded,
				() -> String.format(
						"Expected %s to be decoded from 0x%02x, but it was %s.", expected, actualByte, decoded));
		final byte actualCode = Register8.toByte(expected);
		assertEquals(
				registerCode,
				actualCode,
				() -> String.format(
						"Expected %s to be encoded into 0x%02x, but it was 0x%02x.",
						expected, registerCode, actualCode));
	}

	private static Stream<Arguments> registers128Bits() {
		return Stream.of(
				Arguments.of((byte) 0x00, false, RegisterXMM.XMM0),
				Arguments.of((byte) 0x01, false, RegisterXMM.XMM1),
				Arguments.of((byte) 0x02, false, RegisterXMM.XMM2),
				Arguments.of((byte) 0x03, false, RegisterXMM.XMM3),
				Arguments.of((byte) 0x04, false, RegisterXMM.XMM4),
				Arguments.of((byte) 0x05, false, RegisterXMM.XMM5),
				Arguments.of((byte) 0x06, false, RegisterXMM.XMM6),
				Arguments.of((byte) 0x07, false, RegisterXMM.XMM7),
				Arguments.of((byte) 0x00, true, RegisterXMM.XMM8),
				Arguments.of((byte) 0x01, true, RegisterXMM.XMM9),
				Arguments.of((byte) 0x02, true, RegisterXMM.XMM10),
				Arguments.of((byte) 0x03, true, RegisterXMM.XMM11),
				Arguments.of((byte) 0x04, true, RegisterXMM.XMM12),
				Arguments.of((byte) 0x05, true, RegisterXMM.XMM13),
				Arguments.of((byte) 0x06, true, RegisterXMM.XMM14),
				Arguments.of((byte) 0x07, true, RegisterXMM.XMM15));
	}

	@ParameterizedTest
	@MethodSource("registers128Bits")
	void decodeRegisters128Bits(final byte registerCode, final boolean needsExtensions, final RegisterXMM expected) {
		assertEquals(
				needsExtensions,
				RegisterXMM.requiresExtension(expected),
				() -> String.format(
						"Expected %s to%s require the XMM extension but it did%s.",
						expected, needsExtensions ? "" : " not", needsExtensions ? " not" : ""));
		final byte actualCode = BitUtils.or(registerCode, needsExtensions ? (byte) 0x08 : 0);
		final RegisterXMM actual = RegisterXMM.fromByte(actualCode);
		assertEquals(
				expected,
				actual,
				() -> String.format("Expected to decode %s from 0x%02x but was %s.", expected, actualCode, actual));
		final byte encoded = RegisterXMM.toByte(expected);
		assertEquals(
				registerCode,
				encoded,
				() -> String.format(
						"Expected %s to be encoded as 0x%02x but was 0x%02x.", expected, registerCode, encoded));
	}

	@Test
	void nullExtensionRegister8() {
		assertThrows(NullPointerException.class, () -> Register8.requiresExtension(null));
	}

	@Test
	void nullRexPrefixRegister8() {
		assertThrows(NullPointerException.class, () -> Register8.requiresRexPrefix(null));
	}

	@Test
	void nullEncodeRegister8() {
		assertThrows(NullPointerException.class, () -> Register8.toByte(null));
	}

	@Test
	void nullExtensionRegister16() {
		assertThrows(NullPointerException.class, () -> Register16.requiresExtension(null));
	}

	@Test
	void nullEncodeRegister16() {
		assertThrows(NullPointerException.class, () -> Register16.toByte(null));
	}

	@Test
	void nullExtensionRegister32() {
		assertThrows(NullPointerException.class, () -> Register32.requiresExtension(null));
	}

	@Test
	void nullEncodeRegister32() {
		assertThrows(NullPointerException.class, () -> Register32.toByte(null));
	}

	@Test
	void nullExtensionRegister64() {
		assertThrows(NullPointerException.class, () -> Register64.requiresExtension(null));
	}

	@Test
	void nullEncodeRegister64() {
		assertThrows(NullPointerException.class, () -> Register64.toByte(null));
	}

	@Test
	void nullExtensionRegisterXMM() {
		assertThrows(NullPointerException.class, () -> RegisterXMM.requiresExtension(null));
	}

	@Test
	void nullEncodeRegisterXMM() {
		assertThrows(NullPointerException.class, () -> RegisterXMM.toByte(null));
	}

	@Test
	void nullExtensionRegister() {
		assertThrows(NullPointerException.class, () -> Registers.requiresExtension(null));
	}

	@Test
	void nullEncodeRegister() {
		assertThrows(NullPointerException.class, () -> Registers.toByte(null));
	}
}
