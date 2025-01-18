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
package com.ledmington.cpu.x86;

import java.util.List;

/** Common interface for x86 instruction decoders. */
public interface InstructionDecoder {

	/**
	 * Decodes all instructions that are found up to n bytes ahead.
	 *
	 * @param nBytesToDecode Maximum number of bytes to read.
	 * @return A list of decoded instructions.
	 */
	List<Instruction> decodeAll(int nBytesToDecode);

	/**
	 * Decodes a single instruction reading as few bytes as possible.
	 *
	 * @return The decoded instruction.
	 */
	Instruction decode();
}
