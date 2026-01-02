/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2026 Filippo Barbari <filippo.barbari@gmail.com>
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
package com.ledmington.elf.section.rel;

import com.ledmington.elf.ISA;

/**
 * The type of an entry in a relaction section with addends. Since the types are processor-specific, here are some
 * useful references:
 *
 * <ul>
 *   <li><a href= "https://gist.github.com/DtxdF/e6d940271e0efca7e0e2977723aec360#relocation">X86 and X86_64</a>
 *   <li><a href= "https://docs.oracle.com/cd/E19120-01/open.solaris/819-0690/chapter7-2/index.html">AMD64</a>
 *   <li><a href= "https://github.com/ARM-software/abi-aa/blob/main/aaelf64/aaelf64.rst#relocation">AARCH64</a>
 *   <li><a href= "https://docs.oracle.com/cd/E19683-01/816-7529/chapter6-62988/index.html">SPARC</a>
 * </ul>
 */
public enum RelocationAddendEntryType {
	// AMD x86-64 relocation types.

	/** No reloc. */
	R_X86_64_NONE(0),
	/** Direct 64 bit */
	R_X86_64_64(1),
	/** PC relative 32-bit signed */
	R_X86_64_PC32(2),
	/** 32 bit GOT entry */
	R_X86_64_GOT32(3),
	/** 32-bit PLT address */
	R_X86_64_PLT32(4),
	/** Copy symbol at runtime */
	R_X86_64_COPY(5),
	/** Create GOT entry */
	R_X86_64_GLOB_DAT(6),
	/** Create PLT entry */
	R_X86_64_JUMP_SLOT(7),
	/** Adjust by program base */
	R_X86_64_RELATIVE(8),
	/** 32-bit signed PC relative offset to GOT */
	R_X86_64_GOTPCREL(9),
	/** Direct 32 bit zero extended */
	R_X86_64_32(10),
	/** Direct 32-bit sign extended */
	R_X86_64_32S(11),
	/** Direct 16 bit zero extended */
	R_X86_64_16(12),
	/** 16 bit sign extended pc relative */
	R_X86_64_PC16(13),
	/** Direct 8 bit sign extended */
	R_X86_64_8(14),
	/** 8 bit sign extended pc relative */
	R_X86_64_PC8(15),
	/** ID of module containing symbol */
	R_X86_64_DTPMOD64(16),
	/** Offset in module's TLS block */
	R_X86_64_DTPOFF64(17),
	/** Offset in initial TLS block */
	R_X86_64_TPOFF64(18),
	/** 32-bit signed PC relative offset to two GOT entries for GD symbol */
	R_X86_64_TLSGD(19),
	/** 32-bit signed PC relative offset to two GOT entries for LD symbol */
	R_X86_64_TLSLD(20),
	/** Offset in TLS block */
	R_X86_64_DTPOFF32(21),
	/** 32-bit signed PC relative offset to GOT entry for IE symbol */
	R_X86_64_GOTTPOFF(22),
	/** Offset in initial TLS block */
	R_X86_64_TPOFF32(23),
	/** PC relative 64 bit */
	R_X86_64_PC64(24),
	/** 64 bit offset to GOT */
	R_X86_64_GOTOFF64(25),
	/** 32-bit signed pc relative offset to GOT */
	R_X86_64_GOTPC32(26),
	/** 64-bit GOT entry offset */
	R_X86_64_GOT64(27),
	/** 64-bit PC relative offset to GOT entry */
	R_X86_64_GOTPCREL64(28),
	/** 64-bit PC relative offset to GOT */
	R_X86_64_GOTPC64(29),
	/** like GOT64, says PLT entry needed */
	R_X86_64_GOTPLT64(30),
	/** 64-bit GOT relative offset to PLT entry */
	R_X86_64_PLTOFF64(31),
	/** Size of symbol plus 32-bit addend */
	R_X86_64_SIZE32(32),
	/** Size of symbol plus 64-bit addend */
	R_X86_64_SIZE64(33),
	/** GOT offset for TLS descriptor. */
	R_X86_64_GOTPC32_TLSDESC(34),
	/** Marker for call through TLS descriptor. */
	R_X86_64_TLSDESC_CALL(35),
	/** TLS descriptor. */
	R_X86_64_TLSDESC(36),
	/** Adjust indirectly by program base */
	R_X86_64_IRELATIVE(37),
	/** 64-bit adjust by program base */
	R_X86_64_RELATIVE64(38),
	// 39 Reserved was R_X86_64_PC32_BND
	// 40 Reserved was R_X86_64_PLT32_BND
	/** Load from 32-bit signed pc relative offset to GOT entry without REX prefix, relaxable. */
	R_X86_64_GOTPCRELX(41),
	/** Load from 32-bit signed pc relative offset to GOT entry with REX prefix, relaxable. */
	R_X86_64_REX_GOTPCRELX(42),
	/** Unknown/undefined. */
	R_X86_64_NUM(43);

	private final int code;

	/**
	 * Returns the type of relocation entry corresponding to the given ISA and code combination.
	 *
	 * @param isa The ISA the code is referring to.
	 * @param code The actual 32-bit code of the type.
	 * @return The proper type of the entry.
	 */
	public static RelocationAddendEntryType fromCode(final ISA isa, final int code) {
		if (isa == ISA.AMD_X86_64) {
			for (final RelocationAddendEntryType type : values()) {
				if (type.name().startsWith("R_X86_64_") && type.getCode() == code) {
					return type;
				}
			}
			throw new IllegalArgumentException(String.format("Unknown value %d (0x%08x) for ISA %s.", code, code, isa));
		} else {
			throw new IllegalArgumentException(String.format("Unknown ISA: %s.", isa));
		}
	}

	RelocationAddendEntryType(final int code) {
		this.code = code;
	}

	/**
	 * Returns the 32-bit code of this entry type.
	 *
	 * @return The 32-bit code of this entry type.
	 */
	public int getCode() {
		return code;
	}
}
