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

import java.util.Objects;

/** The type of an entry of a .note ELF section. */
public enum NoteSectionEntryType {

	/** ABI information. */
	NT_GNU_ABI_TAG(1, "NT_GNU_ABI_TAG (ABI version tag)"),

	/** Synthetic hwcap information. */
	NT_GNU_HWCAP(2, "NT_GNU_HWCAP"),

	/** Build ID bits as generated by 'ld --build-id'. */
	NT_GNU_BUILD_ID(3, "NT_GNU_BUILD_ID (unique build ID bitstring)"),

	/** Version note generated by GNU gold containing a version string. */
	NT_GNU_GOLD_VERSION(4, "NT_GNU_GOLD_VERSION (gold version)"),

	/** Program property. */
	NT_GNU_PROPERTY_TYPE_0(5, "NT_GNU_PROPERTY_TYPE_0"),

	/** SystemTap USDT probe descriptors. */
	NT_STAPSDT(3, "NT_STAPSDT (SystemTap probe descriptors)"),

	/** Packaging metadata as defined <a href="https://systemd.io/ELF_PACKAGE_METADATA">here</a>. */
	NT_FDO_PACKAGING_METADATA(0xcafe1a7e, "FDO_PACKAGING_METADATA");

	private final int code;
	private final String description;

	NoteSectionEntryType(final int code, final String description) {
		this.code = code;
		this.description = Objects.requireNonNull(description);
	}

	/**
	 * Returns the type corresponding to the given 32-bit code.
	 *
	 * @param owner The owner of the entry.
	 * @param type The 32-bit code which must correspond to a known type.
	 * @return The type corresponding to the code.
	 */
	public static NoteSectionEntryType fromCode(final String owner, final int type) {
		return switch (owner) {
			case "GNU" ->
				switch (type) {
					case 1 -> NT_GNU_ABI_TAG;
					case 2 -> NT_GNU_HWCAP;
					case 3 -> NT_GNU_BUILD_ID;
					case 4 -> NT_GNU_GOLD_VERSION;
					case 5 -> NT_GNU_PROPERTY_TYPE_0;
					default ->
						throw new IllegalArgumentException(String.format(
								"Unknown note section entry type %d (0x%08x) for owner '%s'.", type, type, owner));
				};
			case "stapsdt" -> {
				if (type == NT_STAPSDT.getCode()) {
					yield NT_STAPSDT;
				} else {
					throw new IllegalArgumentException(String.format(
							"Unknown note section entry type %d (0x%08x) for owner '%s'.", type, type, owner));
				}
			}
			case "FDO" -> {
				if (type == NT_FDO_PACKAGING_METADATA.getCode()) {
					yield NT_FDO_PACKAGING_METADATA;
				} else {
					throw new IllegalArgumentException(String.format(
							"Unknown note section entry type %d (0x%08x) for owner '%s'.", type, type, owner));
				}
			}
			default ->
				throw new IllegalArgumentException(String.format("Unknown note section entry owner '%s'.", owner));
		};
	}

	/**
	 * Returns the 32-bit code of this type.
	 *
	 * @return The 32-bit code of this type.
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Returns the description of this type.
	 *
	 * @return The description of this type.
	 */
	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return "NoteSectionEntryType(code=" + code + ";description='" + description + '\'' + ')';
	}
}
