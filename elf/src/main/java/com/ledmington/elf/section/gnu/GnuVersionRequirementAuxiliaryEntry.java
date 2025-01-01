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
package com.ledmington.elf.section.gnu;

/**
 * This class holds auxiliary information regarding the entries in the .gnu.version_r section.
 *
 * @param hash Dependency name hash value (ELF hash function).
 * @param flags Dependency information flag bitmask.
 * @param other Object file version identifier used in the .gnu.version symbol version array. Bit number 15 controls
 *     whether or not the object is hidden; if this bit is set, the object cannot be used and the static linker will
 *     ignore the symbol's presence in the object.
 * @param nameOffset Offset to the dependency name string in the section header, in bytes.
 * @param nextOffset Offset to the next vernaux entry, in bytes.
 */
public record GnuVersionRequirementAuxiliaryEntry(int hash, short flags, short other, int nameOffset, int nextOffset) {}
