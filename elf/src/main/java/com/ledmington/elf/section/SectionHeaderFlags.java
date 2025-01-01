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

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/** Flags for an ELF section's header. */
public enum SectionHeaderFlags {

	/** The section contains data that should be writable during process execution. */
	SHF_WRITE(0x00000000000001L, "WRITE", "Writable", 'W'),

	/**
	 * The section occupies memory during process execution. Some control sections do not reside in the memory image of
	 * an object file; this attribute is off for those sections.
	 */
	SHF_ALLOC(0x00000000000002L, "ALLOC", "Occupies memory during execution", 'A'),

	/** The section contains executable machine instructions. */
	SHT_EXECINSTR(0x00000000000004L, "EXEC", "Executable", 'X'),

	/**
	 * Identifies a section containing data that can be merged to eliminate duplication. Unless the SHF_STRINGS flag is
	 * also set, the data elements in the section are of a uniform size. The size of each element is specified in the
	 * section header's sh_entsize field. If the SHF_STRINGS flag is also set, the data elements consist of
	 * null-terminated character strings. The size of each character is specified in the section header's sh_entsize
	 * field.
	 */
	SHF_MERGE(0x00000000000010L, "MERGE", "Might be merged", 'M'),

	/**
	 * Identifies a section that consists of null-terminated character strings. The size of each character is specified
	 * in the section header's sh_entsize field.
	 */
	SHF_STRINGS(0x00000000000020L, "STRINGS", "Contains null-terminated strings", 'S'),

	/** This section header's sh_info field holds a section header table index. */
	SHF_INFO_LINK(0x00000000000040L, "INFO LINK", "'sh_info' contains SHT index", 'I'),

	/**
	 * This section adds special ordering requirements to the link-editor. The requirements apply if the sh_link field
	 * of this section's header references another section, the linked-to section. If this section is combined with
	 * other sections in the output file, the section appears in the same relative order with respect to those sections.
	 * Similarly the linked-to section appears with respect to sections the linked-to section is combined with. The
	 * linked-to section must be unordered, and cannot in turn specify SHF_LINK_ORDER or SHF_ORDERED.
	 *
	 * <p>The special sh_link values SHN_BEFORE and SHN_AFTER imply that the sorted section is to precede or follow,
	 * respectively, all other sections in the set being ordered. Input file link-line order is preserved if multiple
	 * sections in an ordered set have one of these special values.
	 *
	 * <p>A typical use of this flag is to build a table that references text or data sections in address order.
	 *
	 * <p>In the absence of the sh_link ordering information, sections from a single input file combined within one
	 * section of the output file are contiguous. These section have the same relative ordering as the sections did in
	 * the input file. The contributions from multiple input files appear in link-line order.
	 */
	SHF_LINK_ORDER(0x00000000000080L, "LINK ORDER", "Preserve order after combining", 'L'),

	/**
	 * This section requires special OS-specific processing beyond the standard linking rules to avoid incorrect
	 * behavior. If this section has either an sh_type value or contains sh_flags bits in the OS-specific ranges for
	 * those fields, and the link-editor does not recognize these values, then the object file containing this section
	 * is rejected with an error.
	 */
	SHF_OS_NONCONFORMING(0x00000000000100L, "OS NONCONFORMING", "Non-standard OS specific handling required", 'O'),

	/**
	 * This section is a member, perhaps the only member, of a section group. The section must be referenced by a
	 * section of type SHT_GROUP. The SHF_GROUP flag can be set only for sections that are contained in relocatable
	 * objects.
	 */
	SHF_GROUP(0x00000000000200L, "GROUP", "Section is member of a group", 'G'),

	/** This section holds thread-local storage. Each thread within a process has a distinct instance of this data. */
	SHF_TLS(0x00000000000400L, "TLS", "Section hold thread-local data", 'T'),

	SHF_GNU_RETAIN(0x0000000000200000L, "GNU", "GNU Retain", 'R'),

