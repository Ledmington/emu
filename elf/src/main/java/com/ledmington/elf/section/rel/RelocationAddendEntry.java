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
package com.ledmington.elf.section.rel;

/**
 * An entry of a relocation table with explicit addend.
 *
 * @param offset This member gives the location at which to apply the relocation action. For a relocatable file, the
 *     value is the byte offset from the beginning of the section to the storage unit affected by the relocation. For an
 *     executable file or a shared object, the value is the virtual address of the storage unit affected by the
 *     relocation.
 * @param symbolTableIndex This member gives the symbol table index with respect to which the relocation must be made.
 * @param type This member gives the type of relocation to apply.
 * @param addend This member specifies a constant addend used to compute the value to be stored into the relocatable
 *     field.
 */
public record RelocationAddendEntry(long offset, int symbolTableIndex, RelocationAddendEntryType type, long addend) {}
