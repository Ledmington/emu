/*
 * emu - Processor Emulator
 * Copyright (C) 2023-2024 Filippo Barbari <filippo.barbari@gmail.com>
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

import java.util.NoSuchElementException;

import com.ledmington.elf.section.Section;

/** An interface for ELF sections which behave like a symbol table. */
public interface SymbolTable extends Section {

	/**
	 * Returns the number of entries in the symbol table.
	 *
	 * @return The number of entries in the symbol table.
	 */
	int getSymbolTableLength();

	/**
	 * Returns the i-th entry in the symbol table.
	 *
	 * @param idx The index of the entry to return.
	 * @return The i-th entry in the symbol table.
	 */
	SymbolTableEntry getSymbolTableEntry(final int idx);

	/**
	 * Looks for a symbol with the given value and returns it.
	 *
	 * @param value The value to look for.
	 * @return The symbol with the given value, if it is present. Otherwise, a NoSuchElementException is thrown.
	 */
	default SymbolTableEntry getSymbolWithValue(final long value) {
		final int n = getSymbolTableLength();
		for (int i = 0; i < n; i++) {
			final SymbolTableEntry ste = getSymbolTableEntry(i);
			if (ste.value() == value) {
				return ste;
			}
		}
		throw new NoSuchElementException(
				String.format("No such symbol table entry with value %,d (0x%016x)", value, value));
	}
}