	/**
	 * SHF_ORDERED is an older version of the functionality provided by SHF_LINK_ORDER, and has been superseded by
	 * SHF_LINK_ORDER. SHF_ORDERED offers two distinct and separate abilities. First, an output section can be
	 * specified, and second, special ordering requirements are required from the link-editor.
	 *
	 * <p>The sh_link field of an SHF_ORDERED section forms a linked list of sections. This list is terminated by a
	 * final section with a sh_link that points at itself. All sections in this list are assigned to the output section
	 * with the name of the final section in the list.
	 *
	 * <p>If the sh_info entry of the ordered section is a valid section within the same input file, the ordered section
	 * is sorted based on the relative ordering within the output file of the section pointed to by the sh_info entry.
	 * The section pointed at by the sh_info entry must be unordered, and cannot in turn specify SHF_LINK_ORDER or
	 * SHF_ORDERED.
	 *
	 * <p>The special sh_info values SHN_BEFORE and SHN_AFTER imply that the sorted section is to precede or follow,
	 * respectively, all other sections in the set being ordered. Input file link-line order is preserved if multiple
	 * sections in an ordered set have one of these special values.
	 *
	 * <p>In the absence of the sh_info ordering information, sections from a single input file combined within one
	 * section of the output file are contiguous. These sections have the same relative ordering as the sections appear
	 * in the input file. The contributions from multiple input files appear in link-line order.
	 */
	SHF_ORDERED(0x0000000004000000L, "ORDERED", "Special ordering requirement (Solaris)", 'x'),

	/**
	 * This section is excluded from input to the link-edit of an executable or shared object. This flag is ignored if
	 * the SHF_ALLOC flag is also set, or if relocations exist against the section.
	 */
	SHF_EXCLUDE(0x0000000008000000L, "EXCLUDE", "Section is excluded unless referenced or allocated (Solaris)", 'E');

	private static final long SHF_MASK = Arrays.stream(SectionHeaderFlags.values())
			.map(SectionHeaderFlags::getCode)
			.reduce(0L, (a, b) -> a | b);

	private static boolean isOSSpecific(final long code) {
		return (code & 0x000000000ff00000L) != 0L;
	}

	private static boolean isCPUSpecific(final long code) {
		return (code & 0x00000000f0000000L) != 0L;
	}

	/**
	 * Checks whether the given flags are valid.
	 *
	 * @param flags The ELF section header flags to be checked.
	 * @return True if the flags are valid, false otherwise.
	 */
	public static boolean isValid(final long flags) {
		return (flags & (~SHF_MASK)) == 0L;
	}

	/**
	 * Converts the given 64-bit flags into a Set of objects.
	 *
	 * @param flags The flags to be converted.
	 * @return A non-null Set of flags.
	 */
	public static Set<SectionHeaderFlags> fromLong(final long flags) {
		if (!isValid(flags)) {
			if (isOSSpecific(flags)) {
				throw new IllegalArgumentException(
						String.format("Unknown OS-specific Section header flag: 0x%016x", flags));
			}
			if (isCPUSpecific(flags)) {
				throw new IllegalArgumentException(
						String.format("Unknown CPU-specific Section header flag: 0x%016x", flags));
			}
			throw new IllegalArgumentException(String.format("Invalid SHF flags 0x%016x", flags));
		}
		final Set<SectionHeaderFlags> shf = new TreeSet<>();
		for (final SectionHeaderFlags f : SectionHeaderFlags.values()) {
			if ((flags & f.code) != 0L) { // NOPMD
				shf.add(f);
			}
		}
		return shf;
	}

	private final long code;
	private final String name;
	private final String description;
	private final char id;

	SectionHeaderFlags(final long code, final String name, final String description, final char id) {
		this.code = code;
		this.name = Objects.requireNonNull(name);
		this.description = Objects.requireNonNull(description);
		this.id = id;
	}

	/**
	 * Returns the code of this flag.
	 *
	 * @return The 64-bit code of this flag.
	 */
	public long getCode() {
		return code;
	}

	/**
	 * The name of this flag without the "SHF_" prefix.
	 *
	 * @return The name of this flag.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns a short description of this flag.
	 *
	 * @return The description of this flag.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns a single unique char for representing this flag for a compact string representation.
	 *
	 * @return The char identifier of this flag.
	 */
	public char getId() {
		return id;
	}

	@Override
	public String toString() {
		return "SectionHeaderFlags(code=" + code + ";name=" + name + ";description=" + description + ";id=" + id + ')';
	}
}
