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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The tag of an entry of the Dynamic section (.dyn).
 *
 * <p>Useful reference <a href=
 * "https://docs.oracle.com/cd/E19120-01/open.solaris/819-0690/chapter6-14428/index.html">here</a>.
 */
@SuppressWarnings("PMD.ExcessivePublicCount")
public enum DynamicTableEntryTag {

	/** The last entry of the dynamic section has this tag. */
	DT_NULL(0L, "NULL"),

	/**
	 * This element holds the string table offset of a null-terminated string, giving the name of a needed library. The
	 * offset is an index into the table recorded in the DT_STRTAB entry. The dynamic array may contain multiple entries
	 * with this type. These entries' relative order is significant, though their relation to entries of other types is
	 * not.
	 */
	DT_NEEDED(1L, "NEEDED"),

	/**
	 * This element holds the total size, in bytes, of the relocation entries associated with the procedure linkage
	 * table. If an entry of type DT_JMPREL is present, a DT_PLTRELSZ must accompany it.
	 */
	DT_PLTRELSZ(2L, "PLTRELSZ"),

	/** This element holds an address associated with the procedure linkage table and/or the global offset table. */
	DT_PLTGOT(3L, "PLTGOT"),

	/**
	 * This element holds the address of the symbol hash table. This hash table refers to the symbol table referenced by
	 * the DT_SYMTAB element.
	 */
	DT_HASH(4L, "HASH"),

	/**
	 * This element holds the address of the string table. Symbol names, library names, and other strings reside in this
	 * table.
	 */
	DT_STRTAB(5L, "STRTAB"),

	/** This element holds the address of the symbol table. */
	DT_SYMTAB(6L, "SYMTAB"),

	/**
	 * This element holds the address of a relocation table. Entries in the table have explicit addends. An object file
	 * may have multiple relocation sections. When building the relocation table for an executable or shared object
	 * file, the link editor catenates those sections to form a single table. Although the sections remain independent
	 * in the object file, the dynamic linker sees a single table. When the dynamic linker creates the process image for
	 * an executable file or adds a shared object to the process image, it reads the relocation table and performs the
	 * associated actions. If this element is present, the dynamic structure must also have DT_RELASZ and DT_RELAENT
	 * elements. When relocation is "mandatory" for a file, either DT_RELA or DT_REL may occur (both are permitted but
	 * not required).
	 */
	DT_RELA(7L, "RELA"),

	/** This element holds the total size, in bytes, of the DT_RELA relocation table. */
	DT_RELASZ(8L, "RELASZ"),

	/** This element holds the size, in bytes, of the DT_RELA relocation entry. */
	DT_RELAENT(9L, "RELAENT"),

	/** This element holds the size, in bytes, of the string table. */
	DT_STRSZ(10L, "STRSZ"),

	/** This element holds the size, in bytes, of a symbol table entry. */
	DT_SYMENT(11L, "SYMENT"),

	/** This element holds the address of the initialization function. */
	DT_INIT(12L, "INIT"),

	/** This element holds the address of the termination function. */
	DT_FINI(13L, "FINI"),

	/**
	 * This element holds the string table offset of a null-terminated string, giving the name of the shared object. The
	 * offset is an index into the table recorded in the DT_STRTAB entry.
	 */
	DT_SONAME(14L, "SONAME"),

	/**
	 * This element holds the string table offset of a null-terminated search library search path string. The offset is
	 * an index into the table recorded in the DT_STRTAB entry.
	 */
	DT_RPATH(15L, "RPATH"),

	/**
	 * This element's presence in a shared object library alters the dynamic linker's symbol resolution algorithm for
	 * references within the library. Instead of starting a symbol search with the executable file, the dynamic linker
	 * starts from the shared object itself. If the shared object fails to supply the referenced symbol, the dynamic
	 * linker then searches the executable file and other shared objects as usual.
	 */
	DT_SYMBOLIC(16L, "SYMBOLIC"),

	/**
	 * This element is similar to DT_RELA, except its table has implicit addends. If this element is present, the
	 * dynamic structure must also have DT_RELSZ and DT_RELENT elements.
	 */
	DT_REL(17L, "REL"),

	/** This element holds the total size, in bytes, of the DT_REL relocation table. */
	DT_RELSZ(18L, "RELSZ"),

	/** This element holds the size, in bytes, of the DT_REL relocation entry */
	DT_RELENT(19L, "RELENT"),

	/**
	 * This member specifies the type of relocation entry to which the procedure linkage table refers. The d_val member
	 * holds DT_REL or DT_RELA, as appropriate. All relocations in a procedure linkage table must use the same
	 * relocation.
	 */
	DT_PLTREL(20L, "PLTREL"),

	/** This member is used for debugging. */
	DT_DEBUG(21L, "DEBUG"),

	/**
	 * This member's absence signifies that no relocation entry should cause a modification to a non-writable segment,
	 * as specified by the segment permissions in the program header table. If this member is present, one or more
	 * relocation entries might request modifications to a non-writable segment, and the dynamic linker can prepare
	 * accordingly.
	 */
	DT_TEXTREL(22L, "TEXTREL"),

	/**
	 * If present, this entry's d_ptr member holds the address of relocation entries associated solely with the
	 * procedure linkage table. Separating these relocation entries lets the dynamic linker ignore them during process
	 * initialization, if lazy binding is enabled. If this entry is present, the related entries of types DT_PLTRELSZ
	 * and DT_PLTREL must also be present.
	 */
	DT_JMPREL(23L, "JMPREL"),

	/**
	 * If present in a shared object or executable, this entry instructs the dynamic linker to process all relocations
	 * for the object containing this entry before transferring control to the program. The presence of this entry takes
	 * precedence over a directive to use lazy binding for this object when specified through the environment or via
	 * dlopen(BA_LIB).
	 */
	DT_BIND_NOW(24L, "BIND_NOW"),

