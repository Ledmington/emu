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
package com.ledmington.elf.section.note;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The type of property found inside the .note.gnu.property ELF section. A lot of values have been taken from <a
 * href="https://github.com/bminor/binutils-gdb/blob/2b001c799977a97304311df238fe33daa9b8fa7f/include/elf/common.h">the
 * GNU binutils source code</a>.
 */
@SuppressWarnings("PMD.ExcessivePublicCount")
public enum GnuPropertyType {

	/** Stack size. */
	GNU_PROPERTY_STACK_SIZE(1),

	/** No copy relocation on protected data symbol. */
	GNU_PROPERTY_NO_COPY_ON_PROTECTED(2),

	/** A 4-byte unsigned integer property: A bit is set if it is set in all relocatable inputs. */
	GNU_PROPERTY_UINT32_AND_LO(0xb0000000),
	/** End range of {@link GnuPropertyType#GNU_PROPERTY_UINT32_AND_LO}. */
	GNU_PROPERTY_UINT32_AND_HI(0xb0007fff),

	/** A 4-byte unsigned integer property: A bit is set if it is set in any relocatable inputs. */
	GNU_PROPERTY_UINT32_OR_LO(0xb0008000),
	/** End range of {@link GnuPropertyType#GNU_PROPERTY_UINT32_OR_LO}. */
	GNU_PROPERTY_UINT32_OR_HI(0xb000ffff),

	/** The needed properties by the object file. */
	GNU_PROPERTY_1_NEEDED(GNU_PROPERTY_UINT32_OR_LO.getCode()),

	/** Set if the object file requires canonical function pointers and cannot be used with copy relocation. */
	GNU_PROPERTY_1_NEEDED_INDIRECT_EXTERN_ACCESS(1),

	/** Processor-specific semantics, lo */
	GNU_PROPERTY_LOPROC(0xc0000000),
	/** Processor-specific semantics, hi */
	GNU_PROPERTY_HIPROC(0xdfffffff),
	/** Application-specific semantics, lo */
	GNU_PROPERTY_LOUSER(0xe0000000),
	/** Application-specific semantics, hi */
	GNU_PROPERTY_HIUSER(0xffffffff),

	/** A 4-byte unsigned integer property: A bit is set if it is set in all relocatable inputs. */
	GNU_PROPERTY_X86_UINT32_AND_LO(0xc0000002),
	/** End range of {@link GnuPropertyType#GNU_PROPERTY_X86_UINT32_AND_LO}. */
	GNU_PROPERTY_X86_UINT32_AND_HI(0xc0007fff),

	/** A 4-byte unsigned integer property: A bit is set if it is set in any relocatable inputs. */
	GNU_PROPERTY_X86_UINT32_OR_LO(0xc0008000),
	/** End range of {@link GnuPropertyType#GNU_PROPERTY_X86_UINT32_OR_LO}. */
	GNU_PROPERTY_X86_UINT32_OR_HI(0xc000ffff),

	/**
	 * A 4-byte unsigned integer property: A bit is set if it is set in any relocatable inputs and the property is
	 * present in all relocatable inputs.
	 */
	GNU_PROPERTY_X86_UINT32_OR_AND_LO(0xc0010000),
	/** End range of {@link GnuPropertyType#GNU_PROPERTY_X86_UINT32_OR_AND_LO}. */
	GNU_PROPERTY_X86_UINT32_OR_AND_HI(0xc0017fff),

	/** X86 processor-specific features used in a program. */
	GNU_PROPERTY_X86_FEATURE_1_AND(GNU_PROPERTY_X86_UINT32_AND_LO.getCode()),

	/** This bit indicates that this property contains a list of x86 ISAs needed for execution. */
	GNU_PROPERTY_X86_ISA_1_NEEDED(GNU_PROPERTY_X86_UINT32_OR_LO.getCode() + 2),
	/** This bit indicates that this property contains a list of x86 features needed for execution. */
	GNU_PROPERTY_X86_FEATURE_2_NEEDED(GNU_PROPERTY_X86_UINT32_OR_LO.getCode() + 1),

