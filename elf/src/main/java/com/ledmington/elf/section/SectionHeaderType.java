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
package com.ledmington.elf.section;

import java.util.HashMap;
import java.util.Map;

/** Type of an ELF Section. */
public enum SectionHeaderType {

	/**
	 * This value marks the section header as inactive; it does not have an associated section. Other members of the
	 * section header have undefined values.
	 */
	SHT_NULL(0x00000000, "NULL", "Entry unused"),

	/**
	 * This section holds information defined by the program, whose format and meaning are determined solely by the
	 * program.
	 */
	SHT_PROGBITS(0x00000001, "PROGBITS", "Program data"),

	/** This section holds a symbol table. */
	SHT_SYMTAB(0x00000002, "SYMTAB", "Symbol table"),

	/** This section holds a string table. */
	SHT_STRTAB(0x00000003, "STRTAB", "String table"),

	/**
	 * This section holds relocation entries with explicit addends, such as type ElfXX_Rela. An object file may have
	 * multiple relocation sections.
	 */
	SHT_RELA(0x00000004, "RELA", "Relocation entries with addends"),

	/** This section holds a symbol hash table. */
	SHT_HASH(0x00000005, "HASH", "Symbol Hash table"),

	/** This section holds information for dynamic linking. */
	SHT_DYNAMIC(0x00000006, "DYNAMIC", "Dynamic linking info"),

	/** This section holds information that marks the file in some way. */
	SHT_NOTE(0x00000007, "NOTE", "Notes"),

	/**
	 * A section of this type occupies no space in the file but otherwise resembles SHT_PROGBITS. Although this section
	 * contains no bytes, the sh_offset member contains the conceptual file offset.
	 */
	SHT_NOBITS(0x00000008, "NOBITS", "Program space with no data (bss)"),

	/**
	 * This section holds relocation entries without explicit addends, such as type ElfXX_Rel. An object file may have
	 * multiple relocation sections.
	 */
	SHT_REL(0x00000009, "REL", "Relocation entries (no addends)"),

	/** This section type is reserved but has unspecified semantics. */
	SHT_SHLIB(0x0000000a, "SHLIB", "Reserved"),

	/** This section holds a symbol table. */
	SHT_DYNSYM(0x0000000b, "DYNSYM", "Dynamic linker symbol table"),

	/**
	 * Identifies a section containing an array of pointers to initialization functions. Each pointer in the array is
	 * taken as a parameterless procedure with a void return.
	 */
	SHT_INIT_ARRAY(0x0000000e, "INIT_ARRAY", "Array of constructors"),

	/**
	 * Identifies a section containing an array of pointers to termination functions. Each pointer in the array is taken
	 * as a parameterless procedure with a void return.
	 */
	SHT_FINI_ARRAY(0x0000000f, "FINI_ARRAY", "Array of destructors"),

	/**
	 * Identifies a section containing an array of pointers to functions that are invoked before all other
	 * initialization functions. Each pointer in the array is taken as a parameterless procedure with a void return.
	 */
	SHT_PREINIT_ARRAY(0x00000010, "PREINIT_ARRAY", "Array of pre-constructors"),

	/**
	 * Identifies a section group. A section group identifies a set of related sections that must be treated as a unit
	 * by the link-editor. Sections of type SHT_GROUP may appear only in relocatable objects.
	 */
	SHT_GROUP(0x00000011, "GROUP", "Section group"),

	/**
	 * Identifies a section containing extended section indexes, that is associated with a symbol table. If any section
	 * header indexes referenced by a symbol table, contain the escape value SHN_XINDEX, an associated SHT_SYMTAB_SHNDX
	 * is required.
	 *
	 * <p>The SHT_SYMTAB_SHNDX section is an array of ElfXX_Word values. There is one value for every entry in the
	 * associated symbol table entry. The values represent the section header indexes against which the symbol table
	 * entries are defined. Only if corresponding symbol table entry's st_shndx field contains the escape value
	 * SHN_XINDEX will the matching Elf32_Word hold the actual section header index. Otherwise, the entry must be
	 * SHN_UNDEF (0).
	 */
	SHT_SYMTAB_SHNDX(0x00000012, "SYMTAB_SHNDX", "Extended section indices"),

	/** Number of defined types. */
	SHT_NUM(0x00000013, "NUM", "Number of defined types"),

	/** GNU-style hash table (bloom filter?). */
	SHT_GNU_HASH(0x6ffffff6, "GNU_HASH", "GNU Hash table"),

	/** Version definition section. */
	SHT_GNU_verdef(0x6ffffffd, "VERDEF", "GNU version symbol definitions"),

	/** Version needs section. */
	SHT_GNU_verneed(0x6ffffffe, "VERNEED", "GNU version symbol needed elements"),

	/** Version symbol table. */
	SHT_GNU_versym(0x6fffffff, "VERSYM", "GNU version symbol table"),

	/** x86_64 specific unwind info. */
	SHT_X86_64_UNWIND(0x70000001, "X86_64_UNWIND", "x86_64 unwind info");

	private static final Map<Integer, SectionHeaderType> codeToType = new HashMap<>();

	static {
		for (final SectionHeaderType type : values()) {
			codeToType.put(type.getCode(), type);
		}
	}

	private static boolean isOSSpecific(final int code) {
		return (code & 0xf0000000) == 0x60000000;
	}

	private static boolean isCPUSpecific(final int code) {
		return (code & 0xf0000000) == 0x70000000;
	}

	private static boolean isApplicationSpecific(final int code) {
		return (code & 0x80000000) == 0x80000000;
	}

	/**
	 * Checks whether the given 32-bit code corresponds to a valid type of a section header.
	 *
	 * @param code The 32-bit value to be checked.
	 * @return True if the code is valid, false otherwise.
	 */
	public static boolean isValid(final int code) {
		return codeToType.containsKey(code);
	}

	/**
	 * Returns the SectionHeaderType object corresponding to the given 32-bit code.
	 *
	 * @param code The 32-bit value of the type.
	 * @return A non-null type object.
	 */
	public static SectionHeaderType fromCode(final int code) {
		if (!codeToType.containsKey(code)) {
			if (isOSSpecific(code)) {
				throw new IllegalArgumentException(
						String.format("Unknown OS-specific SHT entry identifier: 0x%08x", code));
			}
			if (isCPUSpecific(code)) {
				throw new IllegalArgumentException(
						String.format("Unknown CPU-specific SHT entry identifier: 0x%08x", code));
			}
			if (isApplicationSpecific(code)) {
				throw new IllegalArgumentException(
						String.format("Unknown application-specific SHT entry identifier: 0x%08x", code));
			}
			throw new IllegalArgumentException(String.format("Unknown SHT entry identifier: 0x%08x", code));
		}
		return codeToType.get(code);
	}

	private final int code;
	private final String name;
	private final String description;

	SectionHeaderType(final int code, final String name, final String description) {
		this.code = code;
		this.name = name;
		this.description = description;
	}

	/**
	 * Returns the 32-bit code of this type object.
	 *
	 * @return The code of this type object.
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Returns the name of this type object without the "SHT_" prefix.
	 *
	 * @return The name of this type object.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns a brief description of this type object.
	 *
	 * @return A brief description of this type object.
	 */
	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return "SectionHeaderType(code=" + code + ";name=" + name + ";description=" + description + ")";
	}
}
