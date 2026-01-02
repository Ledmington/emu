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

/**
 * A symbol's visibility, determined from its st_other field, may be specified in a relocatable object. This visibility
 * defines how that symbol may be accessed once the symbol has become part of an executable or shared object.
 */
public enum SymbolTableEntryVisibility {

	/**
	 * The visibility of symbols with the STV_DEFAULT attribute is as specified by the symbol's binding type. That is,
	 * global and weak symbols are visible outside their defining component, the executable file or shared object. Local
	 * symbols are hidden. Global and weak symbols can also be preempted, that is, they may be interposed by definitions
	 * of the same name in another component.
	 */
	STV_DEFAULT((byte) 0, "DEFAULT"),

	/** This visibility attribute is currently reserved. */
	STV_INTERNAL((byte) 1, "INTERNAL"),

	/**
	 * A symbol defined in the current component is hidden if its name is not visible to other components. Such a symbol
	 * is necessarily protected. This attribute is used to control the external interface of a component. An object
	 * named by such a symbol may still be referenced from another component if its address is passed outside.
	 *
	 * <p>A hidden symbol contained in a relocatable object is either removed or converted to STB_LOCAL binding by the
	 * link-editor when the relocatable object is included in an executable file or shared object.
	 */
	STV_HIDDEN((byte) 2, "HIDDEN"),

	/**
	 * A symbol defined in the current component is protected if it is visible in other components but cannot be
	 * preempted. Any reference to such a symbol from within the defining component must be resolved to the definition
	 * in that component, even if there is a definition in another component that would interpose by the default rules.
	 * A symbol with STB_LOCAL binding will not have STV_PROTECTED visibility.
	 */
	STV_PROTECTED((byte) 3, "PROTECTED");

	private static final Map<Byte, SymbolTableEntryVisibility> codeToVisibility = new ConcurrentHashMap<>();

	static {
		for (final SymbolTableEntryVisibility visibility : values()) {
			codeToVisibility.put(visibility.code, visibility);
		}
	}

	private final byte code;
	private final String name;

	/**
	 * Returns the STV object corresponding to the given code.
	 *
	 * @param code The code representing the STV object.
	 * @return The STV object.
	 */
	public static SymbolTableEntryVisibility fromByte(final byte code) {
		if (!codeToVisibility.containsKey(code)) {
			throw new IllegalArgumentException(
					String.format("Unknown Symbol table entry visibility identifier: 0x%02x", code));
		}
		return codeToVisibility.get(code);
	}

	SymbolTableEntryVisibility(final byte code, final String name) {
		this.code = code;
		this.name = Objects.requireNonNull(name);
	}

	/**
	 * Returns the 8-bit code of this STV object.
	 *
	 * @return The byte representing the code.
	 */
	public byte getCode() {
		return code;
	}

	/**
	 * Returns the name of this STV object without the "STV_" prefix.
	 *
	 * @return The name of this STV object.
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "SymbolTableEntryVisibility(code=" + code + ";name=" + name + ')';
	}
}