	/**
	 * This bit indicates that this property contains a list of x86 ISAs used by the program during execution but are
	 * not needed.
	 */
	GNU_PROPERTY_X86_ISA_1_USED(GNU_PROPERTY_X86_UINT32_OR_AND_LO.getCode() + 2),
	/**
	 * This bit indicates that this property contains a list of x86 features used by the program during execution but
	 * are not needed.
	 */
	GNU_PROPERTY_X86_FEATURE_2_USED(GNU_PROPERTY_X86_UINT32_OR_AND_LO.getCode() + 1),

	/**
	 * GNU_PROPERTY_X86_ISA_1_BASELINE: CMOV, CX8 (cmpxchg8b), FPU (fld), MMX, OSFXSR (fxsave), SCE (syscall), SSE and
	 * SSE2.
	 */
	GNU_PROPERTY_X86_ISA_1_BASELINE(1, "x86-64-baseline"),
	/**
	 * GNU_PROPERTY_X86_ISA_1_V2: GNU_PROPERTY_X86_ISA_1_BASELINE, CMPXCHG16B (cmpxchg16b), LAHF-SAHF (lahf), POPCNT
	 * (popcnt), SSE3, SSSE3, SSE4.1 and SSE4.2.
	 */
	GNU_PROPERTY_X86_ISA_1_V2(1 << 1, "x86-64-v2"),
	/** GNU_PROPERTY_X86_ISA_1_V3: GNU_PROPERTY_X86_ISA_1_V2, AVX, AVX2, BMI1, BMI2, F16C, FMA, LZCNT, MOVBE, XSAVE. */
	GNU_PROPERTY_X86_ISA_1_V3(1 << 2, "x86-64-v3"),
	/** GNU_PROPERTY_X86_ISA_1_V4: GNU_PROPERTY_X86_ISA_1_V3, AVX512F, AVX512BW, AVX512CD, AVX512DQ and AVX512VL. */
	GNU_PROPERTY_X86_ISA_1_V4(1 << 3, "x86-64-v4"),

	/** Uses IBT. */
	GNU_PROPERTY_X86_FEATURE_1_IBT(1, "IBT"),
	/** Uses SHSTK. */
	GNU_PROPERTY_X86_FEATURE_1_SHSTK(1 << 1, "SHSTK"),
	/** Uses LAM_U48. */
	GNU_PROPERTY_X86_FEATURE_1_LAM_U48(1 << 2, "LAM_U48"),
	/** Uses LAM_U57. */
	GNU_PROPERTY_X86_FEATURE_1_LAM_U57(1 << 3, "LAM_U57"),

	/** Uses x86. */
	GNU_PROPERTY_X86_FEATURE_2_X86(1, "X86"),
	/** Uses x87. */
	GNU_PROPERTY_X86_FEATURE_2_X87(1 << 1, "X87"),
	/** Uses MMX. */
	GNU_PROPERTY_X86_FEATURE_2_MMX(1 << 2, "MMX"),
	/** Uses XMM. */
	GNU_PROPERTY_X86_FEATURE_2_XMM(1 << 3, "XMM"),
	/** Uses YMM. */
	GNU_PROPERTY_X86_FEATURE_2_YMM(1 << 4, "YMM"),
	/** Uses ZMM. */
	GNU_PROPERTY_X86_FEATURE_2_ZMM(1 << 5, "ZMM"),
	/** Uses FXSR. */
	GNU_PROPERTY_X86_FEATURE_2_FXSR(1 << 6, "FXSR"),
	/** Uses XSAVE. */
	GNU_PROPERTY_X86_FEATURE_2_XSAVE(1 << 7, "XSAVE"),
	/** Uses XSAVEOPT. */
	GNU_PROPERTY_X86_FEATURE_2_XSAVEOPT(1 << 8, "XSAVEOPT"),
	/** Uses XSAVEC. */
	GNU_PROPERTY_X86_FEATURE_2_XSAVEC(1 << 9, "XSAVEC"),
	/** Uses TMM. */
	GNU_PROPERTY_X86_FEATURE_2_TMM(1 << 10, "TMM"),
	/** Uses MASK. */
	GNU_PROPERTY_X86_FEATURE_2_MASK(1 << 11, "MASK");

