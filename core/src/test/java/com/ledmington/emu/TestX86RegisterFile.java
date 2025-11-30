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
package com.ledmington.emu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.ledmington.cpu.x86.Register16;
import com.ledmington.cpu.x86.Register32;
import com.ledmington.cpu.x86.Register64;
import com.ledmington.cpu.x86.Register8;
import com.ledmington.cpu.x86.SegmentRegister;
import com.ledmington.utils.BitUtils;

final class TestX86RegisterFile {

	private static final RandomGenerator RNG =
			RandomGeneratorFactory.getDefault().create(42);
	private static final Register8[] ALL_8_BIT_REGISTERS = {
		Register8.AL,
		Register8.AH,
		Register8.BL,
		Register8.BH,
		Register8.CL,
		Register8.CH,
		Register8.DL,
		Register8.DH,
		Register8.R8B,
		Register8.R9B,
		Register8.R10B,
		Register8.R11B,
		Register8.R12B,
		Register8.R13B,
		Register8.R14B,
		Register8.R15B,
		Register8.BPL,
		Register8.SPL,
		Register8.SIL,
		Register8.DIL
	};
	private static final Register16[] ALL_16_BIT_REGISTERS = {
		Register16.AX,
		Register16.BX,
		Register16.CX,
		Register16.DX,
		Register16.SP,
		Register16.BP,
		Register16.SI,
		Register16.DI,
		Register16.R8W,
		Register16.R9W,
		Register16.R10W,
		Register16.R11W,
		Register16.R12W,
		Register16.R13W,
		Register16.R14W,
		Register16.R15W
	};
	private static final Register32[] ALL_32_BIT_REGISTERS = {
		Register32.EAX,
		Register32.EBX,
		Register32.ECX,
		Register32.EDX,
		Register32.ESP,
		Register32.EBP,
		Register32.ESI,
		Register32.EDI,
		Register32.R8D,
		Register32.R9D,
		Register32.R10D,
		Register32.R11D,
		Register32.R12D,
		Register32.R13D,
		Register32.R14D,
		Register32.R15D,
		Register32.EIP
	};
	private static final Register64[] ALL_64_BIT_REGISTERS = {
		Register64.RAX,
		Register64.RBX,
		Register64.RCX,
		Register64.RDX,
		Register64.RSP,
		Register64.RBP,
		Register64.RSI,
		Register64.RDI,
		Register64.R8,
		Register64.R9,
		Register64.R10,
		Register64.R11,
		Register64.R12,
		Register64.R13,
		Register64.R14,
		Register64.R15,
		Register64.RIP
	};
	private static final SegmentRegister[] ALL_SEGMENT_REGISTERS = {
		SegmentRegister.CS,
		SegmentRegister.DS,
		SegmentRegister.ES,
		SegmentRegister.FS,
		SegmentRegister.GS,
		SegmentRegister.SS
	};
	private X86RegisterFile regFile;

	@BeforeEach
	void setup() {
		regFile = new X86RegisterFile();
	}

	private static Stream<Arguments> all8BitsRegisters() {
		return Arrays.stream(ALL_8_BIT_REGISTERS).map(Arguments::of);
	}

	@ParameterizedTest
	@MethodSource("all8BitsRegisters")
	void initiallyAllZero(final Register8 r) {
		assertEquals(
				(byte) 0x00,
				regFile.get(r),
				() -> String.format("Expected register %s to be initially zero but was 0x%02x", r, regFile.get(r)));
	}

	@ParameterizedTest
	@MethodSource("all8BitsRegisters")
	void setToValue(final Register8 r) {
		final byte x = BitUtils.asByte(RNG.nextInt(1, 256));
		regFile.set(r, x);
		assertEquals(
				x,
				regFile.get(r),
				() -> String.format("Expected register %s to be 0x%02x but was 0x%02x", r, x, regFile.get(r)));
	}

	@ParameterizedTest
	@MethodSource("all8BitsRegisters")
	void setToValueShouldNotChangeOtherRegisters(final Register8 r) {
		final byte x = BitUtils.asByte(RNG.nextInt(1, 256));
		regFile.set(r, x);

		for (final Register8 other : ALL_8_BIT_REGISTERS) {
			if (r == other) {
				continue;
			}
			assertEquals(
					(byte) 0x00,
					regFile.get(other),
					() -> String.format("Expected register %s to be zero but was 0x%02x", other, regFile.get(other)));
		}
	}

	private static Stream<Arguments> all16BitsRegisters() {
		return Arrays.stream(ALL_16_BIT_REGISTERS).map(Arguments::of);
	}

	@ParameterizedTest
	@MethodSource("all16BitsRegisters")
	void initiallyAllZero(final Register16 r) {
		assertEquals(
				(short) 0x0000,
				regFile.get(r),
				() -> String.format("Expected register %s to be initially zero but was 0x%04x", r, regFile.get(r)));
	}

	@ParameterizedTest
	@MethodSource("all16BitsRegisters")
	void setToValue(final Register16 r) {
		final short x = BitUtils.asShort(RNG.nextInt(1, 65_536));
		regFile.set(r, x);
		assertEquals(
				x,
				regFile.get(r),
				() -> String.format("Expected register %s to be 0x%04x but was 0x%04x", r, x, regFile.get(r)));
	}

