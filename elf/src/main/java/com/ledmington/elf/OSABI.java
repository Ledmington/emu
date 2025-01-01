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
package com.ledmington.elf;

import java.util.HashMap;
import java.util.Map;

/** The OS Application Binary Interface used for creating the ELF file. */
public enum OSABI {

	/** UNIX System V. */
	SYSTEM_V((byte) 0x00, "UNIX - System V"),

	/** HP - UX. */
	HP_UX((byte) 0x01, "HP-UX"),

	/** NetBSD. */
	NetBSD((byte) 0x02, "NetBSD"),

	/** Linux / GNU. */
	Linux((byte) 0x03, "UNIX - GNU"),

	/** GNU Hurd. */
	GNU_Hurd((byte) 0x04, "GNU Hurd"),

	/** Solaris. */
	Solaris((byte) 0x06, "Solaris"),

	/** AIX (Monterey). */
	AIX_Monterey((byte) 0x07, "AIX (Monterey)"),

	/** IRIX. */
	IRIX((byte) 0x08, "IRIX"),

	/** FreeBSD. */
	FreeBSD((byte) 0x09, "FreeBSD"),

	/** Tru64. */
	Tru64((byte) 0x0a, "Tru64"),

	/** Novell Modesto. */
	Novell_Modesto((byte) 0x0b, "Novell Modesto"),

	/** OpenBSD. */
	OpenBSD((byte) 0x0c, "OpenBSD"),

	/** OpenVMS. */
	OpenVMS((byte) 0x0d, "OpenVMS"),

	/** NonStop Kernel. */
	NonStop_Kernel((byte) 0x0e, "NonStop Kernel"),

	/** AROS. */
	AROS((byte) 0x0f, "AROS"),

	/** Fenix OS. */
	Fenix_OS((byte) 0x10, "Fenix OS"),

	/** Nuxi CloudABI. */
	Nuxi_CloudABI((byte) 0x11, "Nuxi CloudABI"),

	/** Stratus Technologies OpenVOS. */
	Stratus_Technologies_OpenVOS((byte) 0x12, "Stratus Technologies OpenVOS"),

	/** Standalone (embedded) application. */
	STANDALONE((byte) 0xff, "Standalone (embedded) application");

	private static final Map<Byte, OSABI> codeToABI = new HashMap<>();

	static {
		for (final OSABI x : OSABI.values()) {
			if (codeToABI.containsKey(x.code)) {
				throw new IllegalStateException(String.format(
						"OSABI enum value with code %d (0x%02x) and name '%s' already exists",
						x.code, x.code, x.OSName));
			}
			codeToABI.put(x.code, x);
		}
	}

	/**
	 * Checks whether the given code corresponds to an existing OS ABI object.
	 *
	 * @param code The code to look for.
	 * @return True if an OSABI object exists, false otherwise.
	 */
	public static boolean isValid(final byte code) {
		return codeToABI.containsKey(code);
	}

	/**
	 * Finds the OSABI object corresponding to the given code.
	 *
	 * @param code The code to look for.
	 * @return The OSABI object.
	 */
	public static OSABI fromCode(final byte code) {
		if (!codeToABI.containsKey(code)) {
			throw new IllegalArgumentException(String.format("Unknown ELF OS/ABI identifier: 0x%02x", code));
		}
		return codeToABI.get(code);
	}

	private final byte code;
	private final String OSName;

	OSABI(final byte code, final String OSName) {
		this.code = code;
		this.OSName = OSName;
	}

	/**
	 * Hexadecimal 1-byte code.
	 *
	 * @return The code of this OSABI object.
	 */
	public byte getCode() {
		return this.code;
	}

	/**
	 * Name of the OS ABI.
	 *
	 * @return A String representation of this OS ABI object.
	 */
	public String getName() {
		return this.OSName;
	}

	@Override
	public String toString() {
		return "OSABI(code=" + code + ";OSName=" + OSName + ')';
	}
}