	private final int code;
	private final String description;

	GnuPropertyType(final int code, final String description) {
		this.code = code;
		Objects.requireNonNull(description);
		if (description.isBlank()) {
			throw new IllegalArgumentException("Empty description.");
		}
		this.description = description;
	}

	GnuPropertyType(final int code) {
		this(code, "UNKNOWN");
	}

	/**
	 * Returns the 32-bit code of this GNU property type.
	 *
	 * @return The 32-bit code of this GNU property type.
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Returns a String with the description of this type as GNU's <code>readelf</code> would print it.
	 *
	 * @return The description of this type.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the property type corresponding to the given 32-bit code.
	 *
	 * @param code The 32-bit code of the GNU property.
	 * @return The proper GNU property.
	 */
	public static GnuPropertyType fromCode(final int code) {
		if (code == GNU_PROPERTY_STACK_SIZE.getCode()) {
			return GNU_PROPERTY_STACK_SIZE;
		}
		if (code == GNU_PROPERTY_NO_COPY_ON_PROTECTED.getCode()) {
			return GNU_PROPERTY_NO_COPY_ON_PROTECTED;
		}
		if (code == GNU_PROPERTY_X86_ISA_1_USED.getCode()) {
			return GNU_PROPERTY_X86_ISA_1_USED;
		}
		if (code == GNU_PROPERTY_X86_ISA_1_NEEDED.getCode()) {
			return GNU_PROPERTY_X86_ISA_1_NEEDED;
		}
		if (code == GNU_PROPERTY_X86_FEATURE_1_AND.getCode()) {
			return GNU_PROPERTY_X86_FEATURE_1_AND;
		}
		if (code == GNU_PROPERTY_X86_FEATURE_2_USED.getCode()) {
			return GNU_PROPERTY_X86_FEATURE_2_USED;
		}
		if (code == GNU_PROPERTY_X86_FEATURE_2_NEEDED.getCode()) {
			return GNU_PROPERTY_X86_FEATURE_2_NEEDED;
		}
		throw new IllegalArgumentException(String.format("Unknown GNU property type code %d (0x%08x)", code, code));
	}

	/**
	 * Decodes the given 32-bit code assuming it represents an x86 ISA GNU property.
	 *
	 * @param code The x86 ISA GNU property.
	 * @return A List of properties regarding the x86 ISA features used.
	 */
	public static List<GnuPropertyType> decodeX86ISA(final int code) {
		final List<GnuPropertyType> codes = new ArrayList<>();
		int bitmask = code;
		while (bitmask != 0) {
			final int bit = bitmask & (-bitmask);
			bitmask &= ~bit;
			if (bit == GNU_PROPERTY_X86_ISA_1_BASELINE.getCode()) {
				codes.add(GNU_PROPERTY_X86_ISA_1_BASELINE);
			} else if (bit == GNU_PROPERTY_X86_ISA_1_V2.getCode()) {
				codes.add(GNU_PROPERTY_X86_ISA_1_V2);
			} else if (bit == GNU_PROPERTY_X86_ISA_1_V3.getCode()) {
				codes.add(GNU_PROPERTY_X86_ISA_1_V3);
			} else if (bit == GNU_PROPERTY_X86_ISA_1_V4.getCode()) {
				codes.add(GNU_PROPERTY_X86_ISA_1_V4);
			} else {
				throw new IllegalArgumentException(
						String.format("Unknown bit %d (0x%08x) in code %d (0x%08x).", bit, bit, code, code));
			}
		}
		return Collections.unmodifiableList(codes);
	}