	@ParameterizedTest
	@MethodSource("all16BitsRegisters")
	void setToValueShouldNotChangeOtherRegisters(final Register16 r) {
		final short x = BitUtils.asShort(RNG.nextInt(1, 65_536));
		regFile.set(r, x);

		for (final Register16 other : ALL_16_BIT_REGISTERS) {
			if (r == other) {
				continue;
			}
			assertEquals(
					(short) 0x0000,
					regFile.get(other),
					() -> String.format("Expected register %s to be zero but was 0x%04x", other, regFile.get(other)));
		}
	}

	private static Stream<Arguments> all32BitsRegisters() {
		return Arrays.stream(ALL_32_BIT_REGISTERS).map(Arguments::of);
	}

	@ParameterizedTest
	@MethodSource("all32BitsRegisters")
	void initiallyAllZero(final Register32 r) {
		assertEquals(
				0x00000000,
				regFile.get(r),
				() -> String.format("Expected register %s to be initially zero but was 0x%08x", r, regFile.get(r)));
	}

	@ParameterizedTest
	@MethodSource("all32BitsRegisters")
	void setToValue(final Register32 r) {
		final int x = RNG.nextInt();
		regFile.set(r, x);
		assertEquals(
				x,
				regFile.get(r),
				() -> String.format("Expected register %s to be 0x%08x but was 0x%08x", r, x, regFile.get(r)));
	}

	@ParameterizedTest
	@MethodSource("all32BitsRegisters")
	void setToValueShouldNotChangeOtherRegisters(final Register32 r) {
		final int x = RNG.nextInt();
		regFile.set(r, x);

		for (final Register32 other : ALL_32_BIT_REGISTERS) {
			if (r == other) {
				continue;
			}
			assertEquals(
					0x00000000,
					regFile.get(other),
					() -> String.format("Expected register %s to be zero but was 0x%08x", other, regFile.get(other)));
		}
	}

	private static Stream<Arguments> all64BitsRegisters() {
		return Arrays.stream(ALL_64_BIT_REGISTERS).map(Arguments::of);
	}

	@ParameterizedTest
	@MethodSource("all64BitsRegisters")
	void initiallyAllZero(final Register64 r) {
		assertEquals(
				0x0000000000000000L,
				regFile.get(r),
				() -> String.format("Expected register %s to be initially zero but was 0x%016x", r, regFile.get(r)));
	}

	@ParameterizedTest
	@MethodSource("all64BitsRegisters")
	void setToValue(final Register64 r) {
		final long x = RNG.nextLong(1, Long.MAX_VALUE);
		regFile.set(r, x);
		assertEquals(
				x,
				regFile.get(r),
				() -> String.format("Expected register %s to be 0x%016x but was 0x%016x", r, x, regFile.get(r)));
	}

	@ParameterizedTest
	@MethodSource("all64BitsRegisters")
	void setToValueShouldNotChangeOtherRegisters(final Register64 r) {
		final long x = RNG.nextLong(1, Long.MAX_VALUE);
		regFile.set(r, x);

		for (final Register64 other : ALL_64_BIT_REGISTERS) {
			if (r == other) {
				continue;
			}
			assertEquals(
					0x0000000000000000L,
					regFile.get(other),
					() -> String.format(
							"Expected register %s to be initially zero but was 0x%016x", other, regFile.get(other)));
		}
	}

	private static Stream<Arguments> allSegmentRegisters() {
		return Arrays.stream(ALL_SEGMENT_REGISTERS).map(Arguments::of);
	}

	@ParameterizedTest
	@MethodSource("allSegmentRegisters")
	void initiallyAllZero(final SegmentRegister r) {
		assertEquals(
				(short) 0x0000,
				regFile.get(r),
				() -> String.format("Expected register %s to be initially zero but was 0x%04x", r, regFile.get(r)));
	}

	@ParameterizedTest
	@MethodSource("allSegmentRegisters")
	void setToValue(final SegmentRegister r) {
		final short x = BitUtils.asShort(RNG.nextInt(1, 65_536));
		regFile.set(r, x);
		assertEquals(
				x,
				regFile.get(r),
				() -> String.format("Expected register %s to be 0x%04x but was 0x%04x", r, x, regFile.get(r)));
	}

	@ParameterizedTest
	@MethodSource("allSegmentRegisters")
	void setToValueShouldNotChangeOtherRegisters(final SegmentRegister r) {
		final short x = BitUtils.asShort(RNG.nextInt(1, 65_536));
		regFile.set(r, x);

		for (final SegmentRegister other : ALL_SEGMENT_REGISTERS) {
			if (r == other) {
				continue;
			}
			assertEquals(
					(short) 0x0000,
					regFile.get(other),
					() -> String.format("Expected register %s to be zero but was 0x%04x", other, regFile.get(other)));
		}
	}

	private static Stream<Arguments> allRFlags() {
		return Arrays.stream(RFlags.values()).map(Arguments::of);
	}

	@ParameterizedTest
	@MethodSource("allRFlags")
	void initiallyAllFlagsAreNotSet(final RFlags f) {
		assertFalse(regFile.isSet(f), () -> String.format("Expected flag %s to be not set but wasn't", f));
	}

	@ParameterizedTest
	@MethodSource("allRFlags")
	void setToValueShouldNotChangeOtherFlags(final RFlags f) {
		regFile.set(f, true);

		for (final RFlags other : RFlags.values()) {
			if (f == other) {
				continue;
			}
			assertFalse(regFile.isSet(other), () -> String.format("Expected flag %s to be not set but wasn't", other));
		}
	}
}
