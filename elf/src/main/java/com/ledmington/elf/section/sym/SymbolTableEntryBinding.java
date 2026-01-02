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
package com.ledmington.elf.section.sym;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.ledmington.utils.BitUtils;

/** A symbol's binding determines the linkage visibility and behavior. */
public enum SymbolTableEntryBinding {

	/**
	 * Local symbols are not visible outside the object file containing their definition. Local symbols of the same name
	 * may exist in multiple files without interfering with each other.
	 */
	STB_LOCAL((byte) 0x00, "LOCAL"),

	/**
	 * Global symbols are visible to all object files being combined. One file's definition of a global symbol will
	 * satisfy another file's undefined reference to the same global symbol.
	 */
	STB_GLOBAL((byte) 0x01, "GLOBAL"),

	/** Weak symbols resemble global symbols, but their definitions have lower precedence. */
	STB_WEAK((byte) 0x02, "WEAK");

	private static final Map<Byte, SymbolTableEntryBinding> codeToBind = new ConcurrentHashMap<>();

	private final byte code;
	private final String name;

	static {
		for (final SymbolTableEntryBinding bind : values()) {
			codeToBind.put(bind.code, bind);
		}
	}

	private static boolean isOSSpecific(final byte code) {
		return BitUtils.and(code, (byte) 0x0a) == (byte) 0x0a;
	}

	private static boolean isCPUSpecific(final byte code) {
		return BitUtils.and(code, (byte) 0x0c) == (byte) 0x0c;
	}

	/**
	 * Returns the proper STE binding object corresponding to the given code.
	 *
	 * @param code The code of the binding object.
	 * @return A non-null STE binding object.
	 */
	public static SymbolTableEntryBinding fromCode(final byte code) {
		if (!codeToBind.containsKey(code)) {
			if (isOSSpecific(code)) {
				throw new IllegalArgumentException(
						String.format("Unknown OS-specific Symbol table entry bind identifier: 0x%02x", code));
			}
			if (isCPUSpecific(code)) {
				throw new IllegalArgumentException(
						String.format("Unknown CPU-specific Symbol table entry bind identifier: 0x%02x", code));
			}
			throw new IllegalArgumentException(
					String.format("Unknown Symbol table entry bind identifier: 0x%02x", code));
		}
		return codeToBind.get(code);
	}

	SymbolTableEntryBinding(final byte code, final String name) {
		this.code = code;
		this.name = Objects.requireNonNull(name);
	}

	/**
	 * Returns the code of this object.
	 *
	 * @return The code of this object.
	 */
	public byte getCode() {
		return code;
	}

	/**
	 * Returns the name of this object without the "STB_" prefix.
	 *
	 * @return The name of this object.
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "SymbolTableEntryBinding(name=" + name + ";code=" + code + ")";
	}
}