	/**
	 * Decodes the given 32-bit code assuming it represents an x86 ISA "feature 1" GNU property.
	 *
	 * @param code The x86 ISA "feature 1" GNU property.
	 * @return A List of properties regarding the x86 ISA features used.
	 */
	public static List<GnuPropertyType> decodeX86ISAFeature1(final int code) {
		final List<GnuPropertyType> codes = new ArrayList<>();
		int bitmask = code;
		while (bitmask != 0) {
			final int bit = bitmask & (-bitmask);
			bitmask &= ~bit;
			if (bit == GNU_PROPERTY_X86_FEATURE_1_IBT.getCode()) {
				codes.add(GNU_PROPERTY_X86_FEATURE_1_IBT);
			} else if (bit == GNU_PROPERTY_X86_FEATURE_1_SHSTK.getCode()) {
				codes.add(GNU_PROPERTY_X86_FEATURE_1_SHSTK);
			} else if (bit == GNU_PROPERTY_X86_FEATURE_1_LAM_U48.getCode()) {
				codes.add(GNU_PROPERTY_X86_FEATURE_1_LAM_U48);
			} else if (bit == GNU_PROPERTY_X86_FEATURE_1_LAM_U57.getCode()) {
				codes.add(GNU_PROPERTY_X86_FEATURE_1_LAM_U57);
			} else {
				throw new IllegalArgumentException(
						String.format("Unknown bit %d (0x%08x) in code %d (0x%08x).", bit, bit, code, code));
			}
		}
		return Collections.unmodifiableList(codes);
	}

	/**
	 * Decodes the given 32-bit code assuming it represents an x86 ISA "feature 2" GNU property.
	 *
	 * @param code The x86 ISA "feature 2" GNU property.
	 * @return A List of properties regarding the x86 ISA features used.
	 */
	public static List<GnuPropertyType> decodeX86ISAFeature2(final int code) {
		final List<GnuPropertyType> codes = new ArrayList<>();
		int bitmask = code;
		while (bitmask != 0) {
			final int bit = bitmask & (-bitmask);
			bitmask &= ~bit;
			if (bit == GNU_PROPERTY_X86_FEATURE_2_X86.getCode()) {
				codes.add(GNU_PROPERTY_X86_FEATURE_2_X86);
			} else if (bit == GNU_PROPERTY_X86_FEATURE_2_X87.getCode()) {
				codes.add(GNU_PROPERTY_X86_FEATURE_2_X87);
			} else if (bit == GNU_PROPERTY_X86_FEATURE_2_MMX.getCode()) {
				codes.add(GNU_PROPERTY_X86_FEATURE_2_MMX);
			} else if (bit == GNU_PROPERTY_X86_FEATURE_2_XMM.getCode()) {
				codes.add(GNU_PROPERTY_X86_FEATURE_2_XMM);
			} else if (bit == GNU_PROPERTY_X86_FEATURE_2_YMM.getCode()) {
				codes.add(GNU_PROPERTY_X86_FEATURE_2_YMM);
			} else if (bit == GNU_PROPERTY_X86_FEATURE_2_ZMM.getCode()) {
				codes.add(GNU_PROPERTY_X86_FEATURE_2_ZMM);
			} else if (bit == GNU_PROPERTY_X86_FEATURE_2_TMM.getCode()) {
				codes.add(GNU_PROPERTY_X86_FEATURE_2_TMM);
			} else if (bit == GNU_PROPERTY_X86_FEATURE_2_MASK.getCode()) {
				codes.add(GNU_PROPERTY_X86_FEATURE_2_MASK);
			} else if (bit == GNU_PROPERTY_X86_FEATURE_2_FXSR.getCode()) {
				codes.add(GNU_PROPERTY_X86_FEATURE_2_FXSR);
			} else if (bit == GNU_PROPERTY_X86_FEATURE_2_XSAVE.getCode()) {
				codes.add(GNU_PROPERTY_X86_FEATURE_2_XSAVE);
			} else if (bit == GNU_PROPERTY_X86_FEATURE_2_XSAVEOPT.getCode()) {
				codes.add(GNU_PROPERTY_X86_FEATURE_2_XSAVEOPT);
			} else if (bit == GNU_PROPERTY_X86_FEATURE_2_XSAVEC.getCode()) {
				codes.add(GNU_PROPERTY_X86_FEATURE_2_XSAVEC);
			} else {
				throw new IllegalArgumentException(
						String.format("Unknown bit %d (0x%08x) in code %d (0x%08x).", bit, bit, code, code));
			}
		}
		return Collections.unmodifiableList(codes);
	}
}
