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
package com.ledmington.elf.section;

/**
 * An entry of the .gnu.version_r section. Usually this structure is called Elfxx_Verneed in the ELF documentation.
 *
 * <p>Useful reference <a href=
 * "https://refspecs.linuxfoundation.org/LSB_3.0.0/LSB-PDA/LSB-PDA.junk/symversion.html">here</a>.
 *
 * @param version Version of structure. This value is currently set to 1, and will be reset if the versioning
 *     implementation is incompatibly altered.
 * @param count Number of associated verneed array entries.
 * @param fileOffset Offset to the file name string in the section header, in bytes.
 * @param auxOffset Offset to a corresponding entry in the vernaux array, in bytes.
 * @param nextOffset Offset to the next verneed entry, in bytes.
 */
public record GnuVersionRequirementEntry(short version, short count, int fileOffset, int auxOffset, int nextOffset) {}