	/**
	 * The address of an array of pointers to initialization functions. This element requires that a DT_INIT_ARRAYSZ
	 * element also be present.
	 *
	 * <p>Note: this is generally stored in a .init_array section.
	 */
	DT_INIT_ARRAY(25L, "INIT_ARRAY"),

	/**
	 * The address of an array of pointers to termination functions. This element requires that a DT_FINI_ARRAYSZ
	 * element also be present.
	 *
	 * <p>Note: this is generally stored in a .fini_array section.
	 */
	DT_FINI_ARRAY(26L, "FINI_ARRAY"),

	/** The total size, in bytes, of the DT_INIT_ARRAY array. */
	DT_INIT_ARRAYSZ(27L, "INIT_ARRAYSZ"),

	/** The total size, in bytes, of the DT_FINI_ARRAY array. */
	DT_FINI_ARRAYSZ(28L, "FINI_ARRAYSZ"),

	/** The DT_STRTAB string table offset of a null-terminated library search path string. */
	DT_RUNPATH(29L, "RUNPATH"),

	/**
	 * Flag values specific to this object.
	 *
	 * <p>See <a href=
	 * "https://docs.oracle.com/cd/E19120-01/open.solaris/819-0690/6n33n7fe2/index.html#chapter7-tbl-5">here</a>.
	 */
	DT_FLAGS(30L, "FLAGS"),

	/**
	 * The address of an array of pointers to pre-initialization functions. This element requires that a
	 * DT_PREINIT_ARRAYSZ element also be present. This array is processed only in an executable file. This array is
	 * ignored if contained in a shared object.
	 */
	DT_PREINIT_ARRAY(32L, "PREINIT_ARRAY"),

	/** The total size, in bytes, of the DT_PREINIT_ARRAY array. */
	DT_PREINIT_ARRAYSZ(33L, "PREINIT_ARRAYSZ"),

	/** Holds the address of .gnu.hash. */
	DT_GNU_HASH(0x000000006ffffef5L, "GNU_HASH"),

	/** Address of the table provided by the .gnu.version section. */
	DT_VERSYM(0x000000006ffffff0L, "VERSYM"),

	/**
	 * Indicates the RELATIVE relocation count, which is produced from the concatenation of all Elf32_Rela, or
	 * Elf64_Rela relocations.
	 */
	DT_RELACOUNT(0x000000006ffffff9L, "RELACOUNT"),

	/**
	 * Indicates the RELATIVE relocation count, which is produced from the concatenation of all Elf32_Rel relocations.
	 */
	DT_RELCOUNT(0x000000006ffffffaL, "RELCOUNT"),

	/**
	 * Flag values specific to this object.
	 *
	 * <p>See <a href=
	 * "https://docs.oracle.com/cd/E19120-01/open.solaris/819-0690/6n33n7fe2/index.html#chapter6-tbl-53">here</a>.
	 */
	DT_FLAGS_1(0x000000006ffffffbL, "FLAGS_1"),

	/**
	 * The address of the version definition table. Elements within this table contain indexes into the string table
	 * DT_STRTAB. This element requires that the DT_VERDEFNUM element also be present.
	 */
	DT_VERDEF(0x000000006ffffffcL, "VERDEF"),

	/** The number of entries in the DT_VERDEF table. */
	DT_VERDEFNUM(0x000000006ffffffdL, "VERDEFNUM"),

	/**
	 * The address of the version dependency table. Elements within this table contain indexes into the string table
	 * DT_STRTAB. This element requires that the DT_VERNEEDNUM element also be present.
	 */
	DT_VERNEED(0x000000006ffffffeL, "VERNEED"),

	/** The number of entries in the DT_VERNEEDNUM table. */
	DT_VERNEEDNUM(0x000000006fffffffL, "VERNEEDNUM");

	private static final Map<Long, DynamicTableEntryTag> codeToTag = new ConcurrentHashMap<>();

	private final long code;
	private final String name;

	static {
		for (final DynamicTableEntryTag dtet : values()) {
			codeToTag.put(dtet.getCode(), dtet);
		}
	}

	private static boolean isOSSpecific(final long code) {
		return (code & 0x00000000f0000000L) == 0x0000000060000000L;
	}

	private static boolean isCPUSpecific(final long code) {
		return (code & 0x00000000f0000000L) == 0x0000000070000000L;
	}

	/**
	 * Returns the Dynamic table tag corresponding to the given code.
	 *
	 * @param code The 64-bit code of a Dynamic table tag.
	 * @return A Dynamic table tag object.
	 */
	public static DynamicTableEntryTag fromCode(final long code) {
		if (!codeToTag.containsKey(code)) {
			if (isOSSpecific(code)) {
				throw new IllegalArgumentException(
						String.format("Unknown OS-specific dynamic table entry tag identifier: 0x%016x", code));
			}
			if (isCPUSpecific(code)) {
				throw new IllegalArgumentException(
						String.format("Unknown CPU-specific dynamic table entry tag identifier: 0x%016x", code));
			}
			throw new IllegalArgumentException(
					String.format("Unknown dynamic table entry tag identifier: 0x%016x", code));
		}
		return codeToTag.get(code);
	}

	DynamicTableEntryTag(final long code, final String name) {
		this.code = code;
		this.name = Objects.requireNonNull(name);
	}

	/**
	 * Return the code of this Dynamic table tag object.
	 *
	 * @return The 64-bit code.
	 */
	public long getCode() {
		return code;
	}

	/**
	 * Returns the name of this Dynamic table tag object.
	 *
	 * @return The name.
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "DynamicTableEntryTag(code=" + code + ";name=" + name + ')';
	}
}
